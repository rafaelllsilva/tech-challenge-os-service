package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.exception.BadRequestException;
import com.fiap.techchallenge.os.exception.ConflictException;
import com.fiap.techchallenge.os.exception.EventoPrematuroException;
import com.fiap.techchallenge.os.exception.NotFoundException;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoDesconhecidoException;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoRepository;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.observabilidade.MetricasDaSaga;
import com.fiap.techchallenge.os.ordemservico.observabilidade.OrdemDeServicoEventLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Desserializa, deduplica por eventId e despacha para o {@link ManipuladorDeEvento} do tipo.
 * A verificação, o handler e o registro em evento_processado rodam numa única transação.
 * Erros de negócio e eventos sem tratamento são confirmados (retornam resultado); erros técnicos
 * e eventos que chegaram antes do pré-requisito são relançados, e a mensagem volta para a fila
 * ou vai para a DLQ.
 */
@Slf4j
@Component
public class DespachanteDeEventos {

    private final EventoSerializer serializer;
    private final EventoProcessadoRepository processadoRepository;
    private final TransactionOperations transacao;
    private final Clock clock;
    private final MetricasDaSaga metricas;
    private final OrdemDeServicoEventLogger loggerEventos;
    private final LogDeMensagens logMensagens;
    private final Map<TipoEvento, ManipuladorDeEvento<?>> manipuladores = new EnumMap<>(TipoEvento.class);

    public DespachanteDeEventos(EventoSerializer serializer, EventoProcessadoRepository processadoRepository,
                                TransactionOperations transacao, Clock clock,
                                MetricasDaSaga metricas, OrdemDeServicoEventLogger loggerEventos,
                                LogDeMensagens logMensagens, List<ManipuladorDeEvento<?>> manipuladores) {
        this.serializer = serializer;
        this.processadoRepository = processadoRepository;
        this.transacao = transacao;
        this.clock = clock;
        this.metricas = metricas;
        this.loggerEventos = loggerEventos;
        this.logMensagens = logMensagens;
        for (ManipuladorDeEvento<?> manipulador : manipuladores) {
            if (this.manipuladores.putIfAbsent(manipulador.tipo(), manipulador) != null) {
                throw new IllegalStateException("Mais de um manipulador para o evento " + manipulador.tipo());
            }
        }
    }

    public ResultadoDoProcessamento despachar(String json) {
        EventEnvelope<?> evento;
        try {
            evento = serializer.desserializar(json);
        } catch (EventoDesconhecidoException e) {
            log.warn("Evento descartado: {}", e.getMessage());
            metricas.eventoConsumido("DESCONHECIDO", MetricasDaSaga.RESULTADO_IGNORADO);
            logMensagens.consumida(null, null, null, "descartado", e.getMessage());
            return ResultadoDoProcessamento.TIPO_DESCONHECIDO;
        }

        MDC.put("correlationId", evento.correlationId());
        MDC.put("eventId", evento.eventId().toString());
        MDC.put("eventType", evento.type().name());
        try {
            ResultadoDoProcessamento resultado = transacao.execute(status -> processar(evento));
            registrarConsumo(evento, resultado);
            logarResultado(evento, resultado);
            return resultado;
        } catch (EventoPrematuroException e) {
            log.warn("Evento {} adiado, aguardando pré-requisito: {}", evento.type(), e.getMessage());
            registrarConsumo(evento, MetricasDaSaga.RESULTADO_ADIADO);
            logarConsumo(evento, "adiado", e.getMessage());
            throw e;
        } catch (BadRequestException | ConflictException | NotFoundException | IllegalStateException e) {
            log.warn("Evento {} rejeitado por regra de negócio: {}", evento.type(), e.getMessage());
            registrarConsumo(evento, ResultadoDoProcessamento.ERRO_NEGOCIO);
            logarConsumo(evento, "rejeitado", e.getMessage());
            return ResultadoDoProcessamento.ERRO_NEGOCIO;
        } catch (RuntimeException e) {
            registrarConsumo(evento, MetricasDaSaga.RESULTADO_ERRO_TECNICO);
            logarConsumo(evento, "erro_tecnico", e.getMessage());
            throw e;
        } finally {
            MDC.remove("correlationId");
            MDC.remove("eventId");
            MDC.remove("eventType");
        }
    }

    // ERRO_NEGOCIO e TIPO_DESCONHECIDO já são logados no catch e na desserialização.
    private void logarResultado(EventEnvelope<?> evento, ResultadoDoProcessamento resultado) {
        switch (resultado) {
            case SUCESSO -> logarConsumo(evento, "confirmado", LogDeMensagens.descricaoDoConsumo(evento.type().name()));
            case DUPLICADO -> logarConsumo(evento, "duplicado", "Evento já processado anteriormente, ignorado");
            case SEM_HANDLER -> logarConsumo(evento, "ignorado", "Nenhum manipulador para o evento, confirmado sem processar");
            case ERRO_NEGOCIO, TIPO_DESCONHECIDO -> { }
        }
    }

    private void logarConsumo(EventEnvelope<?> evento, String outcome, String message) {
        logMensagens.consumida(evento.correlationId(), evento.eventId().toString(), evento.type().name(), outcome, message);
    }

    private void registrarConsumo(EventEnvelope<?> evento, ResultadoDoProcessamento resultado) {
        registrarConsumo(evento, switch (resultado) {
            case SUCESSO -> MetricasDaSaga.RESULTADO_SUCESSO;
            case DUPLICADO -> MetricasDaSaga.RESULTADO_DUPLICADO;
            case ERRO_NEGOCIO -> MetricasDaSaga.RESULTADO_ERRO_NEGOCIO;
            case SEM_HANDLER, TIPO_DESCONHECIDO -> MetricasDaSaga.RESULTADO_IGNORADO;
        });
    }

    private void registrarConsumo(EventEnvelope<?> evento, String resultado) {
        metricas.eventoConsumido(evento.type().name(), resultado);
        loggerEventos.registrarEventoConsumido(evento.type().name(), resultado);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResultadoDoProcessamento processar(EventEnvelope<?> evento) {
        if (processadoRepository.existsById(evento.eventId())) {
            log.info("Evento {} já processado, ignorando", evento.eventId());
            return ResultadoDoProcessamento.DUPLICADO;
        }
        ManipuladorDeEvento manipulador = manipuladores.get(evento.type());
        if (manipulador == null) {
            log.warn("Nenhum manipulador para o evento {}, confirmando sem processar", evento.type());
            return ResultadoDoProcessamento.SEM_HANDLER;
        }
        manipulador.tratar(evento);
        processadoRepository.save(new EventoProcessadoEntity(
                evento.eventId(),
                evento.type().name(),
                evento.correlationId(),
                serializer.serializarPayload(evento.payload()),
                evento.occurredAt(),
                clock.instant()));
        return ResultadoDoProcessamento.SUCESSO;
    }
}

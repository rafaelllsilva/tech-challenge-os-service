package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.exception.BadRequestException;
import com.fiap.techchallenge.os.exception.ConflictException;
import com.fiap.techchallenge.os.exception.NotFoundException;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoDesconhecidoException;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoRepository;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
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
 * são relançados, e a mensagem volta para a fila ou vai para a DLQ.
 */
@Slf4j
@Component
public class DespachanteDeEventos {

    private final EventoSerializer serializer;
    private final EventoProcessadoRepository processadoRepository;
    private final TransactionOperations transacao;
    private final Clock clock;
    private final Map<TipoEvento, ManipuladorDeEvento<?>> manipuladores = new EnumMap<>(TipoEvento.class);

    public DespachanteDeEventos(EventoSerializer serializer, EventoProcessadoRepository processadoRepository,
                                TransactionOperations transacao, Clock clock,
                                List<ManipuladorDeEvento<?>> manipuladores) {
        this.serializer = serializer;
        this.processadoRepository = processadoRepository;
        this.transacao = transacao;
        this.clock = clock;
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
            return ResultadoDoProcessamento.TIPO_DESCONHECIDO;
        }

        MDC.put("correlationId", evento.correlationId());
        MDC.put("eventId", evento.eventId().toString());
        MDC.put("eventType", evento.type().name());
        try {
            return transacao.execute(status -> processar(evento));
        } catch (BadRequestException | ConflictException | NotFoundException | IllegalStateException e) {
            log.warn("Evento {} rejeitado por regra de negócio: {}", evento.type(), e.getMessage());
            return ResultadoDoProcessamento.ERRO_NEGOCIO;
        } finally {
            MDC.remove("correlationId");
            MDC.remove("eventId");
            MDC.remove("eventType");
        }
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

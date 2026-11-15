package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.Topico;
import com.fiap.techchallenge.os.mensageria.framework.MensageriaProperties;
import com.fiap.techchallenge.os.mensageria.framework.OutboxRelayProperties;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import com.fiap.techchallenge.os.observabilidade.MetricasDaSaga;
import com.fiap.techchallenge.os.ordemservico.observabilidade.OrdemDeServicoEventLogger;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Envia os eventos PENDENTE da outbox para o SNS. A entrega é "pelo menos uma vez": se a transação
 * falhar depois do envio, o evento é reenviado, e os consumidores deduplicam por eventId.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "os.outbox.relay.habilitado", havingValue = "true", matchIfMissing = true)
public class RelayDoOutbox {

    private final OutboxEventoRepository repository;
    private final SnsTemplate snsTemplate;
    private final MensageriaProperties mensageria;
    private final OutboxRelayProperties propriedades;
    private final Clock clock;
    private final MetricasDaSaga metricas;
    private final OrdemDeServicoEventLogger loggerEventos;
    private final LogDeMensagens logMensagens;

    public RelayDoOutbox(OutboxEventoRepository repository, SnsTemplate snsTemplate,
                         MensageriaProperties mensageria, OutboxRelayProperties propriedades, Clock clock,
                         MetricasDaSaga metricas, OrdemDeServicoEventLogger loggerEventos,
                         LogDeMensagens logMensagens) {
        this.repository = repository;
        this.snsTemplate = snsTemplate;
        this.mensageria = mensageria;
        this.propriedades = propriedades;
        this.clock = clock;
        this.metricas = metricas;
        this.loggerEventos = loggerEventos;
        this.logMensagens = logMensagens;
    }

    @Scheduled(fixedDelayString = "${os.outbox.relay.intervalo-ms:1000}")
    @Transactional
    public int processarLote() {
        List<OutboxEventoEntity> lote = repository.findByStatusOrderByOcorridoEmAsc(
                StatusOutbox.PENDENTE, PageRequest.of(0, propriedades.tamanhoLote()));
        lote.forEach(this::enviar);
        return lote.size();
    }

    private void enviar(OutboxEventoEntity evento) {
        MDC.put("correlationId", evento.getCorrelationId());
        MDC.put("eventId", evento.getId().toString());
        MDC.put("eventType", evento.getTipo());
        try {
            snsTemplate.send(destino(evento), MessageBuilder.withPayload(evento.getPayload())
                    .setHeader("eventId", evento.getId().toString())
                    .setHeader("type", evento.getTipo())
                    .setHeader("correlationId", evento.getCorrelationId())
                    .build());
            evento.setStatus(StatusOutbox.ENVIADO);
            evento.setEnviadoEm(Instant.now(clock));
            evento.setUltimoErro(null);
            metricas.eventoPublicado(evento.getTipo());
            loggerEventos.registrarEventoPublicado(evento.getTipo(), evento.getTopico());
            logarPublicacao(evento, "publicado", LogDeMensagens.descricaoDaPublicacao(evento.getTipo()));
        } catch (Exception e) {
            registrarFalha(evento, e);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("eventId");
            MDC.remove("eventType");
        }
    }

    private void registrarFalha(OutboxEventoEntity evento, Exception e) {
        evento.setTentativas(evento.getTentativas() + 1);
        evento.setUltimoErro(e.toString());
        if (evento.getTentativas() >= propriedades.maxTentativas()) {
            evento.setStatus(StatusOutbox.FALHOU);
            log.error("Evento {} marcado como FALHOU após {} tentativas", evento.getTipo(), evento.getTentativas(), e);
            logarPublicacao(evento, "falhou", e.toString());
        } else {
            log.warn("Falha ao publicar evento {} (tentativa {}): {}", evento.getTipo(), evento.getTentativas(), e.toString());
            logarPublicacao(evento, "falha", e.toString());
        }
    }

    private void logarPublicacao(OutboxEventoEntity evento, String outcome, String message) {
        logMensagens.publicada(evento.getCorrelationId(), evento.getId().toString(), evento.getTipo(), outcome, message);
    }

    private String destino(OutboxEventoEntity evento) {
        return mensageria.destino(topicoPorNome(evento.getTopico()));
    }

    private static Topico topicoPorNome(String nome) {
        for (Topico topico : Topico.values()) {
            if (topico.nome().equals(nome)) {
                return topico;
            }
        }
        throw new IllegalStateException("Tópico desconhecido na outbox: " + nome);
    }
}

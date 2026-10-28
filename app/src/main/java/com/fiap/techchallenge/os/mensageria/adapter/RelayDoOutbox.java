package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.Topico;
import com.fiap.techchallenge.os.mensageria.framework.MensageriaProperties;
import com.fiap.techchallenge.os.mensageria.framework.OutboxRelayProperties;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
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

    public RelayDoOutbox(OutboxEventoRepository repository, SnsTemplate snsTemplate,
                         MensageriaProperties mensageria, OutboxRelayProperties propriedades, Clock clock) {
        this.repository = repository;
        this.snsTemplate = snsTemplate;
        this.mensageria = mensageria;
        this.propriedades = propriedades;
        this.clock = clock;
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
            log.info("Evento {} publicado no tópico {}", evento.getTipo(), evento.getTopico());
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
        } else {
            log.warn("Falha ao publicar evento {} (tentativa {}): {}", evento.getTipo(), evento.getTentativas(), e.toString());
        }
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

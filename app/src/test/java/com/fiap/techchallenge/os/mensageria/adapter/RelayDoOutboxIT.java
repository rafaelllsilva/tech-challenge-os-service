package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OsCriadaPayload;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class RelayDoOutboxIT {

    private static final String FILA_TESTE = "test-os-events";

    @Autowired
    private OutboxPublicadorDeEventos publicador;

    @Autowired
    private OutboxEventoRepository repository;

    @Autowired
    private EventoSerializer serializer;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void relayPublicaEventoPendenteNoSnsEEleChegaNaFilaDeTeste() {
        EventEnvelope<OsCriadaPayload> evento = EventEnvelope.novo(TipoEvento.OS_CRIADA, "77",
                new OsCriadaPayload("77", "1", "2", "Revisão geral", java.time.Instant.parse("2026-01-01T10:00:00Z")),
                Clock.systemUTC());

        new TransactionTemplate(transactionManager).executeWithoutResult(s -> publicador.publicar(evento));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            OutboxEventoEntity gravado = repository.findById(evento.eventId()).orElseThrow();
            assertThat(gravado.getStatus()).isEqualTo(StatusOutbox.ENVIADO);
            assertThat(gravado.getEnviadoEm()).isNotNull();
        });

        // A fila pode conter mensagens de outros testes: procura a que tem o eventId esperado.
        AtomicReference<Message<String>> encontrada = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            Optional<Message<String>> recebida = sqsTemplate.receive(
                    from -> from.queue(FILA_TESTE).pollTimeout(Duration.ofSeconds(1)), String.class);
            recebida.filter(m -> m.getPayload().contains(evento.eventId().toString())).ifPresent(encontrada::set);
            assertThat(encontrada.get()).isNotNull();
        });

        Message<String> mensagem = encontrada.get();
        assertThat(serializer.desserializar(mensagem.getPayload())).isEqualTo(evento);
        assertThat(mensagem.getHeaders())
                .containsEntry("eventId", evento.eventId().toString())
                .containsEntry("type", "OS_CRIADA")
                .containsEntry("correlationId", "77");
    }
}

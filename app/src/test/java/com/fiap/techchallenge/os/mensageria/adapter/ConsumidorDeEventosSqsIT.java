package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.EstoqueReservadoPayload;
import com.fiap.techchallenge.os.mensageria.framework.persistence.EventoProcessadoRepository;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Import(ConsumidorDeEventosSqsIT.ManipuladorFake.class)
class ConsumidorDeEventosSqsIT {

    private static final String FILA_BILLING = "os-service-billing-events";

    /** Handler de teste: só registra os eventIds recebidos. */
    @TestConfiguration
    static class ManipuladorFake {
        static final List<UUID> TRATADOS = new CopyOnWriteArrayList<>();

        @Bean
        ManipuladorDeEvento<EstoqueReservadoPayload> manipuladorFakeDeEstoque() {
            return new ManipuladorDeEvento<>() {
                @Override
                public TipoEvento tipo() {
                    return TipoEvento.ESTOQUE_RESERVADO;
                }

                @Override
                public void tratar(EventEnvelope<EstoqueReservadoPayload> evento) {
                    TRATADOS.add(evento.eventId());
                }
            };
        }
    }

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private EventoSerializer serializer;

    @Autowired
    private EventoProcessadoRepository processadoRepository;

    private EventEnvelope<EstoqueReservadoPayload> evento() {
        return EventEnvelope.novo(TipoEvento.ESTOQUE_RESERVADO, "99", new EstoqueReservadoPayload("99"), Clock.systemUTC());
    }

    @Test
    void mensagemEnviadaDuasVezesEhProcessadaUmaUnicaVez() throws Exception {
        var duplicado = evento();
        var sentinela = evento();

        sqsTemplate.send(to -> to.queue(FILA_BILLING).payload(serializer.serializar(duplicado)));
        sqsTemplate.send(to -> to.queue(FILA_BILLING).payload(serializer.serializar(duplicado)));
        sqsTemplate.send(to -> to.queue(FILA_BILLING).payload(serializer.serializar(sentinela)));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(ManipuladorFake.TRATADOS).contains(duplicado.eventId(), sentinela.eventId()));
        Thread.sleep(3000); // dá tempo para a cópia duplicada ser consumida e ignorada

        assertThat(ManipuladorFake.TRATADOS.stream().filter(duplicado.eventId()::equals)).hasSize(1);
        assertThat(processadoRepository.existsById(duplicado.eventId())).isTrue();
        var registrado = processadoRepository.findById(duplicado.eventId()).orElseThrow();
        assertThat(registrado.getTipo()).isEqualTo("ESTOQUE_RESERVADO");
        assertThat(registrado.getCorrelationId()).isEqualTo("99");
    }
}

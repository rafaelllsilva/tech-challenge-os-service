package com.fiap.techchallenge.os.mensageria.adapter;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OrcamentoAprovadoPayload;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxPublicadorDeEventosTest {

    private final OutboxEventoRepository repository = mock(OutboxEventoRepository.class);
    private final EventoSerializer serializer = new EventoSerializer(JsonMapper.builder().build());
    private final OutboxPublicadorDeEventos publicador = new OutboxPublicadorDeEventos(repository, serializer);

    @Test
    void gravaEventoPendenteNaOutboxComEnvelopeSerializado() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        EventEnvelope<OrcamentoAprovadoPayload> evento = EventEnvelope.novo(
                TipoEvento.ORCAMENTO_APROVADO, "42", new OrcamentoAprovadoPayload("42", "orc-1"), clock);

        publicador.publicar(evento);

        ArgumentCaptor<OutboxEventoEntity> captor = ArgumentCaptor.forClass(OutboxEventoEntity.class);
        verify(repository).save(captor.capture());
        OutboxEventoEntity gravado = captor.getValue();
        assertThat(gravado.getId()).isEqualTo(evento.eventId());
        assertThat(gravado.getTipo()).isEqualTo("ORCAMENTO_APROVADO");
        assertThat(gravado.getTopico()).isEqualTo("os-events");
        assertThat(gravado.getCorrelationId()).isEqualTo("42");
        assertThat(gravado.getOcorridoEm()).isEqualTo(evento.occurredAt());
        assertThat(gravado.getStatus()).isEqualTo(StatusOutbox.PENDENTE);
        assertThat(gravado.getTentativas()).isZero();
        assertThat(serializer.desserializar(gravado.getPayload())).isEqualTo(evento);
    }
}

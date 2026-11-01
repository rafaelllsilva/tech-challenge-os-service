package com.fiap.techchallenge.os.mensageria.contrato;

import com.fiap.techchallenge.os.mensageria.contrato.payload.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventoSerializerTest {

    private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(AGORA, ZoneOffset.UTC);
    private static final String OS_ID = "42";

    private static final Map<TipoEvento, Object> PAYLOADS = Map.ofEntries(
            Map.entry(TipoEvento.OS_CRIADA, new OsCriadaPayload(OS_ID, "1", "2", "Trocar óleo", AGORA)),
            Map.entry(TipoEvento.DIAGNOSTICO_CONCLUIDO, new DiagnosticoConcluidoPayload(OS_ID)),
            Map.entry(TipoEvento.ORCAMENTO_GERADO, new OrcamentoGeradoPayload(OS_ID, "orc-1", new BigDecimal("350.5"))),
            Map.entry(TipoEvento.ORCAMENTO_APROVADO, new OrcamentoAprovadoPayload(OS_ID, "orc-1")),
            Map.entry(TipoEvento.ORCAMENTO_REJEITADO, new OrcamentoRejeitadoPayload(OS_ID, "orc-1", "caro")),
            Map.entry(TipoEvento.PAGAMENTO_CONFIRMADO, new PagamentoConfirmadoPayload(OS_ID, "pag-1", new BigDecimal("350.5"))),
            Map.entry(TipoEvento.PAGAMENTO_FALHOU, new PagamentoFalhouPayload(OS_ID, "pag-1", "cartão recusado")),
            Map.entry(TipoEvento.PAGAMENTO_ESTORNADO, new PagamentoEstornadoPayload(OS_ID, "pag-1")),
            Map.entry(TipoEvento.ESTOQUE_RESERVADO, new EstoqueReservadoPayload(OS_ID)),
            Map.entry(TipoEvento.EXECUCAO_INICIADA, new ExecucaoIniciadaPayload(OS_ID, AGORA)),
            Map.entry(TipoEvento.EXECUCAO_FINALIZADA, new ExecucaoFinalizadaPayload(OS_ID, AGORA)),
            Map.entry(TipoEvento.EXECUCAO_FALHOU, new ExecucaoFalhouPayload(OS_ID, "sem estoque")),
            Map.entry(TipoEvento.OS_CANCELADA, new OsCanceladaPayload(OS_ID, "desistiu", "AGUARDANDO_APROVACAO"))
    );

    private final EventoSerializer serializer = new EventoSerializer(
            JsonMapper.builder().build());

    @Test
    void catalogoDePayloadsCobreTodosOsTipos() {
        assertThat(PAYLOADS.keySet()).isEqualTo(Set.of(TipoEvento.values()));
    }

    @ParameterizedTest
    @EnumSource(TipoEvento.class)
    void roundTripPreservaEnvelopeEPayload(TipoEvento tipo) {
        Object payload = PAYLOADS.get(tipo);
        assertThat(payload).isInstanceOf(tipo.tipoPayload());
        EventEnvelope<Object> original = EventEnvelope.novo(tipo, OS_ID, payload, CLOCK);

        EventEnvelope<?> lido = serializer.desserializar(serializer.serializar(original));

        assertThat(lido).isEqualTo(original);
        assertThat(lido.payload()).isInstanceOf(tipo.tipoPayload());
    }

    @Test
    void novoGeraEventIdEVersao1() {
        EventEnvelope<Object> a = EventEnvelope.novo(TipoEvento.OS_CANCELADA, OS_ID, PAYLOADS.get(TipoEvento.OS_CANCELADA), CLOCK);
        EventEnvelope<Object> b = EventEnvelope.novo(TipoEvento.OS_CANCELADA, OS_ID, PAYLOADS.get(TipoEvento.OS_CANCELADA), CLOCK);

        assertThat(a.eventId()).isNotNull().isNotEqualTo(b.eventId());
        assertThat(a.version()).isEqualTo(1);
        assertThat(a.occurredAt()).isEqualTo(AGORA);
    }

    @Test
    void jsonUsaOsNomesDoEnvelope() {
        String json = serializer.serializar(EventEnvelope.novo(
                TipoEvento.ORCAMENTO_APROVADO, OS_ID, PAYLOADS.get(TipoEvento.ORCAMENTO_APROVADO), CLOCK));

        assertThat(json).contains("\"eventId\"", "\"type\":\"ORCAMENTO_APROVADO\"", "\"version\":1",
                "\"occurredAt\":\"2026-01-01T10:00:00Z\"", "\"correlationId\":\"42\"", "\"payload\":{");
    }

    @Test
    void tipoDesconhecidoLancaEventoDesconhecidoException() {
        String json = """
                {"eventId":"7f1c1a52-6c6f-4a8e-9a5d-3c1f0d2b9b11","type":"INEXISTENTE","version":1,
                 "occurredAt":"2026-01-01T10:00:00Z","correlationId":"42","payload":{}}""";

        assertThatThrownBy(() -> serializer.desserializar(json))
                .isInstanceOf(EventoDesconhecidoException.class)
                .hasMessageContaining("INEXISTENTE");
    }

    @Test
    void tipoAusenteLancaEventoDesconhecidoException() {
        assertThatThrownBy(() -> serializer.desserializar("{\"payload\":{}}"))
                .isInstanceOf(EventoDesconhecidoException.class);
    }

    @Test
    void cadaTipoPertenceAoTopicoEsperado() {
        assertThat(TipoEvento.OS_CRIADA.topico().nome()).isEqualTo("os-events");
        assertThat(TipoEvento.PAGAMENTO_CONFIRMADO.topico().nome()).isEqualTo("billing-events");
        assertThat(TipoEvento.EXECUCAO_FALHOU.topico().nome()).isEqualTo("execution-events");
    }
}

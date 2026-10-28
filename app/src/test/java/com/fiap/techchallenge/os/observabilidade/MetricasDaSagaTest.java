package com.fiap.techchallenge.os.observabilidade;

import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.observabilidade.OrdemDeServicoEventLogger.StatusDaOrdemDeServicoAlterado;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricasDaSagaTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OutboxEventoRepository outbox = mock(OutboxEventoRepository.class);
    private final MetricasDaSaga metricas = new MetricasDaSaga(registry, outbox);

    @Test
    void contaEventosPublicadosPorTipo() {
        metricas.eventoPublicado("OS_CRIADA");
        metricas.eventoPublicado("OS_CRIADA");
        metricas.eventoPublicado("OS_CANCELADA");

        assertThat(registry.get("os.eventos.publicados").tag("tipo", "OS_CRIADA").counter().count()).isEqualTo(2);
        assertThat(registry.get("os.eventos.publicados").tag("tipo", "OS_CANCELADA").counter().count()).isEqualTo(1);
    }

    @Test
    void contaEventosConsumidosPorTipoEResultado() {
        metricas.eventoConsumido("PAGAMENTO_CONFIRMADO", MetricasDaSaga.RESULTADO_SUCESSO);
        metricas.eventoConsumido("PAGAMENTO_CONFIRMADO", MetricasDaSaga.RESULTADO_DUPLICADO);

        assertThat(registry.get("os.eventos.consumidos")
                .tags("tipo", "PAGAMENTO_CONFIRMADO", "resultado", "sucesso").counter().count()).isEqualTo(1);
        assertThat(registry.get("os.eventos.consumidos")
                .tags("tipo", "PAGAMENTO_CONFIRMADO", "resultado", "duplicado").counter().count()).isEqualTo(1);
    }

    @Test
    void gaugeDeOutboxPendentesLeOContadorDoRepositorio() {
        when(outbox.countByStatus(StatusOutbox.PENDENTE)).thenReturn(5L);

        assertThat(registry.get("os.outbox.pendentes").gauge().value()).isEqualTo(5.0);
    }

    @Test
    void contaTransicoesDeStatus() {
        metricas.transicaoDeStatus(new StatusDaOrdemDeServicoAlterado(1L, StatusOrdemDeServico.APROVADA,
                StatusOrdemDeServico.EM_EXECUCAO, 10L, Instant.parse("2026-01-01T10:00:00Z")));

        assertThat(registry.get("os.saga.transicoes")
                .tags("de", "APROVADA", "para", "EM_EXECUCAO").counter().count()).isEqualTo(1);
    }
}

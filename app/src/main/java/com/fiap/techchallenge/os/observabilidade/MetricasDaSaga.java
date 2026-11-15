package com.fiap.techchallenge.os.observabilidade;

import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import com.fiap.techchallenge.os.ordemservico.observabilidade.OrdemDeServicoEventLogger.StatusDaOrdemDeServicoAlterado;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Métricas da saga: os.eventos.publicados{tipo}, os.eventos.consumidos{tipo,resultado},
 * os.outbox.pendentes e os.saga.transicoes{de,para}.
 */
@Component
public class MetricasDaSaga {

    public static final String RESULTADO_SUCESSO = "sucesso";
    public static final String RESULTADO_DUPLICADO = "duplicado";
    public static final String RESULTADO_ERRO_NEGOCIO = "erro_negocio";
    public static final String RESULTADO_ERRO_TECNICO = "erro_tecnico";
    public static final String RESULTADO_IGNORADO = "ignorado";
    public static final String RESULTADO_ADIADO = "adiado";

    private final MeterRegistry registry;

    public MetricasDaSaga(MeterRegistry registry, OutboxEventoRepository outboxRepository) {
        this.registry = registry;
        Gauge.builder("os.outbox.pendentes", () -> outboxRepository.countByStatus(StatusOutbox.PENDENTE))
                .description("Eventos na outbox aguardando envio ao SNS")
                .register(registry);
    }

    public void eventoPublicado(String tipo) {
        registry.counter("os.eventos.publicados", "tipo", tipo).increment();
    }

    public void eventoConsumido(String tipo, String resultado) {
        registry.counter("os.eventos.consumidos", "tipo", tipo, "resultado", resultado).increment();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void transicaoDeStatus(StatusDaOrdemDeServicoAlterado evento) {
        registry.counter("os.saga.transicoes",
                "de", evento.statusAnterior().name(), "para", evento.novoStatus().name()).increment();
    }
}

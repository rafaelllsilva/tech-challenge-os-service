package com.fiap.techchallenge.os.ordemservico.observabilidade;

import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@Slf4j
public class OrdemDeServicoEventLogger {

    private final ApplicationEventPublisher eventPublisher;

    public OrdemDeServicoEventLogger(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void registrarOrdemCriada(Long idOrdemDeServico, StatusOrdemDeServico status, Instant ocorreuEm) {
        eventPublisher.publishEvent(new OrdemDeServicoCriada(idOrdemDeServico, status, ocorreuEm));
    }

    public void registrarStatusAlterado(Long idOrdemDeServico, StatusOrdemDeServico statusAnterior,
                                        StatusOrdemDeServico novoStatus, long duracaoNoStatusAnteriorEmMs,
                                        Instant ocorreuEm) {
        eventPublisher.publishEvent(new StatusDaOrdemDeServicoAlterado(
                idOrdemDeServico, statusAnterior, novoStatus, duracaoNoStatusAnteriorEmMs, ocorreuEm));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registrarLogOrdemCriada(OrdemDeServicoCriada evento) {
        log.atInfo()
                .addKeyValue("serviceOrderEventType", "service_order_created")
                .addKeyValue("orderId", evento.idOrdemDeServico())
                .addKeyValue("status", evento.status().name())
                .addKeyValue("occurredAt", evento.ocorreuEm().toString())
                .log("Ordem de serviço criada");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registrarLogStatusAlterado(StatusDaOrdemDeServicoAlterado evento) {
        log.atInfo()
                .addKeyValue("serviceOrderEventType", "service_order_status_changed")
                .addKeyValue("orderId", evento.idOrdemDeServico())
                .addKeyValue("fromStatus", evento.statusAnterior().name())
                .addKeyValue("toStatus", evento.novoStatus().name())
                .addKeyValue("durationInPreviousStatusMs", evento.duracaoNoStatusAnteriorEmMs())
                .addKeyValue("occurredAt", evento.ocorreuEm().toString())
                .log("Status da ordem de serviço alterado");
    }

    public void registrarFalhaProcessamento(Long idOrdemDeServico, String operacao, String tipoErro,
                                            Instant ocorreuEm, boolean tecnico) {
        LoggingEventBuilder evento = tecnico ? log.atError() : log.atWarn();
        evento.addKeyValue("serviceOrderEventType", "service_order_processing_failed");
        if (idOrdemDeServico != null) {
            evento.addKeyValue("orderId", idOrdemDeServico);
        }
        evento.addKeyValue("operation", operacao)
                .addKeyValue("errorType", tipoErro)
                .addKeyValue("occurredAt", ocorreuEm.toString())
                .log("Falha no processamento da ordem de serviço");
    }

    // correlationId e eventId não entram como key-value: já vão no MDC, e o JSON não aceita chave repetida.
    public void registrarEventoPublicado(String tipo, String topico) {
        log.atInfo()
                .addKeyValue("serviceOrderEventType", "saga_event_published")
                .addKeyValue("sagaEventType", tipo)
                .addKeyValue("topic", topico)
                .log("Evento da saga publicado");
    }

    public void registrarEventoConsumido(String tipo, String resultado) {
        log.atInfo()
                .addKeyValue("serviceOrderEventType", "saga_event_consumed")
                .addKeyValue("sagaEventType", tipo)
                .addKeyValue("result", resultado)
                .log("Evento da saga consumido");
    }

    public void registrarCompensacao(Long idOrdemDeServico, String motivo, StatusOrdemDeServico statusResultante,
                                     Instant ocorreuEm) {
        log.atWarn()
                .addKeyValue("serviceOrderEventType", "saga_compensation")
                .addKeyValue("orderId", idOrdemDeServico)
                .addKeyValue("reason", motivo)
                .addKeyValue("resultingStatus", statusResultante.name())
                .addKeyValue("occurredAt", ocorreuEm.toString())
                .log("Compensação da saga aplicada");
    }

    public record OrdemDeServicoCriada(Long idOrdemDeServico, StatusOrdemDeServico status, Instant ocorreuEm) {}

    public record StatusDaOrdemDeServicoAlterado(Long idOrdemDeServico, StatusOrdemDeServico statusAnterior,
                                                  StatusOrdemDeServico novoStatus, long duracaoNoStatusAnteriorEmMs,
                                                  Instant ocorreuEm) {}
}

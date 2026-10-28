package com.fiap.techchallenge.os.ordemservico.observabilidade;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class OrdemDeServicoEventLoggerTest {

    @Test
    void logDeAlteracaoDeStatusContemApenasOsCamposAprovados() {
        OrdemDeServicoEventLogger loggerEventos = new OrdemDeServicoEventLogger(mock(ApplicationEventPublisher.class));
        Logger logger = (Logger) LoggerFactory.getLogger(OrdemDeServicoEventLogger.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            loggerEventos.registrarLogStatusAlterado(new OrdemDeServicoEventLogger.StatusDaOrdemDeServicoAlterado(
                    123L,
                    StatusOrdemDeServico.EM_DIAGNOSTICO,
                    StatusOrdemDeServico.AGUARDANDO_APROVACAO,
                    3_600_000L,
                    Instant.parse("2026-08-29T12:00:00Z")));

            Map<String, Object> fields = appender.list.getFirst().getKeyValuePairs().stream()
                    .collect(java.util.stream.Collectors.toMap(pair -> pair.key, pair -> pair.value));
            assertEquals(Map.of(
                    "serviceOrderEventType", "service_order_status_changed",
                    "orderId", 123L,
                    "fromStatus", "EM_DIAGNOSTICO",
                    "toStatus", "AGUARDANDO_APROVACAO",
                    "durationInPreviousStatusMs", 3_600_000L,
                    "occurredAt", "2026-08-29T12:00:00Z"), fields);
            assertFalse(fields.containsKey("email"));
            assertFalse(fields.containsKey("cpf"));
            assertFalse(fields.containsKey("authorization"));
            assertFalse(fields.containsKey("webhookSecret"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private ListAppender<ILoggingEvent> anexar(Logger logger) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    @Test
    void logsDeEventoPublicadoConsumidoECompensacaoTrazemOsCamposDaSaga() {
        OrdemDeServicoEventLogger loggerEventos = new OrdemDeServicoEventLogger(mock(ApplicationEventPublisher.class));
        Logger logger = (Logger) LoggerFactory.getLogger(OrdemDeServicoEventLogger.class);
        ListAppender<ILoggingEvent> appender = anexar(logger);
        try {
            loggerEventos.registrarEventoPublicado("OS_CRIADA", "os-events");
            loggerEventos.registrarEventoConsumido("PAGAMENTO_CONFIRMADO", "sucesso");
            loggerEventos.registrarCompensacao(42L, "PAGAMENTO_FALHOU", StatusOrdemDeServico.AGUARDANDO_APROVACAO,
                    Instant.parse("2026-08-29T12:00:00Z"));

            var eventos = appender.list.stream().map(e -> e.getKeyValuePairs().stream()
                    .collect(java.util.stream.Collectors.toMap(pair -> pair.key, pair -> pair.value))).toList();
            assertEquals(Map.of("serviceOrderEventType", "saga_event_published", "sagaEventType", "OS_CRIADA",
                    "topic", "os-events"), eventos.get(0));
            assertEquals(Map.of("serviceOrderEventType", "saga_event_consumed", "sagaEventType", "PAGAMENTO_CONFIRMADO",
                    "result", "sucesso"), eventos.get(1));
            assertEquals(Map.of("serviceOrderEventType", "saga_compensation", "orderId", 42L,
                    "reason", "PAGAMENTO_FALHOU", "resultingStatus", "AGUARDANDO_APROVACAO",
                    "occurredAt", "2026-08-29T12:00:00Z"), eventos.get(2));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}

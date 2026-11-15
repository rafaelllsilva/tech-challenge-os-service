package com.fiap.techchallenge.os.mensageria.adapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class LogDeMensagensTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LogDeMensagens.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final LogDeMensagens logMensagens = new LogDeMensagens(JsonMapper.builder().build(), "os-service");

    @BeforeEach
    void anexar() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void desanexar() {
        logger.detachAppender(appender);
    }

    @Test
    void consumoGeraLinhaComPrefixoSqsEJsonNaOrdemEsperada() {
        logMensagens.consumida("10", "501ddf7b-228e-497c-8b4f-84124f0d35dd", "ESTOQUE_RESERVADO", "confirmado",
                "Reserva de estoque registrada no histórico da OS");

        ILoggingEvent log = appender.list.getFirst();
        assertThat(log.getLevel()).isEqualTo(Level.INFO);
        assertThat(log.getFormattedMessage()).isEqualTo("[SQS LISTENER] "
                + "{\"service\":\"os-service\",\"correlationId\":\"10\",\"orderId\":10,"
                + "\"eventId\":\"501ddf7b-228e-497c-8b4f-84124f0d35dd\",\"eventType\":\"ESTOQUE_RESERVADO\","
                + "\"operation\":\"processar_evento\",\"outcome\":\"confirmado\","
                + "\"message\":\"Reserva de estoque registrada no histórico da OS\"}");
    }

    @Test
    void publicacaoGeraLinhaComPrefixoSns() {
        logMensagens.publicada("10", "e-1", "OS_CRIADA", "publicado", "Abertura da OS publicada");

        ILoggingEvent log = appender.list.getFirst();
        assertThat(log.getLevel()).isEqualTo(Level.INFO);
        assertThat(log.getFormattedMessage())
                .startsWith("[SNS PUBLISHER] {")
                .contains("\"operation\":\"publicar_evento\"", "\"outcome\":\"publicado\"");
    }

    @Test
    void camposNulosEOrderIdNaoNumericoSaoOmitidos() {
        logMensagens.consumida(null, null, null, "descartado", "Tipo de evento desconhecido");

        ILoggingEvent log = appender.list.getFirst();
        assertThat(log.getLevel()).isEqualTo(Level.WARN);
        assertThat(log.getFormattedMessage()).isEqualTo("[SQS LISTENER] "
                + "{\"service\":\"os-service\",\"operation\":\"processar_evento\",\"outcome\":\"descartado\","
                + "\"message\":\"Tipo de evento desconhecido\"}");

        appender.list.clear();
        logMensagens.consumida("abc", "e-1", "OS_CRIADA", "rejeitado", "x");
        assertThat(appender.list.getFirst().getFormattedMessage())
                .contains("\"correlationId\":\"abc\"")
                .doesNotContain("orderId");
    }

    @Test
    void resultadosDeFalhaSaoLogadosComoWarn() {
        logMensagens.consumida("10", "e-1", "OS_CRIADA", "erro_tecnico", "banco fora do ar");
        logMensagens.publicada("10", "e-1", "OS_CRIADA", "falha", "timeout");

        assertThat(appender.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.WARN);
    }

    @Test
    void descricoesCobremTodosOsEventosEUmDesconhecidoCaiNoPadrao() {
        assertThat(LogDeMensagens.descricaoDoConsumo("PAGAMENTO_CONFIRMADO")).isEqualTo("Pagamento confirmado, OS aprovada");
        assertThat(LogDeMensagens.descricaoDoConsumo("OUTRO")).isEqualTo("Evento OUTRO processado");
        assertThat(LogDeMensagens.descricaoDaPublicacao("OS_CANCELADA")).isEqualTo("Cancelamento da OS publicado");
        assertThat(LogDeMensagens.descricaoDaPublicacao("OUTRO")).isEqualTo("Evento OUTRO publicado");
    }
}

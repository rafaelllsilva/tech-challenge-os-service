package com.fiap.techchallenge.os.mensageria.framework;

import com.fiap.techchallenge.os.mensageria.contrato.Topico;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "os.mensageria")
public record MensageriaProperties(Topicos topicos, Filas filas) {

    public record Topicos(String osEvents, String billingEvents, String executionEvents) {
    }

    public record Filas(String billing, String execution) {
    }

    /** Nome ou ARN do tópico configurado para o tópico lógico. */
    public String destino(Topico topico) {
        return switch (topico) {
            case OS_EVENTS -> topicos.osEvents();
            case BILLING_EVENTS -> topicos.billingEvents();
            case EXECUTION_EVENTS -> topicos.executionEvents();
        };
    }
}

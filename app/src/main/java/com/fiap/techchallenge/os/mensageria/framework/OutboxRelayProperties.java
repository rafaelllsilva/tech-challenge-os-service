package com.fiap.techchallenge.os.mensageria.framework;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "os.outbox.relay")
public record OutboxRelayProperties(
        @DefaultValue("true") boolean habilitado,
        @DefaultValue("1000") long intervaloMs,
        @DefaultValue("50") int tamanhoLote,
        @DefaultValue("5") int maxTentativas) {
}

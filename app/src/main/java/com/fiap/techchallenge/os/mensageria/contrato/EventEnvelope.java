package com.fiap.techchallenge.os.mensageria.contrato;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        TipoEvento type,
        int version,
        Instant occurredAt,
        String correlationId,
        T payload
) {

    public static final int VERSAO_ATUAL = 1;

    public static <T> EventEnvelope<T> novo(TipoEvento type, String correlationId, T payload, Clock clock) {
        return new EventEnvelope<>(UUID.randomUUID(), type, VERSAO_ATUAL, Instant.now(clock), correlationId, payload);
    }
}

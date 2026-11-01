package com.fiap.techchallenge.os.mensageria.contrato.payload;

import java.time.Instant;

public record ExecucaoFinalizadaPayload(String osId, Instant finalizadaEm) {
}

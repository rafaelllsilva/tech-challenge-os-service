package com.fiap.techchallenge.os.mensageria.contrato.payload;

import java.time.Instant;

public record ExecucaoIniciadaPayload(String osId, Instant iniciadaEm) {
}

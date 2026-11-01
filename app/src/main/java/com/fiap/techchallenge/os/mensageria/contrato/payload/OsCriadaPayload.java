package com.fiap.techchallenge.os.mensageria.contrato.payload;

import java.time.Instant;

public record OsCriadaPayload(String osId, String clienteId, String veiculoId, String solicitacao, Instant criadaEm) {
}

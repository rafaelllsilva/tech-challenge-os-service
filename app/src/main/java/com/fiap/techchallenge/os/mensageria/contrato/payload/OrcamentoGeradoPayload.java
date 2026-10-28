package com.fiap.techchallenge.os.mensageria.contrato.payload;

import java.math.BigDecimal;

public record OrcamentoGeradoPayload(String osId, String orcamentoId, BigDecimal valorTotal) {
}

package com.fiap.techchallenge.os.mensageria.contrato.payload;

import java.math.BigDecimal;

public record PagamentoConfirmadoPayload(String osId, String pagamentoId, BigDecimal valor) {
}

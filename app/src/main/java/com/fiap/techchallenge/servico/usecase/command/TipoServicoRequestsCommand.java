package com.fiap.techchallenge.servico.usecase.command;

import java.math.BigDecimal;

public record TipoServicoRequestsCommand(
        String getNome,
        BigDecimal getValor
) {
}

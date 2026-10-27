package com.fiap.techchallenge.os.servico.usecase.command;

import java.math.BigDecimal;

public record TipoServicoRequestsCommand(
        String getNome,
        BigDecimal getValor
) {
}

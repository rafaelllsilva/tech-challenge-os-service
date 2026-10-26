package com.fiap.techchallenge.servico.usecase.command;

public record ServicoCreateCommand(
        Long getTipoServicoId,
        Long getOrdemDeServicoId
) {
}

package com.fiap.techchallenge.os.servico.usecase.command;

public record ServicoCreateCommand(
        Long getTipoServicoId,
        Long getOrdemDeServicoId
) {
}

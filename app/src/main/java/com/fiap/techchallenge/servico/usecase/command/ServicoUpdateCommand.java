package com.fiap.techchallenge.servico.usecase.command;

public record ServicoUpdateCommand(
        Long getTipoServicoId,
        Long getOrdemDeServicoId
) {
}

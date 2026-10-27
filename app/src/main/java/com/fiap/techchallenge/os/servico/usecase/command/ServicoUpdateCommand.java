package com.fiap.techchallenge.os.servico.usecase.command;

public record ServicoUpdateCommand(
        Long getTipoServicoId,
        Long getOrdemDeServicoId
) {
}

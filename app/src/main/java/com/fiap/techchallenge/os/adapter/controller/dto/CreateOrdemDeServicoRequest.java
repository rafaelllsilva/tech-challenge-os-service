package com.fiap.techchallenge.os.adapter.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrdemDeServicoRequest {

    @NotNull(message = "Solicitação não pode ser nula!")
    private String solicitacao;

    @NotNull(message = "Id do Cliente não pode ser nulo!")
    private Long idCliente;

    @NotNull(message = "Id do Veículo não pode ser nulo!")
    private Long idVeiculo;

    private List<AdicionarPecaItemRequest> pecas;

    private List<Long> servicos;
}

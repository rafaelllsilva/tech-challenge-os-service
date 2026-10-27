package com.fiap.techchallenge.os.peca.adapter.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePecaRequest {

    @NotNull(message = "Id da Ordem de Serviço não pode ser nulo!")
    private Integer idOs;

    @NotNull(message = "Id do Tipo de Peça não pode ser nulo!")
    private Integer idTipoPeca;

    @NotNull(message = "Quantidade não pode ser nula!")
    private Integer quantidade;
}

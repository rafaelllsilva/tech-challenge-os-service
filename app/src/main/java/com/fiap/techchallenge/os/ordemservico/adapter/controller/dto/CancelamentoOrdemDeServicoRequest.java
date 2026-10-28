package com.fiap.techchallenge.os.ordemservico.adapter.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelamentoOrdemDeServicoRequest {

    @NotBlank(message = "Motivo não pode ser vazio!")
    private String motivo;
}

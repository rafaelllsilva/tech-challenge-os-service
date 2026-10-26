package com.fiap.techchallenge.peca.adapter.controller.dto;

import lombok.Data;

@Data
public class UpdatePecaRequest {
    private Integer idOs;
    private Integer idTipoPeca;
    private Integer quantidade;
}

package com.fiap.techchallenge.os.peca.adapter.controller.dto;

import lombok.Data;

@Data
public class UpdatePecaRequest {
    private Integer idOs;
    private Integer idTipoPeca;
    private Integer quantidade;
}

package com.fiap.techchallenge.os.peca.adapter.controller.dto;

import lombok.Data;

@Data
public class PecaResponse {
    private Long id;
    private Integer idOs;
    private Integer idTipoPeca;
    private String nomeTipoPeca;
    private Integer quantidade;
}

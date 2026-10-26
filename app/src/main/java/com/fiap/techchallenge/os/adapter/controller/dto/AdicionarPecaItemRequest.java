package com.fiap.techchallenge.os.adapter.controller.dto;

import lombok.Data;

@Data
public class AdicionarPecaItemRequest {
    private Long idTipoPeca;
    private int quantidade;
}

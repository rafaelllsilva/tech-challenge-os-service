package com.fiap.techchallenge.os.peca.adapter.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTipoPecaRequest {
    private String nome;
    private BigDecimal valorUnitario;
    private Integer quantidadeEstoque;
}

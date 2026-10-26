package com.fiap.techchallenge.servico.adapter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoServicoRequestDTO {

    private String nome;
    private BigDecimal valor;
}

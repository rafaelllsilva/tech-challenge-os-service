package com.fiap.techchallenge.os.adapter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServicoSimplesResponse {
    private Long id;
    private String status;
    private BigDecimal orcamento;
    private String solicitacao;
    private LocalDateTime dataHoraCriacao;
}

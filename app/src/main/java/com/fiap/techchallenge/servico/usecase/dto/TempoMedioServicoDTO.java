package com.fiap.techchallenge.servico.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TempoMedioServicoDTO {

    private String nome;

    private BigDecimal tempoMedioMinutos;
}

package com.fiap.techchallenge.veiculo.adapter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VeiculoResponse {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
}

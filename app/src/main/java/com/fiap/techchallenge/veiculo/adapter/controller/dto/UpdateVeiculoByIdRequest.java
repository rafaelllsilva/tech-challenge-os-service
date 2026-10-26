package com.fiap.techchallenge.veiculo.adapter.controller.dto;

import com.fiap.techchallenge.veiculo.adapter.controller.dto.validator.ValidAno;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.validator.ValidPlaca;
import lombok.Data;

@Data
public class UpdateVeiculoByIdRequest {
    @ValidPlaca
    String placa;
    String marca;
    String modelo;
    @ValidAno
    Integer ano;
}

package com.fiap.techchallenge.os.veiculo.adapter.controller.dto;

import com.fiap.techchallenge.os.veiculo.adapter.controller.dto.validator.ValidAno;
import lombok.Data;

@Data
public class UpdateVeiculoByPlacaRequest {
    String marca;
    String modelo;
    @ValidAno
    Integer ano;
}

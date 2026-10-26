package com.fiap.techchallenge.veiculo.adapter.controller;

import com.fiap.techchallenge.veiculo.adapter.controller.dto.VeiculoResponse;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import org.springframework.stereotype.Component;

@Component
public class VeiculoWebMapper {

    public VeiculoResponse toResponse(Veiculo veiculo) {
        return VeiculoResponse.builder()
                .id(veiculo.getId())
                .placa(veiculo.getPlaca())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .build();
    }
}

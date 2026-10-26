package com.fiap.techchallenge.veiculo.adapter.controller.dto;

import com.fiap.techchallenge.veiculo.adapter.controller.dto.validator.ValidAno;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.validator.ValidPlaca;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVeiculoRequest {
    @ValidPlaca
    @NotEmpty(message = "Placa não pode ser nulo")
    String placa;
    @NotEmpty(message = "Marca não pode ser nulo")
    String marca;
    @NotEmpty(message = "Modelo não pode ser nulo")
    String modelo;
    @NotNull(message = "Ano não pode ser nulo")
    @ValidAno
    Integer ano;
}

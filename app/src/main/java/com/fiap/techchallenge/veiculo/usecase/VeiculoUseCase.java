package com.fiap.techchallenge.veiculo.usecase;

import com.fiap.techchallenge.veiculo.domain.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoUseCase {

    Veiculo create(String placa, String marca, String modelo, Integer ano);

    List<Veiculo> getAll();

    Optional<Veiculo> getById(Long id);

    Optional<Veiculo> getByPlaca(String placa);

    Veiculo updateById(Long id, String placa, String marca, String modelo, Integer ano);

    Veiculo updateByPlaca(String placa, String marca, String modelo, Integer ano);

    Veiculo deleteById(Long id);

    Veiculo deleteByPlaca(String placa);
}

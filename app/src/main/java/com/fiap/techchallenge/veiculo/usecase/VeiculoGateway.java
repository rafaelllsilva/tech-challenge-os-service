package com.fiap.techchallenge.veiculo.usecase;

import com.fiap.techchallenge.veiculo.domain.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoGateway {

    Veiculo save(Veiculo veiculo);

    List<Veiculo> findAll();

    Optional<Veiculo> findById(Long id);

    Optional<Veiculo> findByPlaca(String placa);

    void delete(Long id);
}

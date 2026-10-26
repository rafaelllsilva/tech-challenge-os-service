package com.fiap.techchallenge.veiculo.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VeiculoInteractor implements VeiculoUseCase {

    private final VeiculoGateway gateway;

    public VeiculoInteractor(VeiculoGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Veiculo create(String placa, String marca, String modelo, Integer ano) {
        Veiculo veiculo = new Veiculo(null, placa, marca, modelo, ano);
        return gateway.save(veiculo);
    }

    @Override
    public List<Veiculo> getAll() {
        return gateway.findAll();
    }

    @Override
    public Optional<Veiculo> getById(Long id) {
        return gateway.findById(id);
    }

    @Override
    public Optional<Veiculo> getByPlaca(String placa) {
        return gateway.findByPlaca(placa);
    }

    @Override
    public Veiculo updateById(Long id, String placa, String marca, String modelo, Integer ano) {
        Veiculo veiculo = gateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Veiculo com id {} não encontrado", id);
                    return new NotFoundException("Veiculo com id " + id + " não encontrado");
                });
        if (placa != null) veiculo.setPlaca(placa);
        if (marca != null) veiculo.setMarca(marca);
        if (modelo != null) veiculo.setModelo(modelo);
        if (ano != null) veiculo.setAno(ano);
        return gateway.save(veiculo);
    }

    @Override
    public Veiculo updateByPlaca(String placa, String marca, String modelo, Integer ano) {
        Veiculo veiculo = gateway.findByPlaca(placa)
                .orElseThrow(() -> {
                    log.warn("Veiculo com placa {} não encontrado", placa);
                    return new NotFoundException("Veiculo com placa " + placa + " não encontrado");
                });
        if (marca != null) veiculo.setMarca(marca);
        if (modelo != null) veiculo.setModelo(modelo);
        if (ano != null) veiculo.setAno(ano);
        return gateway.save(veiculo);
    }

    @Override
    @Transactional
    public Veiculo deleteById(Long id) {
        Veiculo veiculo = gateway.findById(id)
                .orElseThrow(() -> {
                    log.warn("Veiculo com id {} não encontrado", id);
                    return new NotFoundException("Veiculo com id " + id + " não encontrado");
                });
        gateway.delete(id);
        return veiculo;
    }

    @Override
    @Transactional
    public Veiculo deleteByPlaca(String placa) {
        Veiculo veiculo = gateway.findByPlaca(placa)
                .orElseThrow(() -> {
                    log.warn("Veiculo com placa {} não encontrado", placa);
                    return new NotFoundException("Veiculo com placa " + placa + " não encontrado");
                });
        gateway.delete(veiculo.getId());
        return veiculo;
    }
}

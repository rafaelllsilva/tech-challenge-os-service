package com.fiap.techchallenge.veiculo.adapter.gateway;

import com.fiap.techchallenge.exception.ConflictException;
import com.fiap.techchallenge.veiculo.framework.persistence.VeiculoEntity;
import com.fiap.techchallenge.veiculo.framework.persistence.VeiculoJpaRepository;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import com.fiap.techchallenge.veiculo.usecase.VeiculoGateway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VeiculoGatewayImpl implements VeiculoGateway {

    private final VeiculoJpaRepository jpaRepository;

    public VeiculoGatewayImpl(VeiculoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Veiculo save(Veiculo veiculo) {
        VeiculoEntity entity = toEntity(veiculo);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Veiculo> findAll() {
        return jpaRepository.findAll().stream().map(VeiculoGatewayImpl::toDomain).toList();
    }

    @Override
    public Optional<Veiculo> findById(Long id) {
        return jpaRepository.findById(id).map(VeiculoGatewayImpl::toDomain);
    }

    @Override
    public Optional<Veiculo> findByPlaca(String placa) {
        return jpaRepository.findByPlaca(placa).map(VeiculoGatewayImpl::toDomain);
    }

    @Override
    public void delete(Long id) {
        VeiculoEntity entity = jpaRepository.findById(id).orElseThrow();
        try {
            jpaRepository.delete(entity);
            jpaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Esse veiculo tem dependências ativas e não pode ser deletado.");
        }
    }

    public static Veiculo toDomain(VeiculoEntity entity) {
        return new Veiculo(entity.getId(), entity.getPlaca(), entity.getMarca(), entity.getModelo(), entity.getAno());
    }

    public static VeiculoEntity toEntity(Veiculo veiculo) {
        return new VeiculoEntity(veiculo.getId(), veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(), veiculo.getAno());
    }
}

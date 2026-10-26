package com.fiap.techchallenge.peca.adapter.gateway;

import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.repository.TipoPecaRepository;
import com.fiap.techchallenge.peca.usecase.TipoPecaGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TipoPecaGatewayImpl implements TipoPecaGateway {

    private final TipoPecaRepository repository;

    public TipoPecaGatewayImpl(TipoPecaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TipoPeca> findAll() {
        return repository.findAll().stream().map(TipoPecaGatewayImpl::toDomain).toList();
    }

    @Override
    public Optional<TipoPeca> findById(Long id) {
        return repository.findById(id).map(TipoPecaGatewayImpl::toDomain);
    }

    @Override
    public TipoPeca save(TipoPeca tipoPeca) {
        TipoPecaEntity entity = toEntity(tipoPeca);
        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(TipoPeca tipoPeca) {
        repository.delete(toEntity(tipoPeca));
    }

    @Override
    public List<TipoPeca> saveAll(List<TipoPeca> tipoPecas) {
        List<TipoPecaEntity> entities = tipoPecas.stream().map(TipoPecaGatewayImpl::toEntity).toList();
        return repository.saveAll(entities).stream().map(TipoPecaGatewayImpl::toDomain).toList();
    }

    @Override
    public List<TipoPeca> findAllByIds(List<Long> ids) {
        List<TipoPecaEntity> tipoPecaEntities = repository.findAllById(ids);
        return !tipoPecaEntities.isEmpty() ? tipoPecaEntities.stream().map(TipoPecaGatewayImpl::toDomain).toList() : List.of();
    }

    public static TipoPeca toDomain(TipoPecaEntity entity) {
        TipoPeca domain = new TipoPeca();
        domain.setId(entity.getId());
        domain.setNome(entity.getNome());
        domain.setValorUnitario(entity.getValorUnitario());
        domain.setQuantidadeEstoque(entity.getQuantidadeEstoque());
        return domain;
    }

    public static TipoPecaEntity toEntity(TipoPeca domain) {
        TipoPecaEntity entity = new TipoPecaEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setValorUnitario(domain.getValorUnitario());
        entity.setQuantidadeEstoque(domain.getQuantidadeEstoque());
        return entity;
    }
}

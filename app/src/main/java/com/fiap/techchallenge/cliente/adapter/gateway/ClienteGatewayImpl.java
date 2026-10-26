package com.fiap.techchallenge.cliente.adapter.gateway;

import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import com.fiap.techchallenge.cliente.framework.persistence.ClienteEntity;
import com.fiap.techchallenge.cliente.framework.persistence.ClienteJpaRepository;
import com.fiap.techchallenge.cliente.usecase.ClienteGateway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClienteGatewayImpl implements ClienteGateway {

    private final ClienteJpaRepository jpaRepository;

    public ClienteGatewayImpl(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteEntity entity = toEntity(cliente);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll().stream().map(ClienteGatewayImpl::toDomain).toList();
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return jpaRepository.findById(id).map(ClienteGatewayImpl::toDomain);
    }

    @Override
    public Optional<Cliente> findByDocumento(String documento) {
        return jpaRepository.findByDocumento(documento).map(ClienteGatewayImpl::toDomain);
    }

    @Override
    public void delete(Long id) {
        ClienteEntity entity = jpaRepository.findById(id).orElseThrow();
        jpaRepository.delete(entity);
        jpaRepository.flush();
    }

    public static Cliente toDomain(ClienteEntity entity) {
        return new Cliente(
                entity.getId(),
                entity.getNome(),
                TipoPessoa.valueOf(entity.getTipoPessoa().name()),
                entity.getDocumento(),
                entity.getDataNascimento(),
                entity.getEmail());
    }

    public static ClienteEntity toEntity(Cliente cliente) {
        return new ClienteEntity(
                cliente.getId(),
                cliente.getNome(),
                com.fiap.techchallenge.cliente.framework.persistence.ClienteEntity.TipoPessoaEntity
                        .valueOf(cliente.getTipoPessoa().name()),
                cliente.getDocumento(),
                cliente.getDataNascimento(),
                cliente.getEmail());
    }
}

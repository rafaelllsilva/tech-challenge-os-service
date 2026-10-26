package com.fiap.techchallenge.usuario.adapter.gateway;

import com.fiap.techchallenge.cliente.adapter.gateway.ClienteGatewayImpl;
import com.fiap.techchallenge.usuario.domain.Usuario;
import com.fiap.techchallenge.usuario.framework.persistence.UsuarioEntity;
import com.fiap.techchallenge.usuario.framework.persistence.UsuarioRepository;
import com.fiap.techchallenge.usuario.usecase.UsuarioGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UsuarioGatewayImpl implements UsuarioGateway {

    private final UsuarioRepository repository;

    public UsuarioGatewayImpl(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByLogin(String login) {
        return repository.findByLogin(login).map(UsuarioGatewayImpl::toDomain);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = toEntity(usuario);
        entity = repository.save(entity);
        return toDomain(entity);
    }

    @Override
    public boolean existsByLogin(String login) {
        return repository.existsByLogin(login);
    }

    @Override
    public void deleteByClienteId(Long clienteId) {
        repository.deleteByClienteId(clienteId);
    }

    public static Usuario toDomain(UsuarioEntity entity) {
        if (entity == null)
            return null;
        return new Usuario(
                entity.getId(),
                entity.getLogin(),
                entity.getSenhaHash(),
                entity.getRole(),
                entity.getCliente() != null ? ClienteGatewayImpl.toDomain(entity.getCliente()) : null);
    }

    public static UsuarioEntity toEntity(Usuario domain) {
        if (domain == null)
            return null;
        UsuarioEntity entity = new UsuarioEntity(
                domain.getLogin(),
                domain.getSenhaHash(),
                domain.getRole(),
                domain.getCliente() != null ? ClienteGatewayImpl.toEntity(domain.getCliente()) : null);
        entity.setId(domain.getId());
        return entity;
    }
}

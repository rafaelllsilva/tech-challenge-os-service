package com.fiap.techchallenge.usuario.usecase;

import com.fiap.techchallenge.usuario.domain.Usuario;
import java.util.Optional;

public interface UsuarioGateway {

    Optional<Usuario> findByLogin(String login);

    Usuario save(Usuario usuario);
    
    boolean existsByLogin(String login);
    
    void deleteByClienteId(Long clienteId);
}

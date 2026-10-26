package com.fiap.techchallenge.usuario.framework.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByLogin(String login);
    
    boolean existsByLogin(String login);
    
    void deleteByClienteId(Long clienteId);

}

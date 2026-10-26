package com.fiap.techchallenge.cliente.usecase;

import com.fiap.techchallenge.cliente.domain.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteGateway {

    Cliente save(Cliente cliente);

    List<Cliente> findAll();

    Optional<Cliente> findById(Long id);

    Optional<Cliente> findByDocumento(String documento);

    void delete(Long id);
}

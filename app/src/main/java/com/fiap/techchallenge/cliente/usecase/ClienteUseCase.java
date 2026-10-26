package com.fiap.techchallenge.cliente.usecase;

import com.fiap.techchallenge.cliente.domain.Cliente;
import com.fiap.techchallenge.cliente.domain.TipoPessoa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteUseCase {

    Cliente create(String nome, TipoPessoa tipoPessoa, String documento, LocalDate dataNascimento, String email);

    List<Cliente> getAll();

    Optional<Cliente> getById(Long id);

    Optional<Cliente> getByDocumento(String documento);

    Cliente updateById(Long id, String nome, TipoPessoa tipoPessoa, String documento, LocalDate dataNascimento, String email);

    Cliente deleteById(Long id);
}

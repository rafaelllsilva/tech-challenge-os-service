package com.fiap.techchallenge.cliente.adapter.controller.dto;

import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateClienteRequest {
    @NotEmpty(message = "Nome não pode ser vazio")
    String nome;

    @NotNull(message = "Tipo pessoa não pode ser nulo")
    TipoPessoa tipoPessoa;

    @NotEmpty(message = "Documento não pode ser vazio")
    String documento;

    LocalDate dataNascimento;

    @Email(message = "E-mail inválido")
    String email;
}

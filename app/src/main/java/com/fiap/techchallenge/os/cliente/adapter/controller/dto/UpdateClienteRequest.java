package com.fiap.techchallenge.os.cliente.adapter.controller.dto;

import com.fiap.techchallenge.os.cliente.domain.TipoPessoa;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateClienteRequest {
    String nome;
    TipoPessoa tipoPessoa;
    String documento;
    LocalDate dataNascimento;

    @Email(message = "E-mail inválido")
    String email;
}

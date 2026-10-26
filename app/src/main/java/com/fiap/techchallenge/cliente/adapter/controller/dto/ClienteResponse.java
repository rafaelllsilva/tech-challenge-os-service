package com.fiap.techchallenge.cliente.adapter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClienteResponse {
    private Long id;
    private String nome;
    private String tipoPessoa;
    private String documento;
    private LocalDate dataNascimento;
    private String email;
}

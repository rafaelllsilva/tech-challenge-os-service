package com.fiap.techchallenge.cliente.adapter.controller;

import com.fiap.techchallenge.cliente.adapter.controller.dto.ClienteResponse;
import com.fiap.techchallenge.cliente.domain.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .tipoPessoa(cliente.getTipoPessoa() != null ? cliente.getTipoPessoa().name() : null)
                .documento(cliente.getDocumento())
                .dataNascimento(cliente.getDataNascimento())
                .email(cliente.getEmail())
                .build();
    }
}

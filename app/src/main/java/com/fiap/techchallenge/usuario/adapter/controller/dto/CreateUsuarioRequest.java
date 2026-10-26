package com.fiap.techchallenge.usuario.adapter.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUsuarioRequest {

    @NotBlank(message = "Login é obrigatório")
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @NotBlank(message = "Role é obrigatória")
    private String role;

    private Long clienteId;
}

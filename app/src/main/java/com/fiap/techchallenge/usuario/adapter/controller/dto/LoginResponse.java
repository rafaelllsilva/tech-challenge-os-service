package com.fiap.techchallenge.usuario.adapter.controller.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String accessToken;
    private String role;
    private long expiresIn;

    public LoginResponse(String accessToken, String role, long expiresIn) {
        this.accessToken = accessToken;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}

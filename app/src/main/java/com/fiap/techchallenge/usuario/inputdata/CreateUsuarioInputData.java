package com.fiap.techchallenge.usuario.inputdata;

public class CreateUsuarioInputData {

    private String login;
    private String senha;
    private String role;
    private Long clienteId;

    public CreateUsuarioInputData(String login, String senha, String role, Long clienteId) {
        this.login = login;
        this.senha = senha;
        this.role = role;
        this.clienteId = clienteId;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }

    public String getRole() {
        return role;
    }

    public Long getClienteId() {
        return clienteId;
    }
}

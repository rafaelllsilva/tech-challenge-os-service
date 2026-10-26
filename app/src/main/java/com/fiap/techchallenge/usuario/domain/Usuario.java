package com.fiap.techchallenge.usuario.domain;

import com.fiap.techchallenge.cliente.domain.Cliente;

public class Usuario {

    private Long id;
    private String login;
    private String senhaHash;
    private Role role;
    private Cliente cliente;

    public Usuario() {
    }

    public Usuario(Long id, String login, String senhaHash, Role role, Cliente cliente) {
        this.id = id;
        this.login = login;
        this.senhaHash = senhaHash;
        this.role = role;
        this.cliente = cliente;
    }

    public Usuario(String login, String senhaHash, Role role) {
        this.login = login;
        this.senhaHash = senhaHash;
        this.role = role;
    }

    public Usuario(String login, String senhaHash, Role role, Cliente cliente) {
        this.login = login;
        this.senhaHash = senhaHash;
        this.role = role;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}

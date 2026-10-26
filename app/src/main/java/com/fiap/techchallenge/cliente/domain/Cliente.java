package com.fiap.techchallenge.cliente.domain;

import java.time.LocalDate;

public class Cliente {

    private Long id;
    private String nome;
    private TipoPessoa tipoPessoa;
    private String documento;
    private LocalDate dataNascimento;
    private String email;

    public Cliente() {
    }

    public Cliente(Long id, String nome, TipoPessoa tipoPessoa, String documento, LocalDate dataNascimento, String email) {
        this.id = id;
        this.nome = nome;
        this.tipoPessoa = tipoPessoa;
        this.documento = documento;
        this.dataNascimento = dataNascimento;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(TipoPessoa tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

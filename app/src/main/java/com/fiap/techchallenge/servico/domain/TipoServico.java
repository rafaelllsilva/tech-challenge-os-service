package com.fiap.techchallenge.servico.domain;

import java.math.BigDecimal;

public class TipoServico {

    private Long id;
    private String nome;
    private BigDecimal valor;

    public TipoServico(Long id, String nome, BigDecimal valor) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
    }

    public TipoServico(){}
    public static TipoServico criar(String nome, BigDecimal valor) {
        return new TipoServico(null, nome, valor);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getValor() { return valor; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public void atualizar(String nome, BigDecimal valor) {
        if (nome != null && nome.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }

        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }

        if (nome != null) {
            this.nome = nome;
        }

        if (valor != null) {
            this.valor = valor;
        }
    }
}

package com.fiap.techchallenge.os.mensageria.contrato;

public enum Topico {
    OS_EVENTS("os-events"),
    BILLING_EVENTS("billing-events"),
    EXECUTION_EVENTS("execution-events");

    private final String nome;

    Topico(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}

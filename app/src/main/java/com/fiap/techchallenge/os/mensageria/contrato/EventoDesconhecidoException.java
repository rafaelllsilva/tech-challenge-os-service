package com.fiap.techchallenge.os.mensageria.contrato;

public class EventoDesconhecidoException extends RuntimeException {

    public EventoDesconhecidoException(String tipo) {
        super("Tipo de evento desconhecido: " + tipo);
    }
}

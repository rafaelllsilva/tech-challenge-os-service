package com.fiap.techchallenge.os.mensageria.adapter;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

/** Consome as filas do billing e do execution (raw message delivery: o corpo é o envelope). */
@Component
public class ConsumidorDeEventosSqs {

    private final DespachanteDeEventos despachante;

    public ConsumidorDeEventosSqs(DespachanteDeEventos despachante) {
        this.despachante = despachante;
    }

    @SqsListener({"${os.mensageria.filas.billing}", "${os.mensageria.filas.execution}"})
    public void consumir(String mensagem) {
        despachante.despachar(mensagem);
    }
}

package com.fiap.techchallenge.os.mensageria.usecase;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;

public interface ManipuladorDeEvento<T> {

    TipoEvento tipo();

    void tratar(EventEnvelope<T> evento);
}

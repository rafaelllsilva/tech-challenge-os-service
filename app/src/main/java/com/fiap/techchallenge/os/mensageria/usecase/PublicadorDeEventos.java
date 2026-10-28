package com.fiap.techchallenge.os.mensageria.usecase;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;

public interface PublicadorDeEventos {

    void publicar(EventEnvelope<?> evento);
}

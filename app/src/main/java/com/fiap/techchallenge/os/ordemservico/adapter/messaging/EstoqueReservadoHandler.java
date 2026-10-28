package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.EstoqueReservadoPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public class EstoqueReservadoHandler implements ManipuladorDeEvento<EstoqueReservadoPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public EstoqueReservadoHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.ESTOQUE_RESERVADO;
    }

    @Override
    public void tratar(EventEnvelope<EstoqueReservadoPayload> evento) {
        osUseCase.registrarEventoInformativo(Long.parseLong(evento.payload().osId()), evento.type().name());
    }
}

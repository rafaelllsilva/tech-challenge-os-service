package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.PagamentoEstornadoPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;

import org.springframework.stereotype.Component;

@Component
public class PagamentoEstornadoHandler implements ManipuladorDeEvento<PagamentoEstornadoPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public PagamentoEstornadoHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.PAGAMENTO_ESTORNADO;
    }

    @Override
    public void tratar(EventEnvelope<PagamentoEstornadoPayload> evento) {
        osUseCase.aplicarPagamentoEstornado(Long.parseLong(evento.payload().osId()));
    }
}

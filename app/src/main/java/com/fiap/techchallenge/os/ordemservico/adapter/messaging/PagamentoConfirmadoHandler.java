package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.PagamentoConfirmadoPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;

import org.springframework.stereotype.Component;

@Component
public class PagamentoConfirmadoHandler implements ManipuladorDeEvento<PagamentoConfirmadoPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public PagamentoConfirmadoHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.PAGAMENTO_CONFIRMADO;
    }

    @Override
    public void tratar(EventEnvelope<PagamentoConfirmadoPayload> evento) {
        osUseCase.aplicarPagamentoConfirmado(Long.parseLong(evento.payload().osId()));
    }
}

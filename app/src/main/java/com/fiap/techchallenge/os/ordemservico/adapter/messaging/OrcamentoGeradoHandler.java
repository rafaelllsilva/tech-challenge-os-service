package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OrcamentoGeradoPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;

import org.springframework.stereotype.Component;

@Component
public class OrcamentoGeradoHandler implements ManipuladorDeEvento<OrcamentoGeradoPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public OrcamentoGeradoHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.ORCAMENTO_GERADO;
    }

    @Override
    public void tratar(EventEnvelope<OrcamentoGeradoPayload> evento) {
        osUseCase.aplicarOrcamentoGerado(Long.parseLong(evento.payload().osId()), evento.payload().orcamentoId(), evento.payload().valorTotal());
    }
}

package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OrcamentoRejeitadoPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;

import org.springframework.stereotype.Component;

@Component
public class OrcamentoRejeitadoHandler implements ManipuladorDeEvento<OrcamentoRejeitadoPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public OrcamentoRejeitadoHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.ORCAMENTO_REJEITADO;
    }

    @Override
    public void tratar(EventEnvelope<OrcamentoRejeitadoPayload> evento) {
        osUseCase.aplicarOrcamentoRejeitadoPeloBilling(Long.parseLong(evento.payload().osId()));
    }
}

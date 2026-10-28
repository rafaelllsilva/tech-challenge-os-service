package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.ExecucaoFalhouPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public class ExecucaoFalhouHandler implements ManipuladorDeEvento<ExecucaoFalhouPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public ExecucaoFalhouHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.EXECUCAO_FALHOU;
    }

    @Override
    public void tratar(EventEnvelope<ExecucaoFalhouPayload> evento) {
        osUseCase.aplicarExecucaoFalhou(Long.parseLong(evento.payload().osId()));
    }
}

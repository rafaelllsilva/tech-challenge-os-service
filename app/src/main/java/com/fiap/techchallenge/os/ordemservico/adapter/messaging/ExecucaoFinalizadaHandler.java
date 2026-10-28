package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.ExecucaoFinalizadaPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public class ExecucaoFinalizadaHandler implements ManipuladorDeEvento<ExecucaoFinalizadaPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public ExecucaoFinalizadaHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.EXECUCAO_FINALIZADA;
    }

    @Override
    public void tratar(EventEnvelope<ExecucaoFinalizadaPayload> evento) {
        osUseCase.aplicarExecucaoFinalizada(Long.parseLong(evento.payload().osId()));
    }
}

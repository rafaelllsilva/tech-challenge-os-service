package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.ExecucaoIniciadaPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public class ExecucaoIniciadaHandler implements ManipuladorDeEvento<ExecucaoIniciadaPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public ExecucaoIniciadaHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.EXECUCAO_INICIADA;
    }

    @Override
    public void tratar(EventEnvelope<ExecucaoIniciadaPayload> evento) {
        osUseCase.aplicarExecucaoIniciada(Long.parseLong(evento.payload().osId()));
    }
}

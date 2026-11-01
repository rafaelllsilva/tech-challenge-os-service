package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.DiagnosticoConcluidoPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticoConcluidoHandler implements ManipuladorDeEvento<DiagnosticoConcluidoPayload> {

    private final OrdemDeServicoUseCase osUseCase;

    public DiagnosticoConcluidoHandler(OrdemDeServicoUseCase osUseCase) {
        this.osUseCase = osUseCase;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.DIAGNOSTICO_CONCLUIDO;
    }

    @Override
    public void tratar(EventEnvelope<DiagnosticoConcluidoPayload> evento) {
        osUseCase.registrarEventoInformativo(Long.parseLong(evento.payload().osId()), evento.type().name());
    }
}

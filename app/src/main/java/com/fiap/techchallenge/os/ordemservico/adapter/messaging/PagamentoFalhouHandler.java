package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.PagamentoFalhouPayload;
import com.fiap.techchallenge.os.mensageria.usecase.ManipuladorDeEvento;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PagamentoFalhouHandler implements ManipuladorDeEvento<PagamentoFalhouPayload> {

    private final OrdemDeServicoUseCase osUseCase;
    private final int maxTentativas;

    public PagamentoFalhouHandler(OrdemDeServicoUseCase osUseCase,
                                  @Value("${os.saga.pagamento.max-tentativas:3}") int maxTentativas) {
        this.osUseCase = osUseCase;
        this.maxTentativas = maxTentativas;
    }

    @Override
    public TipoEvento tipo() {
        return TipoEvento.PAGAMENTO_FALHOU;
    }

    @Override
    public void tratar(EventEnvelope<PagamentoFalhouPayload> evento) {
        osUseCase.aplicarPagamentoFalhou(Long.parseLong(evento.payload().osId()), maxTentativas);
    }
}

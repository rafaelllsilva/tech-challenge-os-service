package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.*;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HandlersDoBillingTest {

    private final OrdemDeServicoUseCase useCase = mock(OrdemDeServicoUseCase.class);

    private <T> EventEnvelope<T> envelope(TipoEvento tipo, T payload) {
        return EventEnvelope.novo(tipo, "42", payload, Clock.systemUTC());
    }

    @Test
    void orcamentoGeradoDelegaParaOUseCase() {
        var handler = new OrcamentoGeradoHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.ORCAMENTO_GERADO);

        handler.tratar(envelope(TipoEvento.ORCAMENTO_GERADO, new OrcamentoGeradoPayload("42", "orc-1", new BigDecimal("10.5"))));

        verify(useCase).aplicarOrcamentoGerado(42L, "orc-1", new BigDecimal("10.5"));
    }

    @Test
    void orcamentoRejeitadoDelegaParaOUseCase() {
        var handler = new OrcamentoRejeitadoHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.ORCAMENTO_REJEITADO);

        handler.tratar(envelope(TipoEvento.ORCAMENTO_REJEITADO, new OrcamentoRejeitadoPayload("42", "orc-1", "expirado")));

        verify(useCase).aplicarOrcamentoRejeitadoPeloBilling(42L);
    }

    @Test
    void pagamentoConfirmadoDelegaParaOUseCase() {
        var handler = new PagamentoConfirmadoHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.PAGAMENTO_CONFIRMADO);

        handler.tratar(envelope(TipoEvento.PAGAMENTO_CONFIRMADO, new PagamentoConfirmadoPayload("42", "pag-1", BigDecimal.TEN)));

        verify(useCase).aplicarPagamentoConfirmado(42L);
    }

    @Test
    void pagamentoFalhouDelegaComOLimiteConfigurado() {
        var handler = new PagamentoFalhouHandler(useCase, 5);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.PAGAMENTO_FALHOU);

        handler.tratar(envelope(TipoEvento.PAGAMENTO_FALHOU, new PagamentoFalhouPayload("42", "pag-1", "recusado")));

        verify(useCase).aplicarPagamentoFalhou(42L, 5);
    }

    @Test
    void pagamentoEstornadoDelegaParaOUseCase() {
        var handler = new PagamentoEstornadoHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.PAGAMENTO_ESTORNADO);

        handler.tratar(envelope(TipoEvento.PAGAMENTO_ESTORNADO, new PagamentoEstornadoPayload("42", "pag-1")));

        verify(useCase).aplicarPagamentoEstornado(42L);
    }
}

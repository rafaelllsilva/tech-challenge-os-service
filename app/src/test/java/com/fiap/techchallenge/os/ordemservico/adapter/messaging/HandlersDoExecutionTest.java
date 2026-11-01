package com.fiap.techchallenge.os.ordemservico.adapter.messaging;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.*;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HandlersDoExecutionTest {

    private final OrdemDeServicoUseCase useCase = mock(OrdemDeServicoUseCase.class);

    private <T> EventEnvelope<T> envelope(TipoEvento tipo, T payload) {
        return EventEnvelope.novo(tipo, "42", payload, Clock.systemUTC());
    }

    @Test
    void execucaoIniciadaDelegaParaOUseCase() {
        var handler = new ExecucaoIniciadaHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.EXECUCAO_INICIADA);
        handler.tratar(envelope(TipoEvento.EXECUCAO_INICIADA, new ExecucaoIniciadaPayload("42", Instant.now())));
        verify(useCase).aplicarExecucaoIniciada(42L);
    }

    @Test
    void execucaoFinalizadaDelegaParaOUseCase() {
        var handler = new ExecucaoFinalizadaHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.EXECUCAO_FINALIZADA);
        handler.tratar(envelope(TipoEvento.EXECUCAO_FINALIZADA, new ExecucaoFinalizadaPayload("42", Instant.now())));
        verify(useCase).aplicarExecucaoFinalizada(42L);
    }

    @Test
    void execucaoFalhouDelegaParaOUseCase() {
        var handler = new ExecucaoFalhouHandler(useCase);
        assertThat(handler.tipo()).isEqualTo(TipoEvento.EXECUCAO_FALHOU);
        handler.tratar(envelope(TipoEvento.EXECUCAO_FALHOU, new ExecucaoFalhouPayload("42", "sem estoque")));
        verify(useCase).aplicarExecucaoFalhou(42L);
    }

    @Test
    void diagnosticoConcluidoEEstoqueReservadoSoRegistram() {
        var diagnostico = new DiagnosticoConcluidoHandler(useCase);
        var estoque = new EstoqueReservadoHandler(useCase);
        assertThat(diagnostico.tipo()).isEqualTo(TipoEvento.DIAGNOSTICO_CONCLUIDO);
        assertThat(estoque.tipo()).isEqualTo(TipoEvento.ESTOQUE_RESERVADO);

        diagnostico.tratar(envelope(TipoEvento.DIAGNOSTICO_CONCLUIDO, new DiagnosticoConcluidoPayload("42")));
        estoque.tratar(envelope(TipoEvento.ESTOQUE_RESERVADO, new EstoqueReservadoPayload("42")));

        verify(useCase).registrarEventoInformativo(42L, "DIAGNOSTICO_CONCLUIDO");
        verify(useCase).registrarEventoInformativo(42L, "ESTOQUE_RESERVADO");
    }
}

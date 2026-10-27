package com.fiap.techchallenge.os.ordemservico.domain;

import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class OrdemDeServicoTest {

    @Test
    public void given_orcamentoPositivo_when_verificaOrcamento_then_retornaVerdadeiro() {
        OrdemDeServico os = new OrdemDeServico();
        os.setOrcamento(BigDecimal.valueOf(100));

        assertTrue(os.orcamentoMaiorQueZero());
    }

    @Test
    public void given_orcamentoZero_when_verificaOrcamento_then_retornaFalso() {
        OrdemDeServico os = new OrdemDeServico();
        os.setOrcamento(BigDecimal.ZERO);

        assertFalse(os.orcamentoMaiorQueZero());
    }

    @Test
    public void given_orcamentoNulo_when_verificaOrcamento_then_retornaFalso() {
        OrdemDeServico os = new OrdemDeServico();
        os.setOrcamento(null);

        assertFalse(os.orcamentoMaiorQueZero());
    }

    // podeTransicionarParaStatus() - Transições válidas

    @Test
    public void given_statusRecebida_when_transicionaParaDiagnostico_then_transicaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.RECEBIDA);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);

        assertTrue(validacao.permitida());
    }

    @Test
    public void given_statusEmDiagnosticoComOrcamento_when_transicionaParaAguardandoAprovacao_then_transicaoPermitida() {
        var os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setOrcamento(new BigDecimal("300.00"));

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.AGUARDANDO_APROVACAO);

        assertTrue(validacao.permitida());
    }

    @Test
    public void given_statusAguardandoAprovacao_when_transicionaParaAprovado_then_transicaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.AGUARDANDO_APROVACAO);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.APROVADA);

        assertTrue(validacao.permitida());
    }

    @Test
    public void given_statusAprovado_when_transicionaParaEmExecucao_then_transicaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.APROVADA);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.EM_EXECUCAO);

        assertTrue(validacao.permitida());
    }

    @Test
    public void given_statusEmExecucao_when_transicionaParaFinalizada_then_transicaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_EXECUCAO);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.FINALIZADA);

        assertTrue(validacao.permitida());
    }

    @Test
    public void given_statusFinalizada_when_transicionaParaEntregue_then_transicaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.FINALIZADA);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.ENTREGUE);

        assertTrue(validacao.permitida());
    }

    // podeTransicionarParaStatus() - Transições inválidas

    @Test
    public void given_statusEmDiagnostico_when_transicionaParaDiagnostico_then_transicaoNaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);

        assertFalse(validacao.permitida());
    }

    @Test
    public void given_statusEmDiagnosticoSemOrcamento_when_transicionaParaAguardandoAprovacao_then_transicaoNaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setOrcamento(BigDecimal.ZERO);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.AGUARDANDO_APROVACAO);

        assertFalse(validacao.permitida());
    }

    @Test
    public void given_statusRecebida_when_transicionaParaAprovado_then_transicaoNaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.RECEBIDA);

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.APROVADA);

        assertFalse(validacao.permitida());
    }
}

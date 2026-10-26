package com.fiap.techchallenge.entity.os;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.domain.TipoServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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
    public void given_statusEmDiagnosticoComOrcamentoPecasEServicos_when_transicionaParaAguardandoAprovacao_then_transicaoPermitida() {
        var os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);

        var tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));
        var servico = new Servico(1L, tipoServico, os, ServicoStatus.AGUARDANDO_INICIO, null, null);

        var tipoPeca = new TipoPeca();
        tipoPeca.setId(1L);
        tipoPeca.setNome("Óleo de motor");
        tipoPeca.setValorUnitario(BigDecimal.valueOf(120L, 2));
        tipoPeca.setQuantidadeEstoque(20);

        var peca = new Peca();
        peca.setId(1L);
        peca.setTipoPeca(tipoPeca);
        peca.setOrdemDeServico(os);
        peca.setQuantidade(1);

        BigDecimal orcamento = tipoServico.getValor().add(tipoServico.getValor());

        os.setOrcamento(orcamento);
        os.setPecas(List.of(peca));
        os.setServicos(List.of(servico));

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
    public void given_statusEmDiagnosticoSemPecasCadastradas_when_transicionaParaAguardandoAprovacao_then_transicaoNaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setOrcamento(BigDecimal.valueOf(1000L, 2));

        OrdemDeServico.ValidacaoTransicaoDeStatus validacao = os.podeTransicionarParaStatus(StatusOrdemDeServico.AGUARDANDO_APROVACAO);

        assertFalse(validacao.permitida());
    }

    @Test
    public void given_statusEmDiagnosticoSemServicosCadastrados_when_transicionaParaAguardandoAprovacao_then_transicaoNaoPermitida() {
        OrdemDeServico os = new OrdemDeServico();
        os.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);
        os.setOrcamento(BigDecimal.valueOf(1000L, 2));

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

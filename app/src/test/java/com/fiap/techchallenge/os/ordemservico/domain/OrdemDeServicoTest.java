package com.fiap.techchallenge.os.ordemservico.domain;

import com.fiap.techchallenge.os.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrdemDeServicoTest {

    private static final Map<StatusOrdemDeServico, Set<StatusOrdemDeServico>> PERMITIDAS = Map.of(
            RECEBIDA, EnumSet.of(AGUARDANDO_APROVACAO, CANCELADA),
            EM_DIAGNOSTICO, EnumSet.of(AGUARDANDO_APROVACAO, CANCELADA),
            AGUARDANDO_APROVACAO, EnumSet.of(AGUARDANDO_PAGAMENTO, CANCELADA),
            AGUARDANDO_PAGAMENTO, EnumSet.of(APROVADA, AGUARDANDO_APROVACAO, CANCELADA),
            APROVADA, EnumSet.of(EM_EXECUCAO, CANCELADA),
            EM_EXECUCAO, EnumSet.of(FINALIZADA, CANCELADA),
            FINALIZADA, EnumSet.of(ENTREGUE),
            ENTREGUE, EnumSet.noneOf(StatusOrdemDeServico.class),
            CANCELADA, EnumSet.noneOf(StatusOrdemDeServico.class));

    private static OrdemDeServico osCom(StatusOrdemDeServico status) {
        var os = new OrdemDeServico();
        os.setId(1L);
        os.setStatus(status);
        return os;
    }

    static Stream<Arguments> todasAsTransicoes() {
        return Stream.of(StatusOrdemDeServico.values()).flatMap(de ->
                Stream.of(StatusOrdemDeServico.values()).map(para ->
                        Arguments.of(de, para, PERMITIDAS.get(de).contains(para))));
    }

    @ParameterizedTest(name = "{0} -> {1} permitida={2}")
    @MethodSource("todasAsTransicoes")
    public void given_qualquerParDeStatus_when_podeTransicionar_then_respeitaMaquinaDeEstados(
            StatusOrdemDeServico de, StatusOrdemDeServico para, boolean esperado) {
        var validacao = osCom(de).podeTransicionarParaStatus(para);

        assertEquals(esperado, validacao.permitida());
        if (esperado) {
            assertNull(validacao.erroDeValidacao());
        } else {
            assertTrue(validacao.erroDeValidacao().contains("'%s'".formatted(de)));
        }
    }

    // valorTotalMaiorQueZero

    @Test
    public void given_valorPositivo_when_verificaValor_then_retornaVerdadeiro() {
        var os = new OrdemDeServico();
        os.setValorTotal(BigDecimal.valueOf(100));
        assertTrue(os.valorTotalMaiorQueZero());
    }

    @Test
    public void given_valorZeroOuNulo_when_verificaValor_then_retornaFalso() {
        var os = new OrdemDeServico();
        os.setValorTotal(BigDecimal.ZERO);
        assertFalse(os.valorTotalMaiorQueZero());
        os.setValorTotal(null);
        assertFalse(os.valorTotalMaiorQueZero());
    }

    // métodos de domínio: sucesso

    @ParameterizedTest
    @EnumSource(value = StatusOrdemDeServico.class, names = {"RECEBIDA", "EM_DIAGNOSTICO"})
    public void given_osSemOrcamento_when_registrarOrcamento_then_guardaDadosEAguardaAprovacao(StatusOrdemDeServico origem) {
        var os = osCom(origem);

        os.registrarOrcamento("orc-1", new BigDecimal("300.00"));

        assertEquals(AGUARDANDO_APROVACAO, os.getStatus());
        assertEquals("orc-1", os.getOrcamentoId());
        assertEquals(new BigDecimal("300.00"), os.getValorTotal());
    }

    @Test
    public void given_aguardandoAprovacao_when_aprovarOrcamento_then_aguardandoPagamento() {
        var os = osCom(AGUARDANDO_APROVACAO);
        os.setOrcamentoId("orc-1");
        os.aprovarOrcamento();
        assertEquals(AGUARDANDO_PAGAMENTO, os.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemDeServico.class, names = {"AGUARDANDO_APROVACAO", "AGUARDANDO_PAGAMENTO"})
    public void given_osComOrcamentoPendente_when_rejeitarOrcamento_then_cancelada(StatusOrdemDeServico origem) {
        var os = osCom(origem);
        os.rejeitarOrcamento();
        assertEquals(CANCELADA, os.getStatus());
    }

    @Test
    public void given_aguardandoPagamento_when_confirmarPagamento_then_aprovada() {
        var os = osCom(AGUARDANDO_PAGAMENTO);
        os.confirmarPagamento();
        assertEquals(APROVADA, os.getStatus());
    }

    @Test
    public void given_falhasAbaixoDoLimite_when_registrarFalhaPagamento_then_voltaParaAprovacaoEIncrementa() {
        var os = osCom(AGUARDANDO_PAGAMENTO);
        os.setTentativasPagamento(1);

        os.registrarFalhaPagamento(3);

        assertEquals(AGUARDANDO_APROVACAO, os.getStatus());
        assertEquals(2, os.getTentativasPagamento());
    }

    @Test
    public void given_falhaNoLimite_when_registrarFalhaPagamento_then_cancelada() {
        var os = osCom(AGUARDANDO_PAGAMENTO);
        os.setTentativasPagamento(2);

        os.registrarFalhaPagamento(3);

        assertEquals(CANCELADA, os.getStatus());
        assertEquals(3, os.getTentativasPagamento());
    }

    @Test
    public void given_aprovada_when_iniciarExecucao_then_emExecucao() {
        var os = osCom(APROVADA);
        os.iniciarExecucao();
        assertEquals(EM_EXECUCAO, os.getStatus());
    }

    @Test
    public void given_emExecucao_when_finalizarExecucao_then_finalizada() {
        var os = osCom(EM_EXECUCAO);
        os.finalizarExecucao();
        assertEquals(FINALIZADA, os.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemDeServico.class, names = {"APROVADA", "EM_EXECUCAO"})
    public void given_osEmAndamento_when_falharExecucao_then_cancelada(StatusOrdemDeServico origem) {
        var os = osCom(origem);
        os.falharExecucao();
        assertEquals(CANCELADA, os.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemDeServico.class, names = {"RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO", "AGUARDANDO_PAGAMENTO"})
    public void given_statusQuePermiteCancelar_when_cancelar_then_cancelada(StatusOrdemDeServico origem) {
        var os = osCom(origem);
        os.cancelar();
        assertEquals(CANCELADA, os.getStatus());
    }

    @Test
    public void given_finalizada_when_entregar_then_entregue() {
        var os = osCom(FINALIZADA);
        os.entregar();
        assertEquals(ENTREGUE, os.getStatus());
    }

    // métodos de domínio: falhas

    @ParameterizedTest
    @EnumSource(value = StatusOrdemDeServico.class, names = {"RECEBIDA", "EM_DIAGNOSTICO"}, mode = EnumSource.Mode.EXCLUDE)
    public void given_statusInvalido_when_registrarOrcamento_then_badRequest(StatusOrdemDeServico origem) {
        var os = osCom(origem);
        assertThrows(BadRequestException.class, () -> os.registrarOrcamento("orc-1", BigDecimal.TEN));
        assertEquals(origem, os.getStatus());
        assertNull(os.getOrcamentoId());
    }

    @Test
    public void given_dadosInvalidos_when_registrarOrcamento_then_badRequest() {
        var os = osCom(RECEBIDA);
        assertThrows(BadRequestException.class, () -> os.registrarOrcamento(null, BigDecimal.TEN));
        assertThrows(BadRequestException.class, () -> os.registrarOrcamento(" ", BigDecimal.TEN));
        assertThrows(BadRequestException.class, () -> os.registrarOrcamento("orc-1", null));
        assertThrows(BadRequestException.class, () -> os.registrarOrcamento("orc-1", BigDecimal.ZERO));
        assertEquals(RECEBIDA, os.getStatus());
    }

    @Test
    public void given_semOrcamentoId_when_aprovarOrcamento_then_illegalState() {
        var os = osCom(AGUARDANDO_APROVACAO);
        assertThrows(IllegalStateException.class, os::aprovarOrcamento);
    }

    private static Stream<Arguments> metodosEStatusInvalidos() {
        return Stream.of(
                invalido("aprovarOrcamento", OrdemDeServico::aprovarOrcamento, AGUARDANDO_APROVACAO),
                invalido("rejeitarOrcamento", OrdemDeServico::rejeitarOrcamento, AGUARDANDO_APROVACAO, AGUARDANDO_PAGAMENTO),
                invalido("confirmarPagamento", OrdemDeServico::confirmarPagamento, AGUARDANDO_PAGAMENTO),
                invalido("registrarFalhaPagamento", os -> os.registrarFalhaPagamento(3), AGUARDANDO_PAGAMENTO),
                invalido("iniciarExecucao", OrdemDeServico::iniciarExecucao, APROVADA),
                invalido("finalizarExecucao", OrdemDeServico::finalizarExecucao, EM_EXECUCAO),
                invalido("falharExecucao", OrdemDeServico::falharExecucao, APROVADA, EM_EXECUCAO),
                invalido("cancelar", OrdemDeServico::cancelar, RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, AGUARDANDO_PAGAMENTO),
                invalido("entregar", OrdemDeServico::entregar, FINALIZADA)
        ).flatMap(s -> s);
    }

    private static Stream<Arguments> invalido(String nome, Consumer<OrdemDeServico> acao, StatusOrdemDeServico... validos) {
        Set<StatusOrdemDeServico> ok = EnumSet.copyOf(Set.of(validos));
        return Stream.of(StatusOrdemDeServico.values())
                .filter(s -> !ok.contains(s))
                .map(s -> Arguments.of(nome, acao, s));
    }

    @ParameterizedTest(name = "{0} em {2}")
    @MethodSource("metodosEStatusInvalidos")
    public void given_statusInvalido_when_metodoDeDominio_then_badRequestESemMudanca(
            String nome, Consumer<OrdemDeServico> acao, StatusOrdemDeServico origem) {
        var os = osCom(origem);
        os.setOrcamentoId("orc-1");

        assertThrows(BadRequestException.class, () -> acao.accept(os));
        assertEquals(origem, os.getStatus());
        assertEquals(0, os.getTentativasPagamento());
    }
}

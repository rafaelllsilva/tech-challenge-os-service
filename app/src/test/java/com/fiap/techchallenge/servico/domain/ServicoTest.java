package com.fiap.techchallenge.servico.domain;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.exception.ServicoBadStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicoTest {

    private Servico servico;
    private TipoServico tipoServico;
    private OrdemDeServico ordemDeServico;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));
        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(1L);

        servico = new Servico(
                1L,
                tipoServico,
                ordemDeServico,
                ServicoStatus.AGUARDANDO_INICIO,
                null,
                null
        );
    }

    // --- criar factory method ---

    @Test
    void testCriar_criaServicoComStatusAguardandoInicio() {
        Servico novoServico = Servico.criar(tipoServico, ordemDeServico);

        assertNotNull(novoServico);
        assertNull(novoServico.getId());
        assertEquals(tipoServico, novoServico.getTipoServico());
        assertEquals(ordemDeServico, novoServico.getOrdemDeServico());
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, novoServico.getStatus());
        assertNull(novoServico.getDataInicio());
        assertNull(novoServico.getDataFim());
    }

    // --- atualizar ---

    @Test
    void testAtualizar_comNovoTipo_atualizaComSucesso() {
        TipoServico novoTipo = new TipoServico(2L, "Alinhamento", new BigDecimal("200.00"));
        OrdemDeServico novaOrdem = new OrdemDeServico();
        novaOrdem.setId(2L);

        servico.atualizar(novaOrdem, novoTipo);

        assertEquals(novoTipo, servico.getTipoServico());
        assertEquals(novaOrdem, servico.getOrdemDeServico());
    }

    @Test
    void testAtualizar_comNulosTipo_naoAlteraValoresAntigos() {
        TipoServico tipoOriginal = servico.getTipoServico();

        servico.atualizar(null, null);

        assertEquals(tipoOriginal, servico.getTipoServico());
        assertEquals(ordemDeServico, servico.getOrdemDeServico());
    }

    @Test
    void testAtualizar_apenasOrdem_alteraOrdemEmantemTipo() {
        OrdemDeServico novaOrdem = new OrdemDeServico();
        novaOrdem.setId(3L);
        TipoServico tipoOriginal = servico.getTipoServico();

        servico.atualizar(novaOrdem, null);

        assertEquals(tipoOriginal, servico.getTipoServico());
        assertEquals(novaOrdem, servico.getOrdemDeServico());
    }

    @Test
    void testAtualizar_apenasTipo_alteraTipoEmantemOrdem() {
        TipoServico novoTipo = new TipoServico(2L, "Alinhamento", new BigDecimal("200.00"));
        OrdemDeServico ordemOriginal = servico.getOrdemDeServico();

        servico.atualizar(null, novoTipo);

        assertEquals(novoTipo, servico.getTipoServico());
        assertEquals(ordemOriginal, servico.getOrdemDeServico());
    }

    @Test
    void testAtualizar_comStatusFinalizado_lancaExcecao() {
        servico.setStatus(ServicoStatus.FINALIZADO);

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.atualizar(null, tipoServico);
        });
    }

    @Test
    void testAtualizar_comStatusDeletado_permiteMudanca() {
        servico.setStatus(ServicoStatus.DELETADO);
        TipoServico novoTipo = new TipoServico(2L, "Novo Tipo", new BigDecimal("100.00"));

        servico.atualizar(null, novoTipo);

        assertEquals(novoTipo, servico.getTipoServico());
    }

    // --- iniciar ---

    @Test
    void testIniciar_comStatusAguardandoInicio_transicaoParaEmExecucao() {
        assertNull(servico.getDataInicio());
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, servico.getStatus());

        servico.iniciar();

        assertEquals(ServicoStatus.EM_EXECUCAO, servico.getStatus());
        assertNotNull(servico.getDataInicio());
    }

    @Test
    void testIniciar_jaEmExecucao_lancaExcecao() {
        servico.setStatus(ServicoStatus.EM_EXECUCAO);

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.iniciar();
        });
    }

    @Test
    void testIniciar_comStatusFinalizado_lancaExcecao() {
        servico.setStatus(ServicoStatus.FINALIZADO);

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.iniciar();
        });
    }

    @Test
    void testIniciar_comStatusDeletado_lancaExcecao() {
        servico.setStatus(ServicoStatus.DELETADO);

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.iniciar();
        });
    }

    // --- finalizar ---

    @Test
    void testFinalizar_comStatusEmExecucao_transicaoParaFinalizado() {
        servico.setStatus(ServicoStatus.EM_EXECUCAO);
        servico.setDataInicio(LocalDateTime.now().minusHours(1));
        assertNull(servico.getDataFim());

        servico.finalizar();

        assertEquals(ServicoStatus.FINALIZADO, servico.getStatus());
        assertNotNull(servico.getDataFim());
    }

    @Test
    void testFinalizar_comStatusAguardandoInicio_lancaExcecao() {
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, servico.getStatus());

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.finalizar();
        });
    }

    @Test
    void testFinalizar_jaFinalizado_lancaExcecao() {
        servico.setStatus(ServicoStatus.FINALIZADO);
        servico.setDataFim(LocalDateTime.now());

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.finalizar();
        });
    }

    @Test
    void testFinalizar_comStatusDeletado_lancaExcecao() {
        servico.setStatus(ServicoStatus.DELETADO);

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.finalizar();
        });
    }

    // --- deletar ---

    @Test
    void testDeletar_comStatusAguardandoInicio_transicionaParaDeletado() {
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, servico.getStatus());

        servico.deletar();

        assertEquals(ServicoStatus.DELETADO, servico.getStatus());
    }

    @Test
    void testDeletar_comStatusFinalizado_transicionaParaDeletado() {
        servico.setStatus(ServicoStatus.FINALIZADO);
        servico.setDataFim(LocalDateTime.now());

        servico.deletar();

        assertEquals(ServicoStatus.DELETADO, servico.getStatus());
    }

    @Test
    void testDeletar_comStatusDeletado_permaneceComDeletado() {
        servico.setStatus(ServicoStatus.DELETADO);

        servico.deletar();

        assertEquals(ServicoStatus.DELETADO, servico.getStatus());
    }

    @Test
    void testDeletar_comStatusEmExecucao_lancaExcecao() {
        servico.setStatus(ServicoStatus.EM_EXECUCAO);
        servico.setDataInicio(LocalDateTime.now());

        assertThrows(ServicoBadStatusException.class, () -> {
            servico.deletar();
        });
    }

    // --- getters ---

    @Test
    void testGetters_retornamValoresCorretos() {
        LocalDateTime agora = LocalDateTime.now();
        servico.setId(1L);
        servico.setDataInicio(agora);
        servico.setDataFim(agora.plusHours(1));

        assertEquals(1L, servico.getId());
        assertEquals(tipoServico, servico.getTipoServico());
        assertEquals(ordemDeServico, servico.getOrdemDeServico());
        assertEquals(agora, servico.getDataInicio());
        assertEquals(agora.plusHours(1), servico.getDataFim());
    }

    // --- setters ---

    @Test
    void testSetters_alteramValoresCorretamente() {
        Long novoId = 2L;
        TipoServico novoTipo = new TipoServico(2L, "Novo Tipo", new BigDecimal("100.00"));
        OrdemDeServico novaOrdem = new OrdemDeServico();
        novaOrdem.setId(2L);
        LocalDateTime agora = LocalDateTime.now();
        ServicoStatus novoStatus = ServicoStatus.EM_EXECUCAO;

        servico.setId(novoId);
        servico.setTipoServico(novoTipo);
        servico.setOrdemDeServico(novaOrdem);
        servico.setDataInicio(agora);
        servico.setDataFim(agora.plusHours(2));
        servico.setStatus(novoStatus);

        assertEquals(novoId, servico.getId());
        assertEquals(novoTipo, servico.getTipoServico());
        assertEquals(novaOrdem, servico.getOrdemDeServico());
        assertEquals(agora, servico.getDataInicio());
        assertEquals(agora.plusHours(2), servico.getDataFim());
        assertEquals(novoStatus, servico.getStatus());
    }

    // --- constructor ---

    @Test
    void testConstructorCompleto_criaServicoComTodosOsCampos() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(2);
        LocalDateTime fim = LocalDateTime.now();

        Servico novoServico = new Servico(
                5L,
                tipoServico,
                ordemDeServico,
                ServicoStatus.FINALIZADO,
                inicio,
                fim
        );

        assertEquals(5L, novoServico.getId());
        assertEquals(tipoServico, novoServico.getTipoServico());
        assertEquals(ordemDeServico, novoServico.getOrdemDeServico());
        assertEquals(ServicoStatus.FINALIZADO, novoServico.getStatus());
        assertEquals(inicio, novoServico.getDataInicio());
        assertEquals(fim, novoServico.getDataFim());
    }

    @Test
    void testConstructorVazio_criaServicoSemCampos() {
        Servico novoServico = new Servico();

        assertNull(novoServico.getId());
        assertNull(novoServico.getTipoServico());
        assertNull(novoServico.getOrdemDeServico());
        assertNull(novoServico.getStatus());
        assertNull(novoServico.getDataInicio());
        assertNull(novoServico.getDataFim());
    }
}

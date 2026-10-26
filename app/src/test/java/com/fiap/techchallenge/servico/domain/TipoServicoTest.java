package com.fiap.techchallenge.servico.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TipoServicoTest {

    private TipoServico tipoServico;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));
    }

    // --- criar factory method ---

    @Test
    void testCriar_criaComIdNuloESemId() {
        TipoServico novo = TipoServico.criar("Alinhamento", new BigDecimal("200.00"));

        assertNotNull(novo);
        assertNull(novo.getId());
        assertEquals("Alinhamento", novo.getNome());
        assertEquals(new BigDecimal("200.00"), novo.getValor());
    }

    @Test
    void testCriar_preservaNomeEValor() {
        String nome = "Reparo de Freio";
        BigDecimal valor = new BigDecimal("350.50");

        TipoServico novo = TipoServico.criar(nome, valor);

        assertEquals(nome, novo.getNome());
        assertEquals(valor, novo.getValor());
    }

    // --- atualizar ---

    @Test
    void testAtualizar_comNomeENovo_atualizaAmbos() {
        tipoServico.atualizar("Novo Nome", new BigDecimal("200.00"));

        assertEquals("Novo Nome", tipoServico.getNome());
        assertEquals(new BigDecimal("200.00"), tipoServico.getValor());
    }

    @Test
    void testAtualizar_apenasNome_atualizaNomePreservaValor() {
        BigDecimal valorOriginal = tipoServico.getValor();

        tipoServico.atualizar("Novo Nome", null);

        assertEquals("Novo Nome", tipoServico.getNome());
        assertEquals(valorOriginal, tipoServico.getValor());
    }

    @Test
    void testAtualizar_apenasValor_atualizaValorPreservaNome() {
        String nomeOriginal = tipoServico.getNome();

        tipoServico.atualizar(null, new BigDecimal("250.00"));

        assertEquals(nomeOriginal, tipoServico.getNome());
        assertEquals(new BigDecimal("250.00"), tipoServico.getValor());
    }

    @Test
    void testAtualizar_comAmboNulos_naoAltera() {
        String nomeOriginal = tipoServico.getNome();
        BigDecimal valorOriginal = tipoServico.getValor();

        tipoServico.atualizar(null, null);

        assertEquals(nomeOriginal, tipoServico.getNome());
        assertEquals(valorOriginal, tipoServico.getValor());
    }

    @Test
    void testAtualizar_comNomeVazio_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            tipoServico.atualizar("", new BigDecimal("200.00"));
        });
    }

    @Test
    void testAtualizar_comNomeEmBranco_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            tipoServico.atualizar("   ", new BigDecimal("200.00"));
        });
    }

    @Test
    void testAtualizar_comValorNegativo_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            tipoServico.atualizar("Novo Nome", new BigDecimal("-100.00"));
        });
    }

    @Test
    void testAtualizar_comValorZero_permiteAtualizacao() {
        tipoServico.atualizar("Novo Nome", BigDecimal.ZERO);

        assertEquals("Novo Nome", tipoServico.getNome());
        assertEquals(BigDecimal.ZERO, tipoServico.getValor());
    }

    @Test
    void testAtualizar_comNomeVazio_eValorValido_lancaExcecaoNome() {
        assertThrows(IllegalArgumentException.class, () -> {
            tipoServico.atualizar("", new BigDecimal("100.00"));
        });

        assertEquals("Troca de Óleo", tipoServico.getNome());
    }

    @Test
    void testAtualizar_comNomeValido_eValorNegativo_lancaExcecaoValor() {
        assertThrows(IllegalArgumentException.class, () -> {
            tipoServico.atualizar("Novo Nome", new BigDecimal("-50.00"));
        });

        assertEquals("Troca de Óleo", tipoServico.getNome());
    }

    // --- getters ---

    @Test
    void testGetters_retornamValoresCorretos() {
        assertEquals(1L, tipoServico.getId());
        assertEquals("Troca de Óleo", tipoServico.getNome());
        assertEquals(new BigDecimal("150.00"), tipoServico.getValor());
    }

    @Test
    void testGetter_id_comValorNulo() {
        TipoServico novo = new TipoServico();

        assertNull(novo.getId());
    }

    // --- setters ---

    @Test
    void testSetters_alteramValoresCorretamente() {
        tipoServico.setId(2L);
        tipoServico.setNome("Alinhamento");
        tipoServico.setValor(new BigDecimal("200.00"));

        assertEquals(2L, tipoServico.getId());
        assertEquals("Alinhamento", tipoServico.getNome());
        assertEquals(new BigDecimal("200.00"), tipoServico.getValor());
    }

    @Test
    void testSetter_nome_comNuloEPermite() {
        tipoServico.setNome(null);

        assertNull(tipoServico.getNome());
    }

    @Test
    void testSetter_valor_comNuloEPermite() {
        tipoServico.setValor(null);

        assertNull(tipoServico.getValor());
    }

    // --- constructor ---

    @Test
    void testConstructorCompleto_criaComTodosOsCampos() {
        TipoServico novo = new TipoServico(5L, "Pintura", new BigDecimal("500.00"));

        assertEquals(5L, novo.getId());
        assertEquals("Pintura", novo.getNome());
        assertEquals(new BigDecimal("500.00"), novo.getValor());
    }

    @Test
    void testConstructorVazio_criaComCamposNulos() {
        TipoServico novo = new TipoServico();

        assertNull(novo.getId());
        assertNull(novo.getNome());
        assertNull(novo.getValor());
    }

    // --- validation edge cases ---

    @Test
    void testAtualizar_comValorDecimal_atualizaComPrecisao() {
        tipoServico.atualizar("Serviço", new BigDecimal("150.99"));

        assertEquals(new BigDecimal("150.99"), tipoServico.getValor());
    }

    @Test
    void testAtualizar_comNomeComEspacos_permiteAtualizacao() {
        tipoServico.atualizar("Nome Com Espaços", new BigDecimal("100.00"));

        assertEquals("Nome Com Espaços", tipoServico.getNome());
    }

    @Test
    void testAtualizar_comNomeUmCaractere_atualizaComSucesso() {
        tipoServico.atualizar("A", new BigDecimal("50.00"));

        assertEquals("A", tipoServico.getNome());
    }

    @Test
    void testAtualizar_campoNomeComTabEEspacoNoInicio_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            tipoServico.atualizar("  \t  ", new BigDecimal("100.00"));
        });
    }
}

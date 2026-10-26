package com.fiap.techchallenge.peca.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.peca.usecase.TipoPecaGateway;
import com.fiap.techchallenge.peca.usecase.TipoPecaInteractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoPecaInteractorTest {

    @Mock
    private TipoPecaGateway tipoPecaGateway;

    @InjectMocks
    private TipoPecaInteractor tipoPecaInteractor;

    private TipoPeca criarTipoPeca(Long id) {
        TipoPeca tipoPeca = new TipoPeca();
        tipoPeca.setId(id);
        tipoPeca.setNome("Pastilha de freio");
        tipoPeca.setValorUnitario(BigDecimal.valueOf(120));
        tipoPeca.setQuantidadeEstoque(10);
        return tipoPeca;
    }

    @Test
    public void buscarTiposPeca_quandoExistem_returnsLista() {
        var tipoPeca = criarTipoPeca(1L);
        when(tipoPecaGateway.findAll()).thenReturn(List.of(tipoPeca));

        var resultado = tipoPecaInteractor.getAllTiposPeca();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void buscarTiposPeca_quandoNaoExistem_returnsListaVazia() {
        when(tipoPecaGateway.findAll()).thenReturn(Collections.emptyList());

        var resultado = tipoPecaInteractor.getAllTiposPeca();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void buscarTipoPecaPorId_quandoExiste_returnsTipoPeca() {
        var tipoPeca = criarTipoPeca(1L);
        when(tipoPecaGateway.findById(1L)).thenReturn(Optional.of(tipoPeca));

        var resultado = tipoPecaInteractor.getTipoPeca(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    public void buscarTipoPecaPorId_quandoNaoExiste_returnsVazio() {
        when(tipoPecaGateway.findById(1L)).thenReturn(Optional.empty());

        var resultado = tipoPecaInteractor.getTipoPeca(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void criarTipoPeca_quandoValido_returnsTipoPecaCriada() {
        var tipoPecaSalva = criarTipoPeca(1L);
        when(tipoPecaGateway.save(any())).thenReturn(tipoPecaSalva);

        var resultado = tipoPecaInteractor.createTipoPeca("Pastilha de freio", BigDecimal.valueOf(120), 10);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pastilha de freio", resultado.getNome());
    }

    @Test
    public void atualizarTipoPeca_quandoExiste_returnsAtualizada() {
        var tipoPeca = criarTipoPeca(1L);
        when(tipoPecaGateway.findById(1L)).thenReturn(Optional.of(tipoPeca));
        when(tipoPecaGateway.save(any())).thenReturn(tipoPeca);

        var resultado = tipoPecaInteractor.updateTipoPeca(1L, null, null, 20);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(tipoPecaGateway).save(any());
    }

    @Test
    public void atualizarTipoPeca_quandoNaoExiste_throwsNotFoundException() {
        when(tipoPecaGateway.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tipoPecaInteractor.updateTipoPeca(1L, null, null, 20));
        verify(tipoPecaGateway, never()).save(any());
    }

    @Test
    public void deletarTipoPeca_quandoExiste_returnsDeletada() {
        var tipoPeca = criarTipoPeca(1L);
        when(tipoPecaGateway.findById(1L)).thenReturn(Optional.of(tipoPeca));

        var resultado = tipoPecaInteractor.deleteTipoPeca(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(tipoPecaGateway).delete(tipoPeca);
    }

    @Test
    public void deletarTipoPeca_quandoNaoExiste_throwsNotFoundException() {
        when(tipoPecaGateway.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tipoPecaInteractor.deleteTipoPeca(1L));
        verify(tipoPecaGateway, never()).delete(any());
    }
}

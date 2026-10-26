package com.fiap.techchallenge.peca.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoGateway;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PecaInteractorTest {

    @Mock
    private PecaGateway pecaGateway;

    @Mock
    private TipoPecaGateway tipoPecaGateway;

    // validar 
    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @InjectMocks
    private PecaInteractor pecaInteractor;

    private Peca criarPeca(Long id) {
        TipoPeca tp = new TipoPeca();
        tp.setId(2L);
        OrdemDeServico os = new OrdemDeServico();
        os.setId(1L);
        Peca peca = new Peca();
        peca.setId(id);
        peca.setOrdemDeServico(os);
        peca.setTipoPeca(tp);
        peca.setQuantidade(3);
        return peca;
    }

    @Test
    public void buscarPecas_quandoExistem_returnsLista() {
        var peca = criarPeca(1L);
        when(pecaGateway.findAll()).thenReturn(List.of(peca));

        var resultado = pecaInteractor.getAllPecas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void buscarPecas_quandoNaoExistem_returnsListaVazia() {
        when(pecaGateway.findAll()).thenReturn(Collections.emptyList());

        var resultado = pecaInteractor.getAllPecas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void buscarPecaPorId_quandoExiste_returnsPeca() {
        var peca = criarPeca(1L);
        when(pecaGateway.findById(1L)).thenReturn(Optional.of(peca));

        var resultado = pecaInteractor.getPeca(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    public void buscarPecaPorId_quandoNaoExiste_returnsVazio() {
        when(pecaGateway.findById(1L)).thenReturn(Optional.empty());

        var resultado = pecaInteractor.getPeca(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void criarPeca_quandoValido_returnsPecaCriada() {
        var tipoPeca = new TipoPeca();
        tipoPeca.setId(2L);
        var os = new OrdemDeServico();
        os.setId(1L);
        var pecaSalva = criarPeca(1L);

        when(tipoPecaGateway.findById(2L)).thenReturn(Optional.of(tipoPeca));
        when(ordemDeServicoGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(pecaGateway.save(any())).thenReturn(pecaSalva);

        var resultado = pecaInteractor.createPeca(1L, 2L, 3);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getOrdemDeServico().getId());
    }

    @Test
    public void criarPeca_quandoOsNaoExiste_throwsNotFoundException() {
        when(ordemDeServicoGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pecaInteractor.createPeca(1L, 2L, 3));
        verify(pecaGateway, never()).save(any());
    }

    @Test
    public void atualizarPeca_quandoExiste_returnsAtualizada() {
        var peca = criarPeca(1L);
        when(pecaGateway.findById(1L)).thenReturn(Optional.of(peca));
        when(pecaGateway.save(any())).thenReturn(peca);

        var resultado = pecaInteractor.updatePeca(1L, null, null, 10);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pecaGateway).save(any());
    }

    @Test
    public void atualizarPeca_quandoNaoExiste_throwsNotFoundException() {
        when(pecaGateway.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pecaInteractor.updatePeca(1L, null, null, 10));
        verify(pecaGateway, never()).save(any());
    }

    @Test
    public void deletarPeca_quandoExiste_returnsDeletada() {
        var peca = criarPeca(1L);
        when(pecaGateway.findById(1L)).thenReturn(Optional.of(peca));

        var resultado = pecaInteractor.deletePeca(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pecaGateway).delete(peca);
    }

    @Test
    public void deletarPeca_quandoNaoExiste_throwsNotFoundException() {
        when(pecaGateway.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pecaInteractor.deletePeca(1L));
        verify(pecaGateway, never()).delete(any());
    }
}

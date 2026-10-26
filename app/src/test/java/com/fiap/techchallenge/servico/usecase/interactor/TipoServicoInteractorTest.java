package com.fiap.techchallenge.servico.usecase.interactor;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.exception.TipoServicoUnableToModify;
import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.command.TipoServicoRequestsCommand;
import com.fiap.techchallenge.servico.usecase.gateway.ServicoGateway;
import com.fiap.techchallenge.servico.usecase.gateway.TipoServicoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoServicoInteractorTest {

    @Mock
    private TipoServicoGateway tipoServicoGateway;

    @Mock
    private ServicoGateway servicoGateway;

    @InjectMocks
    private TipoServicoInteractor interactor;

    private TipoServico tipoServico;
    private TipoServicoRequestsCommand createCommand;
    private TipoServicoRequestsCommand updateCommand;
    private TempoMedioServicoDTO tempoMedioDTO;
    private TempoMedioServicoDTO tempoMedioDTO2;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        createCommand = new TipoServicoRequestsCommand("Alinhamento", new BigDecimal("100.00"));
        updateCommand = new TipoServicoRequestsCommand("Alinhamento Atualizado", new BigDecimal("120.00"));

        tempoMedioDTO = new TempoMedioServicoDTO() {
            @Override public String getNome() { return "Troca de Óleo"; }
            @Override public BigDecimal getTempoMedioMinutos() { return BigDecimal.valueOf(45.0); }
        };

        tempoMedioDTO2 = new TempoMedioServicoDTO() {
            @Override public String getNome() { return "Alinhamento"; }
            @Override public BigDecimal getTempoMedioMinutos() { return BigDecimal.valueOf(30.0); }
        };
    }

    // --- getById ---

    @Test
    void testGetById_sucesso() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));

        TipoServico resultado = interactor.getById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Troca de Óleo", resultado.getNome());
        verify(tipoServicoGateway, times(1)).findById(1L);
    }

    @Test
    void testGetById_naoEncontrado() {
        when(tipoServicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.getById(999L));
        verify(tipoServicoGateway, times(1)).findById(999L);
    }

    // --- listAll ---

    @Test
    void testListAll_retornaLista() {
        TipoServico outro = new TipoServico(2L, "Alinhamento", new BigDecimal("100.00"));
        when(tipoServicoGateway.findAll()).thenReturn(List.of(tipoServico, outro));

        List<TipoServico> resultado = interactor.listAll();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(tipoServicoGateway, times(1)).findAll();
    }

    @Test
    void testListAll_listaVazia() {
        when(tipoServicoGateway.findAll()).thenReturn(List.of());

        List<TipoServico> resultado = interactor.listAll();

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    // --- getTempoMedio ---

    @Test
    void testGetTempoMedio_semFiltro_retornaTodos() {
        when(tipoServicoGateway.findTempoMedioPorTipoServico())
                .thenReturn(List.of(tempoMedioDTO, tempoMedioDTO2));

        List<TempoMedioServicoDTO> resultado = interactor.getTempoMedio(null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Troca de Óleo", resultado.get(0).getNome());
        assertEquals(0, new BigDecimal("45.00").compareTo(resultado.get(0).getTempoMedioMinutos()));
        verify(tipoServicoGateway, times(1)).findTempoMedioPorTipoServico();
    }

    @Test
    void testGetTempoMedio_comId_retornaUnico() {
        when(tipoServicoGateway.findTempoMedioPorTipoServicoId(1L))
                .thenReturn(Optional.of(tempoMedioDTO));

        List<TempoMedioServicoDTO> resultado = interactor.getTempoMedio(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Troca de Óleo", resultado.getFirst().getNome());
        verify(tipoServicoGateway, times(1)).findTempoMedioPorTipoServicoId(1L);
    }

    @Test
    void testGetTempoMedio_comId_naoEncontrado() {
        when(tipoServicoGateway.findTempoMedioPorTipoServicoId(999L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.getTempoMedio(999L));
        verify(tipoServicoGateway, times(1)).findTempoMedioPorTipoServicoId(999L);
    }

    @Test
    void testGetTempoMedio_listaVazia() {
        when(tipoServicoGateway.findTempoMedioPorTipoServico()).thenReturn(List.of());

        List<TempoMedioServicoDTO> resultado = interactor.getTempoMedio(null);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    // --- create ---

    @Test
    void testCreate_sucesso() {
        TipoServico criado = new TipoServico(2L, "Alinhamento", new BigDecimal("100.00"));
        when(tipoServicoGateway.save(any(TipoServico.class))).thenReturn(criado);

        TipoServico resultado = interactor.create(createCommand);

        assertNotNull(resultado);
        assertEquals("Alinhamento", resultado.getNome());
        assertEquals(new BigDecimal("100.00"), resultado.getValor());
        verify(tipoServicoGateway, times(1)).save(any(TipoServico.class));
    }

    // --- update ---

    @Test
    void testUpdate_sucesso() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(tipoServicoGateway.save(any(TipoServico.class))).thenReturn(tipoServico);

        TipoServico resultado = interactor.update(1L, updateCommand);

        assertNotNull(resultado);
        assertEquals("Alinhamento Atualizado", tipoServico.getNome());
        assertEquals(new BigDecimal("120.00"), tipoServico.getValor());
        verify(tipoServicoGateway, times(1)).save(tipoServico);
    }

    @Test
    void testUpdate_nomeNulo_mantemNomeAtual() {
        TipoServicoRequestsCommand commandSomenteValor = new TipoServicoRequestsCommand(null, new BigDecimal("200.00"));

        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(tipoServicoGateway.save(any(TipoServico.class))).thenReturn(tipoServico);

        interactor.update(1L, commandSomenteValor);

        assertEquals("Troca de Óleo", tipoServico.getNome());
        assertEquals(new BigDecimal("200.00"), tipoServico.getValor());
    }

    @Test
    void testUpdate_naoEncontrado() {
        when(tipoServicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.update(999L, updateCommand));
        verify(tipoServicoGateway, never()).save(any());
    }

    @Test
    void testUpdate_nomeVazio_lancaExcecao() {
        TipoServicoRequestsCommand commandNomeVazio = new TipoServicoRequestsCommand("  ", new BigDecimal("100.00"));

        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));

        assertThrows(IllegalArgumentException.class, () -> interactor.update(1L, commandNomeVazio));
        verify(tipoServicoGateway, never()).save(any());
    }

    @Test
    void testUpdate_valorNegativo_lancaExcecao() {
        TipoServicoRequestsCommand commandValorNegativo = new TipoServicoRequestsCommand("Teste", new BigDecimal("-10.00"));

        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));

        assertThrows(IllegalArgumentException.class, () -> interactor.update(1L, commandValorNegativo));
        verify(tipoServicoGateway, never()).save(any());
    }

    // --- delete ---

    @Test
    void testDelete_sucesso() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(servicoGateway.existsByTipoServicoId(1L)).thenReturn(false);
        doNothing().when(tipoServicoGateway).delete(1L);

        assertDoesNotThrow(() -> interactor.delete(1L));

        verify(tipoServicoGateway, times(1)).delete(1L);
    }

    @Test
    void testDelete_comServicosAssociados_lancaExcecao() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(servicoGateway.existsByTipoServicoId(1L)).thenReturn(true);

        assertThrows(TipoServicoUnableToModify.class, () -> interactor.delete(1L));
        verify(tipoServicoGateway, never()).delete(any());
    }

    @Test
    void testDelete_naoEncontrado() {
        when(tipoServicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.delete(999L));
        verify(tipoServicoGateway, never()).delete(any());
    }
}

package com.fiap.techchallenge.servico.usecase.interactor;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.exception.ServicoBadStatusException;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoGateway;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.command.ServicoCreateCommand;
import com.fiap.techchallenge.servico.usecase.command.ServicoUpdateCommand;
import com.fiap.techchallenge.servico.usecase.gateway.ServicoGateway;
import com.fiap.techchallenge.servico.usecase.gateway.TipoServicoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoInteractorTest {

    @Mock
    private ServicoGateway servicoGateway;

    @Mock
    private TipoServicoGateway tipoServicoGateway;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @InjectMocks
    private ServicoInteractor interactor;

    private TipoServico tipoServico;
    private OrdemDeServico ordemDeServico;
    private Servico servico;
    private ServicoCreateCommand createCommand;
    private ServicoUpdateCommand updateCommand;

    @BeforeEach
    void setUp() {
        tipoServico = new TipoServico(1L, "Troca de Óleo", new BigDecimal("150.00"));

        ordemDeServico = new OrdemDeServico();
        ordemDeServico.setId(1L);
        ordemDeServico.setStatus(StatusOrdemDeServico.RECEBIDA);

        servico = new Servico(
                1L,
                tipoServico,
                ordemDeServico,
                ServicoStatus.AGUARDANDO_INICIO,
                null,
                null
        );

        createCommand = new ServicoCreateCommand(1L, 1L);
        updateCommand = new ServicoUpdateCommand(1L, null);
    }

    // --- creteServico ---

    @Test
    void testCreteServico_sucesso() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(ordemDeServicoGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(ordemDeServico));
        when(servicoGateway.save(any(Servico.class))).thenReturn(servico);

        Servico resultado = interactor.creteServico(createCommand);

        assertNotNull(resultado);
        assertEquals(ServicoStatus.AGUARDANDO_INICIO, resultado.getStatus());
        verify(servicoGateway, times(1)).save(any(Servico.class));
    }

    @Test
    void testCreteServico_tipoServicoNaoEncontrado() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.creteServico(createCommand));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testCreteServico_ordemDeServicoNaoEncontrada() {
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(ordemDeServicoGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.creteServico(createCommand));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testCreteServico_ordemDeServicoStatusInvalido() {
        ordemDeServico.setStatus(StatusOrdemDeServico.EM_EXECUCAO);

        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(ordemDeServicoGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(ordemDeServico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.creteServico(createCommand));
        verify(servicoGateway, never()).save(any());
    }

    // --- listaServicos ---

    @Test
    void testListaServicos_semFiltro_retornaTodos() {
        when(servicoGateway.findAll()).thenReturn(List.of(servico));

        List<Servico> resultado = interactor.listaServicos(null);

        assertEquals(1, resultado.size());
        verify(servicoGateway, times(1)).findAll();
        verify(servicoGateway, never()).findAllByOsId(any());
    }

    @Test
    void testListaServicos_comIdOs_retornaFiltrado() {
        when(servicoGateway.findAllByOsId(1L)).thenReturn(Optional.of(List.of(servico)));

        List<Servico> resultado = interactor.listaServicos(1L);

        assertEquals(1, resultado.size());
        verify(servicoGateway, times(1)).findAllByOsId(1L);
    }

    @Test
    void testListaServicos_comIdOs_semServicos_lancaNotFound() {
        when(servicoGateway.findAllByOsId(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.listaServicos(99L));
        verify(servicoGateway, times(1)).findAllByOsId(99L);
    }

    // --- getServicoById ---

    @Test
    void testGetServicoById_sucesso() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        Servico resultado = interactor.getServicoById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testGetServicoById_naoEncontrado() {
        when(servicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.getServicoById(999L));
    }

    // --- deleteServico ---

    @Test
    void testDeleteServico_sucesso() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.save(any(Servico.class))).thenReturn(servico);

        assertDoesNotThrow(() -> interactor.deleteServico(1L));

        verify(servicoGateway, times(1)).save(any(Servico.class));
        assertEquals(ServicoStatus.DELETADO, servico.getStatus());
    }

    @Test
    void testDeleteServico_emExecucao_lancaExcecao() {
        servico = new Servico(1L, tipoServico, ordemDeServico,
                ServicoStatus.EM_EXECUCAO, LocalDateTime.now().minusHours(1), null);

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.deleteServico(1L));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testDeleteServico_naoEncontrado() {
        when(servicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.deleteServico(999L));
    }

    // --- updateServico ---

    @Test
    void testUpdateServico_sucesso_comTipoServico() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));
        when(tipoServicoGateway.findById(1L)).thenReturn(Optional.of(tipoServico));
        when(servicoGateway.save(any(Servico.class))).thenReturn(servico);

        Servico resultado = interactor.updateServico(1L, updateCommand);

        assertNotNull(resultado);
        verify(servicoGateway, times(1)).save(any(Servico.class));
    }

    @Test
    void testUpdateServico_sucesso_comOrdemDeServico() {
        ServicoUpdateCommand commandComOs = new ServicoUpdateCommand(null, 1L);

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));
        when(ordemDeServicoGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(ordemDeServico));
        when(servicoGateway.save(any(Servico.class))).thenReturn(servico);

        assertDoesNotThrow(() -> interactor.updateServico(1L, commandComOs));
        verify(servicoGateway, times(1)).save(any(Servico.class));
    }

    @Test
    void testUpdateServico_finalizado_lancaExcecao() {
        servico = new Servico(1L, tipoServico, ordemDeServico,
                ServicoStatus.FINALIZADO, LocalDateTime.now().minusHours(2), LocalDateTime.now());

        when(tipoServicoGateway.findById(any())).thenReturn(Optional.of(tipoServico));
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.updateServico(1L, updateCommand));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testUpdateServico_naoEncontrado() {
        when(servicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.updateServico(999L, updateCommand));
    }

    @Test
    void testUpdateServico_ordemDeServicoNaoEncontrada() {
        ServicoUpdateCommand commandComOs = new ServicoUpdateCommand(null, 99L);

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));
        when(ordemDeServicoGateway.findOrdemDeServicoById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.updateServico(1L, commandComOs));
    }

    // --- iniciarServico ---

    @Test
    void testIniciarServico_sucesso() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.save(any(Servico.class))).thenReturn(servico);

        assertDoesNotThrow(() -> interactor.iniciarServico(1L));

        assertEquals(ServicoStatus.EM_EXECUCAO, servico.getStatus());
        assertNotNull(servico.getDataInicio());
        verify(servicoGateway, times(1)).save(servico);
    }

    @Test
    void testIniciarServico_jaEmExecucao_lancaExcecao() {
        servico = new Servico(1L, tipoServico, ordemDeServico,
                ServicoStatus.EM_EXECUCAO, LocalDateTime.now().minusHours(1), null);

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.iniciarServico(1L));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testIniciarServico_jaFinalizado_lancaExcecao() {
        servico = new Servico(1L, tipoServico, ordemDeServico,
                ServicoStatus.FINALIZADO, LocalDateTime.now().minusHours(2), LocalDateTime.now());

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.iniciarServico(1L));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testIniciarServico_naoEncontrado() {
        when(servicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.iniciarServico(999L));
    }

    // --- finalizarServico ---

    @Test
    void testFinalizarServico_sucesso() {
        servico = new Servico(1L, tipoServico, ordemDeServico,
                ServicoStatus.EM_EXECUCAO, LocalDateTime.now().minusHours(1), null);

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoGateway.save(any(Servico.class))).thenReturn(servico);

        assertDoesNotThrow(() -> interactor.finalizarServico(1L));

        assertEquals(ServicoStatus.FINALIZADO, servico.getStatus());
        assertNotNull(servico.getDataFim());
        verify(servicoGateway, times(1)).save(servico);
    }

    @Test
    void testFinalizarServico_naoIniciado_lancaExcecao() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.finalizarServico(1L));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testFinalizarServico_jaFinalizado_lancaExcecao() {
        servico = new Servico(1L, tipoServico, ordemDeServico,
                ServicoStatus.FINALIZADO, LocalDateTime.now().minusHours(2), LocalDateTime.now());

        when(servicoGateway.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ServicoBadStatusException.class, () -> interactor.finalizarServico(1L));
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void testFinalizarServico_naoEncontrado() {
        when(servicoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> interactor.finalizarServico(999L));
    }
}

package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.cliente.domain.Cliente;
import com.fiap.techchallenge.os.cliente.domain.TipoPessoa;
import com.fiap.techchallenge.os.cliente.usecase.ClienteGateway;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.CreateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.UpdateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.observabilidade.OrdemDeServicoEventLogger;
import com.fiap.techchallenge.os.exception.BadRequestException;
import com.fiap.techchallenge.os.veiculo.domain.Veiculo;
import com.fiap.techchallenge.os.veiculo.usecase.VeiculoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdemDeServicoInteractorTest {

    @Mock
    private OrdemDeServicoGateway osGateway;

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Mock
    private OrdemDeServicoEventLogger eventLogger;

    @Mock
    private HistoricoStatusOrdemDeServicoGateway historicoStatusOrdemDeServicoGateway;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-29T12:00:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private OrdemDeServicoInteractor osUseCase;

    private OrdemDeServico criarOrdemDeServico(Long id, StatusOrdemDeServico status) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(123L);

        Cliente cliente = new Cliente();
        cliente.setId(456L);
        cliente.setNome("João Silva");
        cliente.setTipoPessoa(TipoPessoa.PF);

        OrdemDeServico os = new OrdemDeServico();
        os.setId(id);
        os.setStatus(status);
        os.setSolicitacao("Problema nos freios.");
        os.setOrcamento(BigDecimal.valueOf(100));
        os.setVeiculo(veiculo);
        os.setCliente(cliente);
        return os;
    }

    @BeforeEach
    void configurarHistoricoDeStatus() {
        lenient().when(historicoStatusOrdemDeServicoGateway.buscarInicioDoStatusAtual(anyLong()))
                .thenReturn(Optional.of(Instant.parse("2026-08-29T11:00:00Z")));
    }

    // #################### getAllOrdemDeServico() ####################

    @Test
    public void given_existemOrdens_when_buscaTodasAsOrdens_then_retornaListaDeOrdens() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findAllOrdensDeServico()).thenReturn(List.of(os));

        var resultado = osUseCase.getAllOrdemDeServico();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    public void given_nenhumaOrdem_when_buscaTodasAsOrdens_then_retornaListaVazia() {
        when(osGateway.findAllOrdensDeServico()).thenReturn(Collections.emptyList());

        var resultado = osUseCase.getAllOrdemDeServico();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // #################### getOrdemDeServico(Long id) ####################

    @Test
    public void given_ordemExiste_when_buscaPorId_then_retornaOrdem() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));

        var resultado = osUseCase.getOrdemDeServico(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    public void given_ordemNaoExiste_when_buscaPorId_then_retornaVazio() {
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.empty());

        var resultado = osUseCase.getOrdemDeServico(1L);

        assertTrue(resultado.isEmpty());
    }

    // #################### getOrdemDeServicoPorClienteId(Long clienteId) ####################

    @Test
    public void given_ordemExiste_when_buscaPorClienteId_then_retornaOrdem() {
        var ordens = List.of(criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA));
        when(osGateway.findAllOrdensDeServicoClienteId(456L)).thenReturn(ordens);

        var resultado = osUseCase.getOrdensDeServicoPorClienteId(456L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getFirst().getId());
    }

    @Test
    public void given_ordemNaoExiste_when_buscaPorClienteId_then_retornaNulo() {
        when(osGateway.findAllOrdensDeServicoClienteId(456L)).thenReturn(Collections.emptyList());

        var resultado = osUseCase.getOrdensDeServicoPorClienteId(456L);

        assertTrue(resultado.isEmpty());
    }

    // #################### getOrdemDeServicoPorVeiculoId(Long veiculoId) ####################

    @Test
    public void given_ordemExiste_when_buscaPorVeiculoId_then_retornaOrdem() {
        var ordens = List.of(criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA));
        when(osGateway.findAllOrdensDeServicoVeiculoId(123L)).thenReturn(ordens);

        var resultado = osUseCase.getOrdensDeServicoPorVeiculoId(123L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getFirst().getId());
    }

    @Test
    public void given_ordemNaoExiste_when_buscaPorVeiculoId_then_retornaNulo() {
        when(osGateway.findAllOrdensDeServicoVeiculoId(123L)).thenReturn(Collections.emptyList());

        var resultado = osUseCase.getOrdensDeServicoPorVeiculoId(123L);

        assertTrue(resultado.isEmpty());
    }

    // #################### createOrdemDeServico(CreateOrdemDeServicoRequest) ####################

    @Test
    public void given_requestValida_when_criaOrdem_then_retornaOrdemCriada() {
        var request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Problema nos freios.");
        request.setIdCliente(456L);
        request.setIdVeiculo(123L);

        var veiculo = new Veiculo();
        veiculo.setId(123L);

        var cliente = new Cliente();
        cliente.setId(456L);
        cliente.setTipoPessoa(TipoPessoa.PF);

        var osSalva = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.saveOrdemDeServico(any())).thenReturn(osSalva);
        when(clienteGateway.findById(any())).thenReturn(Optional.of(cliente));
        when(veiculoGateway.findById(any())).thenReturn(Optional.of(veiculo));

        var resultado = osUseCase.createOrdemDeServico(request.getSolicitacao(), request.getIdCliente(), request.getIdVeiculo());

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Problema nos freios.", resultado.getSolicitacao());
        verify(historicoStatusOrdemDeServicoGateway).registrarStatus(1L, StatusOrdemDeServico.RECEBIDA, Instant.parse("2026-08-29T12:00:00Z"));
        verify(eventLogger).registrarOrdemCriada(1L, StatusOrdemDeServico.RECEBIDA, Instant.parse("2026-08-29T12:00:00Z"));
    }

    @Test
    public void given_clienteNaoExiste_when_criaOrdem_then_jogaExcecao() {
        var request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Problema nos freios.");
        request.setIdCliente(999L);
        request.setIdVeiculo(123L);

        when(clienteGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> osUseCase.createOrdemDeServico(request.getSolicitacao(), request.getIdCliente(), request.getIdVeiculo()));
        verify(osGateway, never()).saveOrdemDeServico(any());
        verify(eventLogger).registrarFalhaProcessamento(null, "creation", "BUSINESS_RULE_VIOLATION", Instant.parse("2026-08-29T12:00:00Z"), false);
    }

    @Test
    public void given_veiculoNaoExiste_when_criaOrdem_then_jogaExcecao() {
        var request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Problema nos freios.");
        request.setIdCliente(456L);
        request.setIdVeiculo(999L);

        var cliente = new Cliente();
        cliente.setId(456L);
        when(clienteGateway.findById(456L)).thenReturn(Optional.of(cliente));
        when(veiculoGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> osUseCase.createOrdemDeServico(request.getSolicitacao(), request.getIdCliente(), request.getIdVeiculo()));
        verify(osGateway, never()).saveOrdemDeServico(any());
    }

    @Test
    public void given_persistenceFailure_when_criaOrdem_then_emiteEventoTecnicoERelancaExcecao() {
        var cliente = new Cliente();
        cliente.setId(456L);
        var veiculo = new Veiculo();
        veiculo.setId(123L);
        when(clienteGateway.findById(456L)).thenReturn(Optional.of(cliente));
        when(veiculoGateway.findById(123L)).thenReturn(Optional.of(veiculo));
        when(osGateway.saveOrdemDeServico(any())).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class,
                () -> osUseCase.createOrdemDeServico("Problema nos freios.", 456L, 123L));

        verify(eventLogger).registrarFalhaProcessamento(null, "creation", "UNEXPECTED_ERROR", Instant.parse("2026-08-29T12:00:00Z"), true);
    }

    // #################### updateOrdemDeServico(Long id, UpdateOrdemDeServicoRequest) ####################

    @Test
    public void given_ordemExiste_when_atualizaOrdem_then_retornaOrdemAtualizada() {
        var request = new UpdateOrdemDeServicoRequest();
        request.setOrcamento(BigDecimal.valueOf(500));

        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.updateOrdemDeServico(1L, request.getOrcamento(), null);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(osGateway).updateOrdemDeServico(any());
    }

    @Test
    public void given_transicaoDeStatusValida_when_atualizaOrdem_then_retornaOrdemComStatusAtualizado() {
        var request = new UpdateOrdemDeServicoRequest();
        request.setStatus(StatusOrdemDeServico.EM_DIAGNOSTICO);

        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.updateOrdemDeServico(1L, request.getOrcamento(), request.getStatus().toString());

        assertNotNull(resultado);
        verify(osGateway).updateOrdemDeServico(any());
        verify(eventLogger).registrarStatusAlterado(1L, StatusOrdemDeServico.RECEBIDA, StatusOrdemDeServico.EM_DIAGNOSTICO,
                3_600_000L, Instant.parse("2026-08-29T12:00:00Z"));
        verify(historicoStatusOrdemDeServicoGateway).registrarStatus(1L, StatusOrdemDeServico.EM_DIAGNOSTICO, Instant.parse("2026-08-29T12:00:00Z"));
    }

    @Test
    public void given_transicaoDeStatusInvalida_when_atualizaOrdem_then_jogaExcecao() {
        var request = new UpdateOrdemDeServicoRequest();
        request.setStatus(StatusOrdemDeServico.FINALIZADA);

        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));

        assertThrows(BadRequestException.class, () -> osUseCase.updateOrdemDeServico(1L, request.getOrcamento(), request.getStatus().toString()));
        verify(osGateway, never()).saveOrdemDeServico(any());
        verify(eventLogger).registrarFalhaProcessamento(1L, "status_transition", "INVALID_STATUS_TRANSITION", Instant.parse("2026-08-29T12:00:00Z"), false);
    }

    @Test
    public void given_ordemNaoExiste_when_atualizaOrdem_then_jogaExcecao() {
        var request = new UpdateOrdemDeServicoRequest();
        request.setOrcamento(BigDecimal.valueOf(500));

        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> osUseCase.updateOrdemDeServico(1L, request.getOrcamento(), null));
        verify(osGateway, never()).updateOrdemDeServico(any());
    }

    // #################### deleteOrdemDeServico(Long id) ####################

    @Test
    public void given_ordemExiste_when_deletaOrdem_then_retornaOrdemDeletada() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));

        var resultado = osUseCase.deleteOrdemDeServico(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(osGateway).deleteOrdemDeServico(os);
    }

    @Test
    public void given_ordemNaoExiste_when_deletaOrdem_then_jogaExcecao() {
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> osUseCase.deleteOrdemDeServico(1L));
        verify(osGateway, never()).deleteOrdemDeServico(any());
    }

    // #################### mudarParaStatus(Long id, StatusOrdemDeServico novoStatus) ####################

    @Test
    public void given_transicaoPermitida_when_mudaParaStatus_then_retornaSucesso() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.EM_DIAGNOSTICO.toString());

        assertTrue(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("EM_DIAGNOSTICO"));
        verify(osGateway).updateOrdemDeServico(any());
        verify(historicoStatusOrdemDeServicoGateway).registrarStatus(1L, StatusOrdemDeServico.EM_DIAGNOSTICO, Instant.parse("2026-08-29T12:00:00Z"));
        verify(eventLogger).registrarStatusAlterado(1L, StatusOrdemDeServico.RECEBIDA, StatusOrdemDeServico.EM_DIAGNOSTICO,
                3_600_000L, Instant.parse("2026-08-29T12:00:00Z"));
    }

    @Test
    public void given_transicaoParaAguardandoAprovacao_when_mudaParaStatus_then_retornaSucessoComEmailEnviado() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.EM_DIAGNOSTICO);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.AGUARDANDO_APROVACAO.toString());

        assertTrue(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("Email de aprovação foi enviado"));
    }

    @Test
    public void given_transicaoParaAprovada_when_mudaParaStatus_then_retornaSucessoComEmailRecebido() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.AGUARDANDO_APROVACAO);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.APROVADA.toString());

        assertTrue(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("Email com a aprovação"));
    }

    @Test
    public void given_transicaoParaEmExecucao_when_mudaParaStatus_then_retornaSucessoComMensagemGenerica() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.APROVADA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.EM_EXECUCAO.toString());

        assertTrue(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("mudou para 'EM_EXECUCAO'"));
    }

    @Test
    public void given_transicaoParaFinalizada_when_mudaParaStatus_then_retornaSucessoComMensagemGenerica() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.EM_EXECUCAO);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));
        when(osGateway.updateOrdemDeServico(any())).thenReturn(os);

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.FINALIZADA.toString());

        assertTrue(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("mudou para 'FINALIZADA'"));
    }

    @Test
    public void given_transicaoNaoPermitida_when_mudaParaStatus_then_retornaErro() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.RECEBIDA);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.AGUARDANDO_APROVACAO.toString());

        assertFalse(resultado.isSucesso());
        verify(osGateway, never()).updateOrdemDeServico(any());
        verify(eventLogger).registrarFalhaProcessamento(1L, "status_transition", "INVALID_STATUS_TRANSITION", Instant.parse("2026-08-29T12:00:00Z"), false);
    }

    @Test
    public void given_ordemJaNoStatus_when_mudaParaStatus_then_retornaErro() {
        var os = criarOrdemDeServico(1L, StatusOrdemDeServico.EM_DIAGNOSTICO);
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.of(os));

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.EM_DIAGNOSTICO.toString());

        assertFalse(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("já está no status"));
        verify(osGateway, never()).updateOrdemDeServico(any());
    }

    @Test
    public void given_ordemNaoEncontrada_when_mudaParaStatus_then_retornaErro() {
        when(osGateway.findOrdemDeServicoById(1L)).thenReturn(Optional.empty());

        var resultado = osUseCase.mudarParaStatus(1L, StatusOrdemDeServico.EM_DIAGNOSTICO.toString());

        assertFalse(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("não existe"));
        verify(osGateway, never()).updateOrdemDeServico(any());
        verify(eventLogger).registrarFalhaProcessamento(1L, "status_transition", "ORDER_NOT_FOUND", Instant.parse("2026-08-29T12:00:00Z"), false);
    }
}

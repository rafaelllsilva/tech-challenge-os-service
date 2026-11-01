package com.fiap.techchallenge.os.ordemservico.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.os.exception.BadRequestException;
import com.fiap.techchallenge.os.exception.NotFoundException;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.*;
import com.fiap.techchallenge.os.ordemservico.adapter.presenter.OrdemDeServicoMapper;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdemDeServicoController.class)
@Import(com.fiap.techchallenge.os.security.TestSecurityConfig.class)
@WithMockUser(roles = "GERENTE")
public class OrdemDeServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    @MockitoBean
    private OrdemDeServicoMapper osMapper;

    @MockitoBean
    private OrdemDeServicoUseCase osUseCase;

    @MockitoBean
    private ListagemOrdemDeServicoComparator listagemOrdemDeServicoComparator;

    private static final String CONTROLLER_PATH = "/api/ordemDeServico/";

    private OrdemDeServico criarOrdemDeServico(Long id) {
        OrdemDeServico dto = new OrdemDeServico();
        dto.setId(id);
        dto.setStatus(StatusOrdemDeServico.RECEBIDA);
        dto.setSolicitacao("Problema nos freios.");
        dto.setValorTotal(BigDecimal.valueOf(0L, 2));
        return dto;
    }

    private OrdemDeServicoResponse converterEmResponse(OrdemDeServico os) {
        return OrdemDeServicoResponse.builder()
                .id(os.getId())
                .status(os.getStatus().name())
                .valorTotal(os.getValorTotal())
                .solicitacao(os.getSolicitacao())
                .build();
    }

    private OrdemDeServicoSimplesResponse converterEmSimplesResponse(OrdemDeServico os) {
        return OrdemDeServicoSimplesResponse.builder()
                .id(os.getId())
                .status(os.getStatus().name())
                .valorTotal(os.getValorTotal())
                .solicitacao(os.getSolicitacao())
                .dataHoraCriacao(os.getDataHoraCriacao())
                .build();
    }

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
    }

    // GET /{id}

    @Test
    public void given_ordemDeServicoExiste_when_buscaPeloSeuId_then_retornaOrdemDeServico() throws Exception {
        var id = 1L;
        var os = criarOrdemDeServico(id);
        var url = CONTROLLER_PATH + "{id}";

        when(osUseCase.getOrdemDeServico(any())).thenReturn(Optional.of(os));
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(get(url, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void given_ordemDeServicoNaoExiste_when_buscaPeloSeuId_then_retornaNotFound() throws Exception {
        var url = CONTROLLER_PATH + "{id}";

        when(osUseCase.getOrdemDeServico(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(url, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // GET /

    @Test
    public void given_existemOrdensDeServico_when_buscaTodasAsOrdens_then_retornaLista() throws Exception {
        var os = criarOrdemDeServico(1L);
        when(osUseCase.getAllOrdemDeServico()).thenReturn(List.of(os));
        when(osMapper.toSimplesResponse(any())).thenReturn(converterEmSimplesResponse(os));

        mockMvc.perform(get(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void given_nenhumaOrdemDeServico_when_buscaTodasAsOrdens_then_retornaListaVazia() throws Exception {
        when(osUseCase.getAllOrdemDeServico()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // POST /

    @Test
    public void given_requestValida_when_criaOrdemDeServico_then_retornaCreated() throws Exception {
        var request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Problema nos freios.");
        request.setIdCliente(456L);
        request.setIdVeiculo(123L);
        var os = criarOrdemDeServico(1L);

        when(osUseCase.createOrdemDeServico(any(), any(), any())).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(post(CONTROLLER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // PUT /{id}

    @Test
    public void given_ordemDeServicoExiste_when_atualizaOrdemDeServico_then_retornaOrdemAtualizada() throws Exception {
        var id = 1L;
        var request = new UpdateOrdemDeServicoRequest();
        request.setSolicitacao("Troca de óleo.");
        var url = CONTROLLER_PATH + "{id}";
        var os = criarOrdemDeServico(id);

        when(osUseCase.updateOrdemDeServico(any(), any())).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(put(url, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void given_ordemDeServicoNaoExiste_when_atualizaOrdemDeServico_then_retornaNotFound() throws Exception {
        var request = new UpdateOrdemDeServicoRequest();
        request.setSolicitacao("Troca de óleo.");
        var url = CONTROLLER_PATH + "{id}";

        when(osUseCase.updateOrdemDeServico(any(), any())).thenReturn(null);

        mockMvc.perform(put(url, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // DELETE /{id}

    @Test
    public void given_ordemDeServicoExiste_when_deletaOrdemDeServico_then_retornaOrdemDeletada() throws Exception {
        var id = 1L;
        var url = CONTROLLER_PATH + "{id}";
        var os = criarOrdemDeServico(id);

        when(osUseCase.deleteOrdemDeServico(any())).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(delete(url, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    public void given_ordemDeServicoNaoExiste_when_deletaOrdemDeServico_then_retornaNotFound() throws Exception {
        var url = CONTROLLER_PATH + "{id}";

        when(osUseCase.deleteOrdemDeServico(any())).thenReturn(null);

        mockMvc.perform(delete(url, 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // POST {id}/aprovacao

    private static final String SEGREDO = "webhook-secret-key-123";

    @Test
    public void given_aprovado_when_aprovacao_then_chamaAprovarOrcamentoERetorna200() throws Exception {
        var os = criarOrdemDeServico(1L);
        when(osUseCase.aprovarOrcamento(1L)).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 1L)
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(osUseCase).aprovarOrcamento(1L);
    }

    @Test
    public void given_rejeitado_when_aprovacao_then_chamaRejeitarOrcamentoComMotivo() throws Exception {
        var os = criarOrdemDeServico(1L);
        when(osUseCase.rejeitarOrcamento(1L, "caro")).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 1L)
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": false, \"motivo\": \"caro\"}"))
                .andExpect(status().isOk());

        verify(osUseCase).rejeitarOrcamento(1L, "caro");
    }

    @Test
    public void given_segredoInvalido_when_aprovacao_then_retorna401() throws Exception {
        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 1L)
                        .header("X-Webhook-Secret", "errado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(osUseCase);
    }

    @Test
    public void given_transicaoInvalida_when_aprovacao_then_retorna400() throws Exception {
        when(osUseCase.aprovarOrcamento(1L)).thenThrow(new BadRequestException("transição inválida"));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 1L)
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_osInexistente_when_aprovacao_then_retorna404() throws Exception {
        when(osUseCase.aprovarOrcamento(9L)).thenThrow(new NotFoundException("não existe"));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 9L)
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void given_semCampoAprovado_when_aprovacao_then_retorna400() throws Exception {
        mockMvc.perform(post(CONTROLLER_PATH + "{id}/aprovacao", 1L)
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // POST {id}/cancelar

    @Test
    public void given_motivo_when_cancelar_then_chamaUseCaseERetorna200() throws Exception {
        var os = criarOrdemDeServico(1L);
        when(osUseCase.cancelarOrdemDeServico(1L, "desistiu")).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/cancelar", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"desistiu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void given_cancelamentoNaoPermitido_when_cancelar_then_retorna400() throws Exception {
        when(osUseCase.cancelarOrdemDeServico(1L, "x")).thenThrow(new BadRequestException("não pode"));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/cancelar", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_osInexistente_when_cancelar_then_retorna404() throws Exception {
        when(osUseCase.cancelarOrdemDeServico(9L, "x")).thenThrow(new NotFoundException("não existe"));

        mockMvc.perform(post(CONTROLLER_PATH + "{id}/cancelar", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void given_motivoVazio_when_cancelar_then_retorna400() throws Exception {
        mockMvc.perform(post(CONTROLLER_PATH + "{id}/cancelar", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    // POST transicao/paraEntregue/{id}

    @Test
    public void given_transicaoPermitida_when_entregue_then_retornaSucesso() throws Exception {
        when(osUseCase.entregar(1L)).thenReturn(new TransicaoDeStatusDTO(true, "Mudou para ENTREGUE"));

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEntregue/{id}", 1L)
                        .header("X-Webhook-Secret", SEGREDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_transicaoNaoPermitida_when_entregue_then_retornaBadRequest() throws Exception {
        when(osUseCase.entregar(1L)).thenReturn(new TransicaoDeStatusDTO(false, "Erro: transição não permitida"));

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEntregue/{id}", 1L)
                        .header("X-Webhook-Secret", SEGREDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    // GET {id}/historico

    @Test
    public void given_osExiste_when_historico_then_retornaListaMapeada() throws Exception {
        var item = new com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico(
                com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico.TipoItemHistorico.STATUS,
                "RECEBIDA", java.time.Instant.parse("2026-01-01T10:00:00Z"), null, null);
        when(osUseCase.consultarHistorico(1L)).thenReturn(List.of(item));
        when(osMapper.toHistoricoResponse(item)).thenReturn(HistoricoOrdemDeServicoResponse.builder()
                .tipo("STATUS").nome("RECEBIDA").ocorridoEm(item.ocorridoEm()).build());

        mockMvc.perform(get(CONTROLLER_PATH + "{id}/historico", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("STATUS"))
                .andExpect(jsonPath("$[0].nome").value("RECEBIDA"));
    }

    @Test
    public void given_osInexistente_when_historico_then_retorna404() throws Exception {
        when(osUseCase.consultarHistorico(9L)).thenThrow(new NotFoundException("não existe"));

        mockMvc.perform(get(CONTROLLER_PATH + "{id}/historico", 9L))
                .andExpect(status().isNotFound());
    }

    // GET /cliente/{clienteId}/ordens

    @Test
    public void given_ordemDeServicoExisteParaCliente_when_buscaPorClienteId_then_retornaOrdemDeServico() throws Exception {
        var clienteId = 456L;
        var os = List.of(criarOrdemDeServico(1L));
        var url = CONTROLLER_PATH + "cliente/{clienteId}/ordens";

        when(osUseCase.getOrdensDeServicoPorClienteId(clienteId)).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os.getFirst()));

        mockMvc.perform(get(url, clienteId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void given_ordemDeServicoNaoExisteParaCliente_when_buscaPorClienteId_then_retornaNotFound() throws Exception {
        var clienteId = 456L;
        var url = CONTROLLER_PATH + "cliente/{clienteId}/ordens";

        when(osUseCase.getOrdensDeServicoPorClienteId(clienteId)).thenReturn(null);

        mockMvc.perform(get(url, clienteId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // GET /veiculo/{veiculoId}/ordens

    @Test
    public void given_ordemDeServicoExisteParaVeiculo_when_buscaPorVeiculoId_then_retornaOrdemDeServico() throws Exception {
        var veiculoId = 123L;
        var os = List.of(criarOrdemDeServico(1L));
        var url = CONTROLLER_PATH + "veiculo/{veiculoId}/ordens";

        when(osUseCase.getOrdensDeServicoPorVeiculoId(veiculoId)).thenReturn(os);
        when(osMapper.toResponse(any())).thenReturn(converterEmResponse(os.getFirst()));

        mockMvc.perform(get(url, veiculoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void given_ordemDeServicoNaoExisteParaVeiculo_when_buscaPorVeiculoId_then_retornaNotFound() throws Exception {
        var veiculoId = 123L;
        var url = CONTROLLER_PATH + "veiculo/{veiculoId}/ordens";

        when(osUseCase.getOrdensDeServicoPorVeiculoId(veiculoId)).thenReturn(null);

        mockMvc.perform(get(url, veiculoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}

package com.fiap.techchallenge.os.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.os.adapter.controller.dto.*;
import com.fiap.techchallenge.os.adapter.presenter.OrdemDeServicoMapper;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoUseCase;
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
@Import(com.fiap.techchallenge.security.TestSecurityConfig.class)
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
        dto.setOrcamento(BigDecimal.valueOf(0L, 2));
        return dto;
    }

    private OrdemDeServicoResponse converterEmResponse(OrdemDeServico os) {
        return OrdemDeServicoResponse.builder()
                .id(os.getId())
                .status(os.getStatus().name())
                .orcamento(os.getOrcamento())
                .solicitacao(os.getSolicitacao())
                .build();
    }

    private OrdemDeServicoSimplesResponse converterEmSimplesResponse(OrdemDeServico os) {
        return OrdemDeServicoSimplesResponse.builder()
                .id(os.getId())
                .status(os.getStatus().name())
                .orcamento(os.getOrcamento())
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

        when(osUseCase.createOrdemDeServico(any(), any(), any(), any(), any())).thenReturn(os);
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
        request.setOrcamento(BigDecimal.valueOf(500L));
        var url = CONTROLLER_PATH + "{id}";
        var os = criarOrdemDeServico(id);

        when(osUseCase.updateOrdemDeServico(any(), any(), any())).thenReturn(os);
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
        request.setOrcamento(BigDecimal.valueOf(500L));
        var url = CONTROLLER_PATH + "{id}";

        when(osUseCase.updateOrdemDeServico(any(), any(), any())).thenReturn(null);

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

    // POST transicao/paraDiagnostico/{id}

    @Test
    public void given_transicaoPermitida_when_notificaMecanicoParaDiagnostico_then_retornaSucesso() throws Exception {
        var url = CONTROLLER_PATH + "transicao/paraDiagnostico/{id}";

        when(osUseCase.mudarParaStatus(any(), eq(StatusOrdemDeServico.EM_DIAGNOSTICO.toString())))
                .thenReturn(new TransicaoDeStatusDTO(true, "Mudou para EM_DIAGNOSTICO"));

        mockMvc.perform(post(url, 1L)
                .header("X-Webhook-Secret", "webhook-secret-key-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_transicaoNaoPermitida_when_notificaMecanicoParaDiagnostico_then_retornaBadRequest() throws Exception {
        var url = CONTROLLER_PATH + "transicao/paraDiagnostico/{id}";

        when(osUseCase.mudarParaStatus(any(), eq(StatusOrdemDeServico.EM_DIAGNOSTICO.toString())))
                .thenReturn(new TransicaoDeStatusDTO(false, "Erro: transição não permitida"));

        mockMvc.perform(post(url, 1L)
                .header("X-Webhook-Secret", "webhook-secret-key-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
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

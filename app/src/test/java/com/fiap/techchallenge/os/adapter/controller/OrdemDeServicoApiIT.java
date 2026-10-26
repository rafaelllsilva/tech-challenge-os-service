package com.fiap.techchallenge.os.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.techchallenge.os.adapter.controller.dto.OrdemDeServicoResponse;
import com.fiap.techchallenge.os.adapter.controller.dto.CreateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.adapter.controller.dto.UpdateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.usecase.OrdemDeServicoGateway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(value = "gerente", roles = {"GERENTE"})
public class OrdemDeServicoApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrdemDeServicoGateway osGateway;

    private static ObjectMapper objectMapper;

    private static final String CONTROLLER_PATH = OrdemDeServicoController.CONTROLLER_PATH;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private static final String aprovacaoClienteBody = """
                {"aprovado": true}
                """;

    @Test
    public void given_bancoComOrdens_when_getAllOrdemDeServico_then_retornaLista() throws Exception {
        mockMvc.perform(get(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void given_idExistente_when_getOrdemDeServicoById_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Troca de óleo");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        mockMvc.perform(get(CONTROLLER_PATH + osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(osId))
                .andExpect(jsonPath("$.solicitacao").value("Troca de óleo"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    public void given_idInexistente_when_getOrdemDeServicoById_then_retorna404() throws Exception {
        mockMvc.perform(get(CONTROLLER_PATH + "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void given_idClienteExistente_when_getOrdemDeServicoPorClienteId_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Alinhamento");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(CONTROLLER_PATH + "cliente/" + clienteId + "/ordens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cliente.id").value(clienteId));
    }

    @Test
    public void given_idClienteInexistente_when_getOrdemDeServicoPorClienteId_then_retorna404() throws Exception {
        mockMvc.perform(get(CONTROLLER_PATH + "cliente/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void given_idVeiculoExistente_when_getOrdemDeServicoPorVeiculoId_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Balanceamento");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(CONTROLLER_PATH + "veiculo/" + veiculoId + "/ordens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].veiculo.id").value(veiculoId));
    }

    @Test
    public void given_idVeiculoInexistente_when_getOrdemDeServicoPorVeiculoId_then_retorna404() throws Exception {
        mockMvc.perform(get(CONTROLLER_PATH + "veiculo/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void given_payloadValido_when_createOrdemDeServico_then_retorna201() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Primeira OS");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitacao").value("Primeira OS"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    public void given_clienteInexistente_when_createOrdemDeServico_then_retorna400() throws Exception {
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS inválida: Cliente inexistente.");
        request.setIdCliente(999999L);
        request.setIdVeiculo(veiculoId);

        mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_veiculoInexistente_when_createOrdemDeServico_then_retorna400() throws Exception {
        Long clienteId = getClienteId();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS inválida: Veículo inexistente.");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(999999L);

        mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_payloadValido_when_updateOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest createRequest = new CreateOrdemDeServicoRequest();
        createRequest.setSolicitacao("OS original");
        createRequest.setIdCliente(clienteId);
        createRequest.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        UpdateOrdemDeServicoRequest updateRequest = new UpdateOrdemDeServicoRequest();
        updateRequest.setOrcamento(BigDecimal.valueOf(500.00));

        mockMvc.perform(put(CONTROLLER_PATH + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento").value(500.00));
    }

    @Test
    public void given_idInexistente_when_updateOrdemDeServico_then_retorna400() throws Exception {
        UpdateOrdemDeServicoRequest request = new UpdateOrdemDeServicoRequest();
        request.setOrcamento(BigDecimal.valueOf(500.00));

        mockMvc.perform(put(CONTROLLER_PATH + "999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_idExistente_when_deleteOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para deletar");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        mockMvc.perform(delete(CONTROLLER_PATH + osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(osId));

        assertThat(osGateway.findOrdemDeServicoById(osId)).isEmpty();
    }

    @Test
    public void given_idInexistente_when_deleteOrdemDeServico_then_retorna400() throws Exception {
        mockMvc.perform(delete(CONTROLLER_PATH + "999999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_idValido_when_notificarMecanicoParaDiagnostico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para diagnóstico");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_transicaoInvalida_when_notificarMecanicoParaDiagnostico_then_retorna400() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para diagnóstico inválido");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Primeiro muda para EM_DIAGNOSTICO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Tenta mudar para EM_DIAGNOSTICO novamente (já está neste status)
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    public void given_idValidoETipoServicoExistente_when_adicionarServicosNaOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para adicionar serviços");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Muda para EM_DIAGNOSTICO para permitir adição de serviços
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Adiciona serviço (ID 1 é o tipo de serviço padrão nos testes)
        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos").isArray())
                .andExpect(jsonPath("$.servicos").isNotEmpty());
    }

    @Test
    public void given_idInvalido_when_adicionarServicosNaOrdemDeServico_then_retorna400() throws Exception {
        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_idValido_when_removerServicosDaOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para remover serviços");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Muda para EM_DIAGNOSTICO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Primeiro adiciona um serviço
        var requestAdicionar = new HashMap<String, List<Long>>();
        requestAdicionar.put("idTipoServicos", List.of(1L));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestAdicionar)))
                .andExpect(status().isOk());

        // Agora remove o serviço
        var requestRemover = new HashMap<String, List<Long>>();
        requestRemover.put("idTipoServicos", List.of(1L));

        mockMvc.perform(delete(CONTROLLER_PATH + "removerServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestRemover)))
                .andExpect(status().isOk());
    }

    @Test
    public void given_idInvalido_when_removerServicosDaOrdemDeServico_then_retorna400() throws Exception {
        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));

        mockMvc.perform(delete(CONTROLLER_PATH + "removerServicos/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_idValidoETipoPecaExistente_when_adicionarPecasNaOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para adicionar peças");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Muda para EM_DIAGNOSTICO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Adiciona peça (ID 1 é o tipo de peça padrão nos testes)
        var requestPecas = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 2);
        requestPecas.put("pecasParaAdd", List.of(pecaItem));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPecas)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pecas").isArray())
                .andExpect(jsonPath("$.pecas").isNotEmpty());
    }

    @Test
    public void given_idInvalido_when_adicionarPecasNaOrdemDeServico_then_retorna400() throws Exception {
        var requestPecas = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 2);
        requestPecas.put("pecasParaAdd", List.of(pecaItem));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPecas)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_idValido_when_removerPecasDaOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para remover peças");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Muda para EM_DIAGNOSTICO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Primeiro adiciona uma peça
        var requestAdicionar = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 2);
        requestAdicionar.put("pecasParaAdd", List.of(pecaItem));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestAdicionar)))
                .andExpect(status().isOk());

        // Agora remove a peça
        var requestRemover = new HashMap<String, List<Long>>();
        requestRemover.put("idTipoPecasParaRemover", List.of(1L));

        mockMvc.perform(delete(CONTROLLER_PATH + "removerPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestRemover)))
                .andExpect(status().isOk());
    }

    @Test
    public void given_idInvalido_when_removerPecasDaOrdemDeServico_then_retorna400() throws Exception {
        var requestPecas = new HashMap<String, List<Long>>();
        requestPecas.put("idTipoPecasParaRemover", List.of(1L));

        mockMvc.perform(delete(CONTROLLER_PATH + "removerPecas/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPecas)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void given_osEmDiagnosticoComOrcamento_when_enviarParaAprovacaoDoCliente_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para aprovação");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Muda para EM_DIAGNOSTICO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Adiciona serviço e peça para ter orçamento > 0
        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isOk());

        var requestPeca = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 1);
        requestPeca.put("pecasParaAdd", List.of(pecaItem));

        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPeca)))
                .andExpect(status().isOk());

        // Agora envia para aprovação
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_osSemOrcamento_when_enviarParaAprovacaoDoCliente_then_retorna400() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS sem orçamento");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Muda para EM_DIAGNOSTICO mas não adiciona serviços/peças
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Tenta enviar para aprovação sem orçamento
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    public void given_osAprovada_when_iniciarExecucaoDaOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para execução");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Completa fluxo até APROVADA
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Adiciona serviço e peça
        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isOk());

        var requestPeca = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 1);
        requestPeca.put("pecasParaAdd", List.of(pecaItem));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPeca)))
                .andExpect(status().isOk());

        // Envia para aprovação
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Aprova
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacaoOuRejeicaoCliente/" + osId)
                        .header("X-Webhook-Secret", "webhook-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isOk());

        // Inicia execução
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEmExecucao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_osNaoAprovada_when_iniciarExecucaoDaOrdemDeServico_then_retorna400() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS não aprovada");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Tenta iniciar execução sem estar aprovada
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEmExecucao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    public void given_osEmExecucao_when_finalizarExecucaoDaOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para finalizar");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Completa fluxo até EM_EXECUCAO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isOk());

        var requestPeca = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 1);
        requestPeca.put("pecasParaAdd", List.of(pecaItem));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPeca)))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacaoOuRejeicaoCliente/" + osId)
                        .header("X-Webhook-Secret", "webhook-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEmExecucao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Finaliza execução
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraFinalizada/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_osNaoEmExecucao_when_finalizarExecucaoDaOrdemDeServico_then_retorna400() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS não em execução");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Tenta finalizar sem estar em execução
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraFinalizada/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    public void given_osFinalizada_when_entregarOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para entregar");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Completa fluxo até FINALIZADA
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isOk());

        var requestPeca = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 1);
        requestPeca.put("pecasParaAdd", List.of(pecaItem));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPeca)))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacao/" + osId)
                .header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacaoOuRejeicaoCliente/" + osId)
                        .header("X-Webhook-Secret", "webhook-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEmExecucao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraFinalizada/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Entrega a OS
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEntregue/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_osNaoFinalizada_when_entregarOrdemDeServico_then_retorna400() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS não finalizada");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Tenta entregar sem estar finalizada
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEntregue/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    @Test
    public void given_osAguardandoAprovacao_when_aprovarOrdemDeServico_then_retorna200() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS para aprovar");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Completa fluxo até AGUARDANDO_APROVACAO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraDiagnostico/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        var requestServico = new HashMap<String, List<Long>>();
        requestServico.put("idTipoServicos", List.of(1L));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarServicos/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestServico)))
                .andExpect(status().isOk());

        var requestPeca = new HashMap<String, List<Map<String, Object>>>();
        var pecaItem = new HashMap<String, Object>();
        pecaItem.put("idTipoPeca", 1L);
        pecaItem.put("quantidade", 1);
        requestPeca.put("pecasParaAdd", List.of(pecaItem));
        mockMvc.perform(put(CONTROLLER_PATH + "adicionarPecas/" + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPeca)))
                .andExpect(status().isOk());

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacao/" + osId).header("X-Webhook-Secret", "webhook-secret-key-123"))
                .andExpect(status().isOk());

        // Aprova a OS
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacaoOuRejeicaoCliente/" + osId)
                        .header("X-Webhook-Secret", "webhook-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    public void given_osNaoAguardandoAprovacao_when_aprovarOrdemDeServico_then_retorna400() throws Exception {
        Long clienteId = getClienteId();
        Long veiculoId = getVeiculo();

        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("OS não aguardando aprovação");
        request.setIdCliente(clienteId);
        request.setIdVeiculo(veiculoId);

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long osId = getIdDeOrdemDeServicoCriada(resultado);

        // Tenta aprovar sem estar em AGUARDANDO_APROVACAO
        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraAprovacaoOuRejeicaoCliente/" + osId)
                        .header("X-Webhook-Secret", "webhook-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    // Helpers

    private Long getClienteId() {
        return 1L;
    }

    private Long getVeiculo() {
        return 1L;
    }

    private Long getIdDeOrdemDeServicoCriada(MvcResult resultado) throws Exception {
        OrdemDeServicoResponse os = objectMapper.readValue(resultado.getResponse().getContentAsString(), OrdemDeServicoResponse.class);
        return os.getId();
    }
}

package com.fiap.techchallenge.os.ordemservico.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.OrdemDeServicoResponse;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.CreateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.UpdateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoGateway;
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
import java.util.List;

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

    @Autowired
    private OutboxEventoRepository outboxRepository;

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
        updateRequest.setSolicitacao("OS editada");

        mockMvc.perform(put(CONTROLLER_PATH + osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitacao").value("OS editada"));
    }

    @Test
    public void given_idInexistente_when_updateOrdemDeServico_then_retorna400() throws Exception {
        UpdateOrdemDeServicoRequest request = new UpdateOrdemDeServicoRequest();
        request.setSolicitacao("OS editada");

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

    private static final String SEGREDO = "webhook-secret-key-123";

    private Long criarOs(String solicitacao) throws Exception {
        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao(solicitacao);
        request.setIdCliente(getClienteId());
        request.setIdVeiculo(getVeiculo());

        MvcResult resultado = mockMvc.perform(post(CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return getIdDeOrdemDeServicoCriada(resultado);
    }

    private Long criarOsAguardandoAprovacao(String solicitacao) throws Exception {
        Long osId = criarOs(solicitacao);
        definirOrcamento(osId, BigDecimal.valueOf(300.00));
        forcarStatus(osId, StatusOrdemDeServico.AGUARDANDO_APROVACAO);
        return osId;
    }

    private List<OutboxEventoEntity> eventosDaOs(Long osId) {
        return outboxRepository.findAll().stream()
                .filter(e -> String.valueOf(osId).equals(e.getCorrelationId()))
                .toList();
    }

    // POST {id}/aprovacao

    @Test
    public void given_osAguardandoAprovacao_when_aprovacaoAprovada_then_aguardandoPagamentoEPublicaEvento() throws Exception {
        Long osId = criarOsAguardandoAprovacao("OS para aprovar");

        mockMvc.perform(post(CONTROLLER_PATH + osId + "/aprovacao")
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"));

        assertThat(osGateway.findOrdemDeServicoById(osId).orElseThrow().getStatus())
                .isEqualTo(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);
        assertThat(eventosDaOs(osId)).extracting(OutboxEventoEntity::getTipo)
                .containsExactlyInAnyOrder("OS_CRIADA", "ORCAMENTO_APROVADO");
        assertThat(eventosDaOs(osId).stream().filter(e -> e.getTipo().equals("ORCAMENTO_APROVADO")).findFirst().orElseThrow()
                .getTopico()).isEqualTo("os-events");
    }

    @Test
    public void given_osAguardandoAprovacao_when_aprovacaoRejeitada_then_canceladaEPublicaEvento() throws Exception {
        Long osId = criarOsAguardandoAprovacao("OS para rejeitar");

        mockMvc.perform(post(CONTROLLER_PATH + osId + "/aprovacao")
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": false, \"motivo\": \"muito caro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        var evento = eventosDaOs(osId).stream().filter(e -> e.getTipo().equals("ORCAMENTO_REJEITADO")).findFirst().orElseThrow();
        assertThat(evento.getPayload()).contains("muito caro");
    }

    @Test
    public void given_osRecebida_when_aprovacao_then_retorna400SemEvento() throws Exception {
        Long osId = criarOs("OS sem orçamento");

        mockMvc.perform(post(CONTROLLER_PATH + osId + "/aprovacao")
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isBadRequest());

        assertThat(eventosDaOs(osId)).extracting(OutboxEventoEntity::getTipo).containsExactly("OS_CRIADA");
    }

    @Test
    public void given_segredoInvalido_when_aprovacao_then_retorna401() throws Exception {
        Long osId = criarOsAguardandoAprovacao("OS segredo");

        mockMvc.perform(post(CONTROLLER_PATH + osId + "/aprovacao")
                        .header("X-Webhook-Secret", "errado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void given_osInexistente_when_aprovacao_then_retorna404() throws Exception {
        mockMvc.perform(post(CONTROLLER_PATH + "999999/aprovacao")
                        .header("X-Webhook-Secret", SEGREDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprovacaoClienteBody))
                .andExpect(status().isNotFound());
    }

    // POST {id}/cancelar

    @Test
    public void given_osRecebida_when_cancelar_then_canceladaEPublicaOsCancelada() throws Exception {
        Long osId = criarOs("OS para cancelar");

        mockMvc.perform(post(CONTROLLER_PATH + osId + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"cliente desistiu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        var evento = eventosDaOs(osId).stream().filter(e -> e.getTipo().equals("OS_CANCELADA")).findFirst().orElseThrow();
        assertThat(evento.getPayload()).contains("cliente desistiu").contains("RECEBIDA");
    }

    @Test
    public void given_osAprovada_when_cancelar_then_retorna400SemEvento() throws Exception {
        Long osId = criarOs("OS aprovada");
        forcarStatus(osId, StatusOrdemDeServico.APROVADA);

        mockMvc.perform(post(CONTROLLER_PATH + osId + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"x\"}"))
                .andExpect(status().isBadRequest());

        assertThat(eventosDaOs(osId)).extracting(OutboxEventoEntity::getTipo).containsExactly("OS_CRIADA");
    }

    @Test
    public void given_osInexistente_when_cancelar_then_retorna404() throws Exception {
        mockMvc.perform(post(CONTROLLER_PATH + "999999/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"x\"}"))
                .andExpect(status().isNotFound());
    }

    // POST transicao/paraEntregue/{id}

    @Test
    public void given_osFinalizada_when_entregarOrdemDeServico_then_retorna200() throws Exception {
        Long osId = criarOs("OS para entregar");
        forcarStatus(osId, StatusOrdemDeServico.FINALIZADA);

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEntregue/" + osId).header("X-Webhook-Secret", SEGREDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        assertThat(osGateway.findOrdemDeServicoById(osId).orElseThrow().getStatus()).isEqualTo(StatusOrdemDeServico.ENTREGUE);
    }

    @Test
    public void given_osNaoFinalizada_when_entregarOrdemDeServico_then_retorna400() throws Exception {
        Long osId = criarOs("OS não finalizada");

        mockMvc.perform(post(CONTROLLER_PATH + "transicao/paraEntregue/" + osId).header("X-Webhook-Secret", SEGREDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false));
    }

    // Helpers

    private void definirOrcamento(Long osId, BigDecimal valorTotal) {
        var os = osGateway.findOrdemDeServicoById(osId).orElseThrow();
        os.setOrcamentoId("orc-" + osId);
        os.setValorTotal(valorTotal);
        osGateway.updateOrdemDeServico(os);
    }

    private void forcarStatus(Long osId, StatusOrdemDeServico status) {
        var os = osGateway.findOrdemDeServicoById(osId).orElseThrow();
        os.setStatus(status);
        osGateway.updateOrdemDeServico(os);
    }

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

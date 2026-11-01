package com.fiap.techchallenge.os.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.*;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import io.awspring.cloud.sns.core.SnsTemplate;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Passos da saga. O billing e o execution são simulados publicando no SNS do LocalStack; os eventos
 * publicados pelo os-service são lidos da fila de teste test-os-events.
 */
public class SagaSteps {

    private static final String FILA_TESTE = "test-os-events";
    private static final Duration ESPERA = Duration.ofSeconds(30);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SnsTemplate snsTemplate;
    @Autowired
    private SqsTemplate sqsTemplate;
    @Autowired
    private EventoSerializer serializer;
    @Value("${webhook.aprovacao-cliente.secret}")
    private String webhookSecret;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<EventEnvelope<?>> eventosPublicados = new ArrayList<>();
    private long osId;
    private int contadorPagamento;

    @Before
    @After
    public void esvaziarFilaDeTeste() {
        // Remove mensagens deixadas por outros cenários ou testes, para não vazarem entre eles.
        Optional<Message<String>> recebida;
        do {
            recebida = sqsTemplate.receive(from -> from.queue(FILA_TESTE).pollTimeout(Duration.ofMillis(500)), String.class);
        } while (recebida.isPresent());
    }

    @Dado("um cliente e um veículo cadastrados")
    public void clienteEVeiculoCadastrados() {
        // Usa o cliente 1 e o veículo 1 do seed (V008).
    }

    @Quando("abro uma ordem de serviço")
    public void abroUmaOrdemDeServico() throws Exception {
        String resposta = gerente(post("/api/ordemDeServico")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitacao\":\"Revisão completa\",\"idCliente\":1,\"idVeiculo\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        osId = mapper.readTree(resposta).get("id").asLong();
    }

    @Então("a OS fica com status {word}")
    public void aOsFicaComStatus(String esperado) {
        await().atMost(ESPERA).pollInSameThread().untilAsserted(() -> assertThat(statusAtual()).isEqualTo(esperado));
    }

    @Então("o evento {word} é publicado")
    public void oEventoEPublicado(String tipo) {
        assertThat(aguardarEvento(TipoEvento.valueOf(tipo))).isNotNull();
    }

    @Então("o evento OS_CANCELADA é publicado com motivo {word}")
    public void osCanceladaPublicadaComMotivo(String motivo) {
        OsCanceladaPayload payload = (OsCanceladaPayload) aguardarEvento(TipoEvento.OS_CANCELADA).payload();
        assertThat(payload.motivo()).isEqualTo(motivo);
    }

    @Quando("o billing publica ORCAMENTO_GERADO com valor {bigdecimal}")
    public void billingPublicaOrcamentoGerado(BigDecimal valor) {
        publicar("billing-events", TipoEvento.ORCAMENTO_GERADO, new OrcamentoGeradoPayload(id(), "orc-" + osId, valor));
    }

    @Quando("o cliente aprova o orçamento")
    public void clienteAprovaOOrcamento() throws Exception {
        mockMvc.perform(post("/api/ordemDeServico/" + osId + "/aprovacao")
                        .header("X-Webhook-Secret", webhookSecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\":true}"))
                .andExpect(status().isOk());
    }

    @Quando("o billing publica PAGAMENTO_CONFIRMADO")
    public void billingPublicaPagamentoConfirmado() {
        publicar("billing-events", TipoEvento.PAGAMENTO_CONFIRMADO,
                new PagamentoConfirmadoPayload(id(), "pag-" + (++contadorPagamento), new BigDecimal("350.5")));
    }

    @Quando("o billing publica PAGAMENTO_FALHOU")
    public void billingPublicaPagamentoFalhou() {
        publicar("billing-events", TipoEvento.PAGAMENTO_FALHOU,
                new PagamentoFalhouPayload(id(), "pag-" + (++contadorPagamento), "Cartão recusado"));
    }

    @Quando("o execution publica EXECUCAO_INICIADA")
    public void executionPublicaExecucaoIniciada() {
        publicar("execution-events", TipoEvento.EXECUCAO_INICIADA, new ExecucaoIniciadaPayload(id(), Instant.now()));
    }

    @Quando("o execution publica EXECUCAO_FINALIZADA")
    public void executionPublicaExecucaoFinalizada() {
        publicar("execution-events", TipoEvento.EXECUCAO_FINALIZADA, new ExecucaoFinalizadaPayload(id(), Instant.now()));
    }

    @Quando("o execution publica EXECUCAO_FALHOU")
    public void executionPublicaExecucaoFalhou() {
        publicar("execution-events", TipoEvento.EXECUCAO_FALHOU, new ExecucaoFalhouPayload(id(), "Peça indisponível"));
    }

    @Quando("entrego o veículo")
    public void entregoOVeiculo() throws Exception {
        mockMvc.perform(post("/api/ordemDeServico/transicao/paraEntregue/" + osId)
                .header("X-Webhook-Secret", webhookSecret)).andExpect(status().isOk());
    }

    @Quando("o orçamento é aprovado e o pagamento falha {int} vez(es)")
    public void aprovaEFalhaNVezes(int falhas) throws Exception {
        for (int i = 0; i < falhas; i++) {
            aOsFicaComStatus("AGUARDANDO_APROVACAO");
            clienteAprovaOOrcamento();
            aOsFicaComStatus("AGUARDANDO_PAGAMENTO");
            billingPublicaPagamentoFalhou();
            int tentativa = i + 1;
            if (tentativa < falhas) {
                aOsFicaComStatus("AGUARDANDO_APROVACAO");
            }
        }
    }

    @Então("o histórico de status da OS é {string}")
    public void historicoDeStatus(String esperado) throws Exception {
        String corpo = gerente(get("/api/ordemDeServico/" + osId + "/historico"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> status = new ArrayList<>();
        mapper.readTree(corpo).forEach(item -> {
            if ("STATUS".equals(item.get("tipo").asText())) {
                status.add(item.get("nome").asText());
            }
        });
        assertThat(String.join(",", status)).isEqualTo(esperado.replace(" ", ""));
    }

    @Então("o histórico registra os eventos publicados {string} e os recebidos {string}")
    public void historicoRegistraEventos(String publicados, String recebidos) throws Exception {
        String corpo = gerente(get("/api/ordemDeServico/" + osId + "/historico"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> pub = new ArrayList<>();
        List<String> rec = new ArrayList<>();
        mapper.readTree(corpo).forEach(item -> {
            switch (item.get("tipo").asText()) {
                case "EVENTO_PUBLICADO" -> pub.add(item.get("nome").asText());
                case "EVENTO_RECEBIDO" -> rec.add(item.get("nome").asText());
                default -> { }
            }
        });
        assertThat(pub).containsExactlyInAnyOrder(publicados.replace(" ", "").split(","));
        assertThat(rec).containsExactlyInAnyOrder(recebidos.replace(" ", "").split(","));
    }

    private ResultActions gerente(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        return mockMvc.perform(request.with(user("gerente").roles("GERENTE")));
    }

    private String statusAtual() throws Exception {
        String corpo = gerente(get("/api/ordemDeServico/" + osId)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(corpo).get("status").asText();
    }

    private String id() {
        return String.valueOf(osId);
    }

    private void publicar(String topico, TipoEvento tipo, Object payload) {
        snsTemplate.send(topico, MessageBuilder.withPayload(
                serializer.serializar(EventEnvelope.novo(tipo, id(), payload, Clock.systemUTC()))).build());
    }

    /** Lê a fila de teste até achar um evento do tipo para esta OS (os já lidos ficam guardados). */
    private EventEnvelope<?> aguardarEvento(TipoEvento tipo) {
        await().atMost(ESPERA).untilAsserted(() -> {
            sqsTemplate.receive(from -> from.queue(FILA_TESTE).pollTimeout(Duration.ofSeconds(1)), String.class)
                    .map(m -> serializer.desserializar(m.getPayload()))
                    .filter(e -> id().equals(e.correlationId()))
                    .ifPresent(eventosPublicados::add);
            assertThat(eventosPublicados).anyMatch(e -> e.type() == tipo);
        });
        return eventosPublicados.stream().filter(e -> e.type() == tipo).findFirst().orElseThrow();
    }
}

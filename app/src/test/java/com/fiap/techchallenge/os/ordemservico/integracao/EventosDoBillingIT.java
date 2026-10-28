package com.fiap.techchallenge.os.ordemservico.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OrcamentoGeradoPayload;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// Sem @Transactional: o consumidor SQS roda em outra thread e precisa enxergar dados commitados.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(value = "gerente", roles = {"GERENTE"})
class EventosDoBillingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SnsTemplate snsTemplate;

    @Autowired
    private EventoSerializer serializer;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void orcamentoGeradoPublicadoNoBillingEventsLevaAOsParaAguardandoAprovacao() throws Exception {
        String resposta = mockMvc.perform(post("/api/ordemDeServico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitacao\":\"Freios\",\"idCliente\":1,\"idVeiculo\":1}"))
                .andReturn().getResponse().getContentAsString();
        long osId = mapper.readTree(resposta).get("id").asLong();

        var evento = EventEnvelope.novo(TipoEvento.ORCAMENTO_GERADO, String.valueOf(osId),
                new OrcamentoGeradoPayload(String.valueOf(osId), "orc-it-1", new BigDecimal("420.5")), Clock.systemUTC());
        snsTemplate.send("billing-events", MessageBuilder.withPayload(serializer.serializar(evento)).build());

        // pollInSameThread: o @WithMockUser vale só na thread do teste
        await().atMost(Duration.ofSeconds(30)).pollInSameThread().untilAsserted(() -> {
            String corpo = mockMvc.perform(get("/api/ordemDeServico/" + osId)).andReturn().getResponse().getContentAsString();
            JsonNode os = mapper.readTree(corpo);
            assertThat(os.get("status").asText()).isEqualTo("AGUARDANDO_APROVACAO");
            assertThat(os.get("orcamentoId").asText()).isEqualTo("orc-it-1");
            assertThat(os.get("valorTotal").decimalValue()).isEqualByComparingTo("420.5");
        });
    }
}

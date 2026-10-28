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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// Sem @Transactional: o consumidor SQS roda em outra thread e precisa enxergar dados commitados.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(value = "gerente", roles = {"GERENTE"})
class HistoricoApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SnsTemplate snsTemplate;

    @Autowired
    private EventoSerializer serializer;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void historicoMostraStatusEEventosPublicadosERecebidosEmOrdemCronologica() throws Exception {
        String resposta = mockMvc.perform(post("/api/ordemDeServico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitacao\":\"Histórico\",\"idCliente\":1,\"idVeiculo\":1}"))
                .andReturn().getResponse().getContentAsString();
        long osId = mapper.readTree(resposta).get("id").asLong();

        var evento = EventEnvelope.novo(TipoEvento.ORCAMENTO_GERADO, String.valueOf(osId),
                new OrcamentoGeradoPayload(String.valueOf(osId), "orc-hist", new BigDecimal("99.9")), Clock.systemUTC());
        snsTemplate.send("billing-events", MessageBuilder.withPayload(serializer.serializar(evento)).build());

        // pollInSameThread: o @WithMockUser vale só na thread do teste
        await().atMost(Duration.ofSeconds(30)).pollInSameThread().untilAsserted(() -> {
            JsonNode historico = mapper.readTree(mockMvc.perform(get("/api/ordemDeServico/" + osId + "/historico"))
                    .andReturn().getResponse().getContentAsString());
            List<String> itens = new ArrayList<>();
            historico.forEach(i -> itens.add(i.get("tipo").asText() + ":" + i.get("nome").asText()));
            assertThat(itens).containsExactly("STATUS:RECEBIDA", "EVENTO_PUBLICADO:OS_CRIADA",
                    "EVENTO_RECEBIDO:ORCAMENTO_GERADO", "STATUS:AGUARDANDO_APROVACAO");
            assertThat(historico.get(1).get("detalhes").asText()).contains("Histórico");
            assertThat(historico.get(2).get("eventId").asText()).isEqualTo(evento.eventId().toString());
            assertThat(historico.get(2).get("detalhes").asText()).contains("orc-hist");
        });
    }

    @Test
    void historicoDeOsInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/ordemDeServico/999999/historico"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }
}

package com.fiap.techchallenge.os.ordemservico.integracao;

import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.OsCriadaPayload;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoEntity;
import com.fiap.techchallenge.os.mensageria.framework.persistence.OutboxEventoRepository;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.CreateOrdemDeServicoRequest;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.OrdemDeServicoResponse;
import com.fiap.techchallenge.os.mensageria.framework.persistence.StatusOutbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sem @Transactional: o commit real é necessário para o relay enxergar a outbox.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(value = "gerente", roles = {"GERENTE"})
class OrdemDeServicoSagaIT {

    private static final String FILA_TESTE = "test-os-events";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OutboxEventoRepository outboxRepository;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private EventoSerializer serializer;

    @Test
    void postDaOsGravaOsCriadaNaOutboxEEleChegaNaFilaDeTeste() throws Exception {
        CreateOrdemDeServicoRequest request = new CreateOrdemDeServicoRequest();
        request.setSolicitacao("Barulho na suspensão");
        request.setIdCliente(1L);
        request.setIdVeiculo(1L);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        MvcResult resultado = mockMvc.perform(post("/api/ordemDeServico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String osId = String.valueOf(mapper.readValue(resultado.getResponse().getContentAsString(),
                OrdemDeServicoResponse.class).getId());

        List<OutboxEventoEntity> naOutbox = outboxRepository.findAll().stream()
                .filter(e -> osId.equals(e.getCorrelationId())).toList();
        assertThat(naOutbox).hasSize(1);
        assertThat(naOutbox.get(0).getTipo()).isEqualTo("OS_CRIADA");
        assertThat(naOutbox.get(0).getTopico()).isEqualTo("os-events");

        String id = naOutbox.get(0).getId().toString();
        AtomicReference<Message<String>> encontrada = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Optional<Message<String>> recebida = sqsTemplate.receive(
                    from -> from.queue(FILA_TESTE).pollTimeout(Duration.ofSeconds(1)), String.class);
            recebida.filter(m -> m.getPayload().contains(id)).ifPresent(encontrada::set);
            assertThat(encontrada.get()).isNotNull();
        });

        EventEnvelope<?> envelope = serializer.desserializar(encontrada.get().getPayload());
        assertThat(envelope.type()).isEqualTo(TipoEvento.OS_CRIADA);
        assertThat(envelope.correlationId()).isEqualTo(osId);
        OsCriadaPayload payload = (OsCriadaPayload) envelope.payload();
        assertThat(payload.osId()).isEqualTo(osId);
        assertThat(payload.clienteId()).isEqualTo("1");
        assertThat(payload.veiculoId()).isEqualTo("1");
        assertThat(payload.solicitacao()).isEqualTo("Barulho na suspensão");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(outboxRepository.findById(naOutbox.get(0).getId()).orElseThrow().getStatus())
                        .isEqualTo(StatusOutbox.ENVIADO));
    }
}

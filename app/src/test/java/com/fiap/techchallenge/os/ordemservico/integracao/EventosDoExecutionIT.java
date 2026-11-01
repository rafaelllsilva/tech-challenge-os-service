package com.fiap.techchallenge.os.ordemservico.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge.os.mensageria.contrato.EventEnvelope;
import com.fiap.techchallenge.os.mensageria.contrato.EventoSerializer;
import com.fiap.techchallenge.os.mensageria.contrato.TipoEvento;
import com.fiap.techchallenge.os.mensageria.contrato.payload.ExecucaoFinalizadaPayload;
import com.fiap.techchallenge.os.mensageria.contrato.payload.ExecucaoIniciadaPayload;
import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.framework.persistence.OrdemDeServicoStatusHistoricoEntity;
import com.fiap.techchallenge.os.ordemservico.framework.persistence.OrdemDeServicoStatusHistoricoRepository;
import com.fiap.techchallenge.os.ordemservico.usecase.HistoricoStatusOrdemDeServicoGateway;
import com.fiap.techchallenge.os.ordemservico.usecase.OrdemDeServicoGateway;
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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// Sem @Transactional: o consumidor SQS roda em outra thread e precisa enxergar dados commitados.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(value = "gerente", roles = {"GERENTE"})
class EventosDoExecutionIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SnsTemplate snsTemplate;

    @Autowired
    private EventoSerializer serializer;

    @Autowired
    private OrdemDeServicoGateway osGateway;

    @Autowired
    private HistoricoStatusOrdemDeServicoGateway historicoGateway;

    @Autowired
    private OrdemDeServicoStatusHistoricoRepository historicoRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void execucaoIniciadaEFinalizadaLevamAOsAteFinalizadaComHistoricoCompleto() throws Exception {
        String resposta = mockMvc.perform(post("/api/ordemDeServico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitacao\":\"Revisão\",\"idCliente\":1,\"idVeiculo\":1}"))
                .andReturn().getResponse().getContentAsString();
        long osId = mapper.readTree(resposta).get("id").asLong();

        // Prepara a OS como se o fluxo de orçamento e pagamento já tivesse acontecido.
        var os = osGateway.findOrdemDeServicoById(osId).orElseThrow();
        os.setStatus(StatusOrdemDeServico.APROVADA);
        osGateway.updateOrdemDeServico(os);
        historicoGateway.registrarStatus(osId, StatusOrdemDeServico.APROVADA, Instant.now().plusMillis(1));

        publicarNoExecutionEvents(EventEnvelope.novo(TipoEvento.EXECUCAO_INICIADA, String.valueOf(osId),
                new ExecucaoIniciadaPayload(String.valueOf(osId), Instant.now()), Clock.systemUTC()));
        aguardarStatus(osId, StatusOrdemDeServico.EM_EXECUCAO);

        publicarNoExecutionEvents(EventEnvelope.novo(TipoEvento.EXECUCAO_FINALIZADA, String.valueOf(osId),
                new ExecucaoFinalizadaPayload(String.valueOf(osId), Instant.now()), Clock.systemUTC()));
        aguardarStatus(osId, StatusOrdemDeServico.FINALIZADA);

        List<StatusOrdemDeServico> historico = historicoRepository.findAll().stream()
                .filter(h -> h.getIdOrdemDeServico().equals(osId))
                .sorted(Comparator.comparing(OrdemDeServicoStatusHistoricoEntity::getAlteradoEm)
                        .thenComparing(OrdemDeServicoStatusHistoricoEntity::getId))
                .map(OrdemDeServicoStatusHistoricoEntity::getStatus)
                .toList();
        assertThat(historico).containsExactly(StatusOrdemDeServico.RECEBIDA, StatusOrdemDeServico.APROVADA,
                StatusOrdemDeServico.EM_EXECUCAO, StatusOrdemDeServico.FINALIZADA);
    }

    private void publicarNoExecutionEvents(EventEnvelope<?> evento) {
        snsTemplate.send("execution-events", MessageBuilder.withPayload(serializer.serializar(evento)).build());
    }

    private void aguardarStatus(long osId, StatusOrdemDeServico esperado) {
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(osGateway.findOrdemDeServicoById(osId).orElseThrow().getStatus()).isEqualTo(esperado));
    }
}

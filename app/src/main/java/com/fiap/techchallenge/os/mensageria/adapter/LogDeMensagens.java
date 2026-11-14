package com.fiap.techchallenge.os.mensageria.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registra uma linha por mensagem consumida das filas ou publicada nos tópicos, no formato
 * {@code [PREFIXO] {json}}. O texto vai no campo "message" do log estruturado.
 */
@Slf4j
@Component
public class LogDeMensagens {

    static final String PREFIXO_SQS = "[SQS LISTENER]";
    static final String PREFIXO_SNS = "[SNS PUBLISHER]";
    static final String OPERACAO_CONSUMO = "processar_evento";
    static final String OPERACAO_PUBLICACAO = "publicar_evento";

    private static final Set<String> RESULTADOS_INFO = Set.of("confirmado", "duplicado", "publicado");

    private final JsonMapper jsonMapper;
    private final String servico;

    public LogDeMensagens(JsonMapper jsonMapper, @Value("${spring.application.name}") String servico) {
        this.jsonMapper = jsonMapper;
        this.servico = servico;
    }

    public void consumida(String correlationId, String eventId, String eventType, String outcome, String message) {
        registrar(PREFIXO_SQS, OPERACAO_CONSUMO, correlationId, eventId, eventType, outcome, message);
    }

    public void publicada(String correlationId, String eventId, String eventType, String outcome, String message) {
        registrar(PREFIXO_SNS, OPERACAO_PUBLICACAO, correlationId, eventId, eventType, outcome, message);
    }

    private void registrar(String prefixo, String operacao, String correlationId, String eventId, String eventType,
                           String outcome, String message) {
        Map<String, Object> campos = new LinkedHashMap<>();
        campos.put("service", servico);
        campos.put("correlationId", correlationId);
        campos.put("orderId", idDaOs(correlationId));
        campos.put("eventId", eventId);
        campos.put("eventType", eventType);
        campos.put("operation", operacao);
        campos.put("outcome", outcome);
        campos.put("message", message);
        campos.values().removeIf(valor -> valor == null);

        String linha = prefixo + " " + jsonMapper.writeValueAsString(campos);
        if (RESULTADOS_INFO.contains(outcome)) {
            log.info(linha);
        } else {
            log.warn(linha);
        }
    }

    // O correlationId é sempre o id da OS; se vier outra coisa, o orderId é omitido.
    private static Long idDaOs(String correlationId) {
        try {
            return correlationId == null ? null : Long.valueOf(correlationId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String descricaoDoConsumo(String eventType) {
        return switch (eventType) {
            case "ORCAMENTO_GERADO" -> "Orçamento registrado, OS aguardando aprovação";
            case "ORCAMENTO_REJEITADO" -> "Rejeição do orçamento registrada, OS cancelada";
            case "PAGAMENTO_CONFIRMADO" -> "Pagamento confirmado, OS aprovada";
            case "PAGAMENTO_FALHOU" -> "Falha de pagamento tratada";
            case "PAGAMENTO_ESTORNADO" -> "Estorno de pagamento registrado no histórico da OS";
            case "DIAGNOSTICO_CONCLUIDO" -> "Diagnóstico concluído registrado no histórico da OS";
            case "ESTOQUE_RESERVADO" -> "Reserva de estoque registrada no histórico da OS";
            case "EXECUCAO_INICIADA" -> "Execução iniciada, OS em execução";
            case "EXECUCAO_FINALIZADA" -> "Execução finalizada, OS finalizada";
            case "EXECUCAO_FALHOU" -> "Falha na execução tratada, OS cancelada";
            default -> "Evento " + eventType + " processado";
        };
    }

    static String descricaoDaPublicacao(String eventType) {
        return switch (eventType) {
            case "OS_CRIADA" -> "Abertura da OS publicada";
            case "ORCAMENTO_APROVADO" -> "Aprovação do orçamento publicada";
            case "ORCAMENTO_REJEITADO" -> "Rejeição do orçamento publicada";
            case "OS_CANCELADA" -> "Cancelamento da OS publicado";
            default -> "Evento " + eventType + " publicado";
        };
    }
}

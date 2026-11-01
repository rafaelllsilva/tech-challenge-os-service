package com.fiap.techchallenge.os.mensageria.contrato;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventoSerializer {

    private final JsonMapper jsonMapper;

    public EventoSerializer(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String serializar(EventEnvelope<?> envelope) {
        return jsonMapper.writeValueAsString(envelope);
    }

    public String serializarPayload(Object payload) {
        return jsonMapper.writeValueAsString(payload);
    }

    public EventEnvelope<?> desserializar(String json) {
        JsonNode raiz = jsonMapper.readTree(json);
        TipoEvento tipo = resolverTipo(raiz.path("type").asString(null));
        Object payload = jsonMapper.treeToValue(raiz.path("payload"), tipo.tipoPayload());
        return new EventEnvelope<>(
                UUID.fromString(raiz.path("eventId").asString()),
                tipo,
                raiz.path("version").asInt(EventEnvelope.VERSAO_ATUAL),
                Instant.parse(raiz.path("occurredAt").asString()),
                raiz.path("correlationId").asString(null),
                payload
        );
    }

    private TipoEvento resolverTipo(String valor) {
        if (valor == null) {
            throw new EventoDesconhecidoException(null);
        }
        try {
            return TipoEvento.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new EventoDesconhecidoException(valor);
        }
    }
}

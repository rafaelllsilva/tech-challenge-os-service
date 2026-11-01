package com.fiap.techchallenge.os.ordemservico.adapter.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item da linha do tempo da OS: mudança de status ou evento da saga")
public class HistoricoOrdemDeServicoResponse {

    @Schema(description = "Origem do item", allowableValues = {"STATUS", "EVENTO_PUBLICADO", "EVENTO_RECEBIDO"}, example = "EVENTO_RECEBIDO")
    private String tipo;

    @Schema(description = "Nome do status ou tipo do evento", example = "ORCAMENTO_GERADO")
    private String nome;

    @Schema(description = "Instante em que o status mudou ou o evento ocorreu")
    private Instant ocorridoEm;

    @Schema(description = "Identificador do evento (somente para eventos)")
    private UUID eventId;

    @Schema(description = "Payload do evento em JSON (somente para eventos)", example = "{\"osId\":\"42\",\"orcamentoId\":\"orc-1\",\"valorTotal\":350.5}")
    private String detalhes;
}

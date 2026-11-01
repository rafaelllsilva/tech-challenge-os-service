package com.fiap.techchallenge.os.ordemservico.adapter.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AprovacaoOrcamentoRequest {

    @NotNull
    private Boolean aprovado;

    /** Motivo informado pelo cliente; usado quando o orçamento é rejeitado. */
    private String motivo;
}

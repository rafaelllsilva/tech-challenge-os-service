package com.fiap.techchallenge.os.adapter.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WebhookAprovacaoRequest {

    @NotNull
    private Boolean aprovado;
}

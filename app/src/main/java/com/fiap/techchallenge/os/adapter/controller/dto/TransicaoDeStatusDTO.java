package com.fiap.techchallenge.os.adapter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TransicaoDeStatusDTO {
    private boolean sucesso;
    private String mensagem;
}

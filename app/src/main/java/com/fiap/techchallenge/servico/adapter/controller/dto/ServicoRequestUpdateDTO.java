package com.fiap.techchallenge.servico.adapter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServicoRequestUpdateDTO {

    private Long tipoServicoId;

    private Long ordemDeServicoId;
}

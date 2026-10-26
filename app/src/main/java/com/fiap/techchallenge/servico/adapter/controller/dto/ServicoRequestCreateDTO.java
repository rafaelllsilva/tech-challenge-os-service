package com.fiap.techchallenge.servico.adapter.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServicoRequestCreateDTO {

    @NotNull(message = "O id do tipo de serviço é obrigatório.")
    private Long tipoServicoId;

    @NotNull(message = "O id da ordem de serviço é obrigatório.")
    private Long ordemDeServicoId;
}

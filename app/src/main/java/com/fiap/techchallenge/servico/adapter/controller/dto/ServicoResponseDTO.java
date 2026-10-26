package com.fiap.techchallenge.servico.adapter.controller.dto;

import com.fiap.techchallenge.servico.domain.ServicoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServicoResponseDTO {

    private Long id;

    private TipoServicoDTO tipoServico;

    private ServicoStatus status;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private Long ordemDeServicoId;
}

package com.fiap.techchallenge.os.adapter.controller.dto;

import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateOrdemDeServicoRequest {
    private BigDecimal orcamento;
    private StatusOrdemDeServico status;
}

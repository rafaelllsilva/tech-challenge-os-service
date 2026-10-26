package com.fiap.techchallenge.os.adapter.controller.dto;

import com.fiap.techchallenge.cliente.adapter.controller.dto.ClienteResponse;
import com.fiap.techchallenge.peca.adapter.controller.dto.PecaResponse;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoResponseDTO;
import com.fiap.techchallenge.veiculo.adapter.controller.dto.VeiculoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServicoResponse {
    private Long id;
    private String status;
    private BigDecimal orcamento;
    private String solicitacao;
    private LocalDateTime dataHoraCriacao;
    private ClienteResponse cliente;
    private VeiculoResponse veiculo;
    private List<ServicoResponseDTO> servicos;
    private List<PecaResponse> pecas;
}

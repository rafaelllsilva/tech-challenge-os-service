package com.fiap.techchallenge.os.ordemservico.adapter.presenter;

import com.fiap.techchallenge.os.cliente.adapter.controller.ClienteMapper;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.OrdemDeServicoSimplesResponse;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.OrdemDeServicoResponse;
import com.fiap.techchallenge.os.veiculo.adapter.controller.VeiculoWebMapper;
import org.springframework.stereotype.Component;

@Component
public class OrdemDeServicoMapper {

    private final ClienteMapper clienteMapper;
    private final VeiculoWebMapper veiculoMapper;

    public OrdemDeServicoMapper(ClienteMapper clienteMapper, VeiculoWebMapper veiculoMapper) {
        this.clienteMapper = clienteMapper;
        this.veiculoMapper = veiculoMapper;
    }

    public OrdemDeServicoResponse toResponse(OrdemDeServico os) {
        return OrdemDeServicoResponse.builder()
                .id(os.getId())
                .status(os.getStatus().name())
                .orcamento(os.getOrcamento())
                .solicitacao(os.getSolicitacao())
                .dataHoraCriacao(os.getDataHoraCriacao())
                .cliente(clienteMapper.toResponse(os.getCliente()))
                .veiculo(veiculoMapper.toResponse(os.getVeiculo()))
                .build();
    }

    public OrdemDeServicoSimplesResponse toSimplesResponse(OrdemDeServico os) {
        return OrdemDeServicoSimplesResponse.builder()
                .id(os.getId())
                .status(os.getStatus().name())
                .orcamento(os.getOrcamento())
                .solicitacao(os.getSolicitacao())
                .dataHoraCriacao(os.getDataHoraCriacao())
                .build();
    }
}

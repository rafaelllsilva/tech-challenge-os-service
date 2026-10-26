package com.fiap.techchallenge.os.adapter.presenter;

import com.fiap.techchallenge.cliente.adapter.controller.ClienteMapper;
import com.fiap.techchallenge.os.adapter.controller.dto.OrdemDeServicoSimplesResponse;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.adapter.controller.dto.OrdemDeServicoResponse;
import com.fiap.techchallenge.peca.adapter.controller.PecaMapper;
import com.fiap.techchallenge.servico.adapter.controller.mapper.ServicoMapper;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.veiculo.adapter.controller.VeiculoWebMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class OrdemDeServicoMapper {

    private final ClienteMapper clienteMapper;
    private final VeiculoWebMapper veiculoMapper;
    private final PecaMapper pecaMapper;

    public OrdemDeServicoMapper(ClienteMapper clienteMapper, VeiculoWebMapper veiculoMapper, PecaMapper pecaMapper) {
        this.clienteMapper = clienteMapper;
        this.veiculoMapper = veiculoMapper;
        this.pecaMapper = pecaMapper;
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
                .servicos(os.getServicos() != null && !os.getServicos().isEmpty() ? os.getServicos().stream().filter(s -> !ServicoStatus.DELETADO.equals(s.getStatus())).map(ServicoMapper::toServicoDTO).toList() : Collections.emptyList())
                .pecas(os.getPecas() != null && !os.getPecas().isEmpty() ? os.getPecas().stream().map(pecaMapper::toResponse).toList() : Collections.emptyList())
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

package com.fiap.techchallenge.os.ordemservico.adapter.presenter;

import com.fiap.techchallenge.os.cliente.adapter.controller.ClienteMapper;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.OrdemDeServicoSimplesResponse;
import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.HistoricoOrdemDeServicoResponse;
import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;
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
                .orcamentoId(os.getOrcamentoId())
                .valorTotal(os.getValorTotal())
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
                .valorTotal(os.getValorTotal())
                .solicitacao(os.getSolicitacao())
                .dataHoraCriacao(os.getDataHoraCriacao())
                .build();
    }

    public HistoricoOrdemDeServicoResponse toHistoricoResponse(ItemHistorico item) {
        return HistoricoOrdemDeServicoResponse.builder()
                .tipo(item.tipo().name())
                .nome(item.nome())
                .ocorridoEm(item.ocorridoEm())
                .eventId(item.eventId())
                .detalhes(item.detalhes())
                .build();
    }
}

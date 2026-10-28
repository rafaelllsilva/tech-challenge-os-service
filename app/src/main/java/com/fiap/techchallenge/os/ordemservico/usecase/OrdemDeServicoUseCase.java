package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.TransicaoDeStatusDTO;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;

import java.util.*;

public interface OrdemDeServicoUseCase {

    List<OrdemDeServico> getAllOrdemDeServico();

    Optional<OrdemDeServico> getOrdemDeServico(Long id);

    List<OrdemDeServico> getOrdensDeServicoPorClienteId(Long clienteId);

    List<OrdemDeServico> getOrdensDeServicoPorVeiculoId(Long veiculoId);

    OrdemDeServico createOrdemDeServico(String solicitacao, Long idCliente, Long idVeiculo);

    OrdemDeServico updateOrdemDeServico(Long id, String solicitacao);

    OrdemDeServico deleteOrdemDeServico(Long id);

    TransicaoDeStatusDTO mudarParaStatus(Long id, String novoStatus);
}

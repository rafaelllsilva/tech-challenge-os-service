package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.ordemservico.adapter.controller.dto.TransicaoDeStatusDTO;
import com.fiap.techchallenge.os.ordemservico.domain.OrdemDeServico;

import java.math.BigDecimal;
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

    // Reações aos eventos do billing (saga). OS inexistente é logada e ignorada.

    void aplicarOrcamentoGerado(Long id, String orcamentoId, BigDecimal valorTotal);

    void aplicarOrcamentoRejeitadoPeloBilling(Long id);

    void aplicarPagamentoConfirmado(Long id);

    void aplicarPagamentoFalhou(Long id, int maxTentativas);

    void aplicarPagamentoEstornado(Long id);
}

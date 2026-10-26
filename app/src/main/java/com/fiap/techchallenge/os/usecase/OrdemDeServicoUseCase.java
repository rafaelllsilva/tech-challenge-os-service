package com.fiap.techchallenge.os.usecase;

import com.fiap.techchallenge.os.adapter.controller.dto.TransicaoDeStatusDTO;
import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.inputdata.AdicionarPecaItemInputData;

import java.math.BigDecimal;
import java.util.*;

public interface OrdemDeServicoUseCase {

    List<OrdemDeServico> getAllOrdemDeServico();

    Optional<OrdemDeServico> getOrdemDeServico(Long id);

    List<OrdemDeServico> getOrdensDeServicoPorClienteId(Long clienteId);

    List<OrdemDeServico> getOrdensDeServicoPorVeiculoId(Long veiculoId);

    OrdemDeServico createOrdemDeServico(String solicitacao, Long idCliente, Long idVeiculo, List<AdicionarPecaItemInputData> pecas, List<Long> servicos);

    OrdemDeServico updateOrdemDeServico(Long id, BigDecimal orcamento, String status);

    OrdemDeServico deleteOrdemDeServico(Long id);

    TransicaoDeStatusDTO mudarParaStatus(Long id, String novoStatus);

    OrdemDeServico putServicosNaOrdemDeServico(Long id, List<Long> idTipoServicosParaAdicionar);

    OrdemDeServico deleteServicosDaOrdemDeServico(Long id, List<Long> idTipoServicosParaRemover);

    OrdemDeServico putPecasNaOrdemDeServico(Long id, List<AdicionarPecaItemInputData> addPecaItemsInputData);

    OrdemDeServico deletePecasDaOrdemDeServico(Long id, List<Long> idTipoPecasParaRemover);
}

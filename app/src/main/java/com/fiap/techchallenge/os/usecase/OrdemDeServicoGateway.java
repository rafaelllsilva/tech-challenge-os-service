package com.fiap.techchallenge.os.usecase;

import com.fiap.techchallenge.os.domain.OrdemDeServico;

import java.util.List;
import java.util.Optional;

public interface OrdemDeServicoGateway {

    List<OrdemDeServico> findAllOrdensDeServico();

    Optional<OrdemDeServico> findOrdemDeServicoById(Long id);

    List<OrdemDeServico> findAllOrdensDeServicoClienteId(Long clienteId);

    List<OrdemDeServico> findAllOrdensDeServicoVeiculoId(Long veiculoId);

    OrdemDeServico saveOrdemDeServico(OrdemDeServico os);

    OrdemDeServico updateOrdemDeServico(OrdemDeServico os);

    OrdemDeServico deleteOrdemDeServico(OrdemDeServico os);
}

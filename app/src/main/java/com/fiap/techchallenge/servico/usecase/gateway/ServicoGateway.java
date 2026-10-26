package com.fiap.techchallenge.servico.usecase.gateway;

import com.fiap.techchallenge.servico.domain.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoGateway {

    Optional<List<Servico>> findAllByOsId(Long idOS);

    boolean existsByTipoServicoId(Long idTipoServico);

    Servico save(Servico servico);

    List<Servico> findAll();

    Optional<Servico> findById(Long id);

    void delete(Long idServico);
}

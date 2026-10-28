package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.ordemservico.domain.StatusOrdemDeServico;

import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HistoricoStatusOrdemDeServicoGateway {

    void registrarStatus(Long idOrdemDeServico, StatusOrdemDeServico status, Instant alteradoEm);

    Optional<Instant> buscarInicioDoStatusAtual(Long idOrdemDeServico);

    List<ItemHistorico> listarPorOrdemDeServico(Long idOrdemDeServico);
}

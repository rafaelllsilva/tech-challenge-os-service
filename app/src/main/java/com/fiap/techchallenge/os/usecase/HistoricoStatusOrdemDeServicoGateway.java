package com.fiap.techchallenge.os.usecase;

import com.fiap.techchallenge.os.domain.StatusOrdemDeServico;

import java.time.Instant;
import java.util.Optional;

public interface HistoricoStatusOrdemDeServicoGateway {

    void registrarStatus(Long idOrdemDeServico, StatusOrdemDeServico status, Instant alteradoEm);

    Optional<Instant> buscarInicioDoStatusAtual(Long idOrdemDeServico);
}

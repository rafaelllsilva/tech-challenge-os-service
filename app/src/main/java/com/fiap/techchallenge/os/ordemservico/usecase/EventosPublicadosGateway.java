package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;

import java.util.List;

/** Eventos que o os-service publicou para a OS (outbox). */
public interface EventosPublicadosGateway {

    List<ItemHistorico> listarPorOrdemDeServico(Long idOrdemDeServico);
}

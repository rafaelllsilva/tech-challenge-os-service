package com.fiap.techchallenge.os.ordemservico.usecase;

import com.fiap.techchallenge.os.ordemservico.domain.ItemHistorico;

import java.util.List;

/** Eventos que o os-service consumiu e processou para a OS. */
public interface EventosRecebidosGateway {

    List<ItemHistorico> listarPorOrdemDeServico(Long idOrdemDeServico);
}

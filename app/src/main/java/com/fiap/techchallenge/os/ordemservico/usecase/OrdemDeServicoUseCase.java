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

    /** Cliente aprova o orçamento: AGUARDANDO_PAGAMENTO e publica ORCAMENTO_APROVADO. */
    OrdemDeServico aprovarOrcamento(Long id);

    /** Cliente rejeita o orçamento: CANCELADA e publica ORCAMENTO_REJEITADO. */
    OrdemDeServico rejeitarOrcamento(Long id, String motivo);

    /** Cancela a OS e publica OS_CANCELADA. */
    OrdemDeServico cancelarOrdemDeServico(Long id, String motivo);

    TransicaoDeStatusDTO entregar(Long id);

    // Reações aos eventos do billing (saga). OS inexistente é logada e ignorada.

    void aplicarOrcamentoGerado(Long id, String orcamentoId, BigDecimal valorTotal);

    void aplicarOrcamentoRejeitadoPeloBilling(Long id);

    void aplicarPagamentoConfirmado(Long id);

    void aplicarPagamentoFalhou(Long id, int maxTentativas);

    void aplicarPagamentoEstornado(Long id);

    // Reações aos eventos do execution (saga)

    void aplicarExecucaoIniciada(Long id);

    void aplicarExecucaoFinalizada(Long id);

    void aplicarExecucaoFalhou(Long id);

    /** Eventos que só ficam registrados (sem mudar o status), como DIAGNOSTICO_CONCLUIDO. */
    void registrarEventoInformativo(Long id, String tipoEvento);
}

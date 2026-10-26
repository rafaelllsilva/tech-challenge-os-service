package com.fiap.techchallenge.servico.usecase;

import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.usecase.command.ServicoCreateCommand;
import com.fiap.techchallenge.servico.usecase.command.ServicoUpdateCommand;

import java.util.List;

public interface ServicoUseCase {

    Servico updateServico(Long idServico, ServicoUpdateCommand servicoUpdateCommand);

    void finalizarServico(Long idServico);

    void iniciarServico(Long idServico);

    void deleteServico(Long idServico);

    Servico getServicoById(Long idServico);

    List<Servico> listaServicos(Long ordemDeServicoId);

    Servico creteServico(ServicoCreateCommand servicoCreateCommand);
}

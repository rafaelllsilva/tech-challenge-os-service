package com.fiap.techchallenge.servico.usecase;

import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.command.TipoServicoRequestsCommand;

import java.util.List;

public interface TipoServicoUseCase {

    TipoServico getById(Long id);

    List<TipoServico> listAll();

    List<TempoMedioServicoDTO> getTempoMedio(Long tipoServicoId);

    TipoServico create(TipoServicoRequestsCommand command);

    TipoServico update(Long id, TipoServicoRequestsCommand command);

    void delete(Long id);
}

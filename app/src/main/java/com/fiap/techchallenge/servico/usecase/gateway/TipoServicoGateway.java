package com.fiap.techchallenge.servico.usecase.gateway;

import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;

import java.util.List;
import java.util.Optional;

public interface TipoServicoGateway {

    List<TempoMedioServicoDTO> findTempoMedioPorTipoServico();

    Optional<TempoMedioServicoDTO> findTempoMedioPorTipoServicoId(Long tipoServicoId);

    TipoServico save(TipoServico tipoServico);

    List<TipoServico> findAll();

    Optional<TipoServico> findById(Long idTipoServico);

    void delete(Long idTipoServico);

    List<TipoServico> findAllById(List<Long> tipoServicoIds);
}

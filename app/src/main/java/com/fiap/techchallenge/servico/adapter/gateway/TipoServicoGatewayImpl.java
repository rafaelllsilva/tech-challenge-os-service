package com.fiap.techchallenge.servico.adapter.gateway;

import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.adapter.gateway.mapper.TipoServicoGatewayMapper;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.repository.TipoServicoRepository;
import com.fiap.techchallenge.servico.usecase.gateway.TipoServicoGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TipoServicoGatewayImpl implements TipoServicoGateway {

    private TipoServicoRepository tipoServicoRepository;

    public TipoServicoGatewayImpl(TipoServicoRepository tipoServicoRepository) {
        this.tipoServicoRepository = tipoServicoRepository;
    }


    @Override
    public List<TempoMedioServicoDTO> findTempoMedioPorTipoServico() {
        return tipoServicoRepository.findTempoMedioPorTipoServico();
    }

    @Override
    public Optional<TempoMedioServicoDTO> findTempoMedioPorTipoServicoId(Long tipoServicoId) {
        return tipoServicoRepository
                .findTempoMedioPorTipoServicoId(tipoServicoId);
    }

    @Override
    public TipoServico save(TipoServico tipoServico) {
        TipoServicoEntity entity = TipoServicoGatewayMapper.toEntity(tipoServico);
        return TipoServicoGatewayMapper.toDomain(tipoServicoRepository.save(entity));
    }

    @Override
    public List<TipoServico> findAll() {
        return tipoServicoRepository.findAll().stream()
                .map(TipoServicoGatewayMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TipoServico> findById(Long idTipoServico) {
        return tipoServicoRepository.findById(idTipoServico)
                .map(TipoServicoGatewayMapper::toDomain);
    }

    @Override
    public void delete(Long idTipoServico) {
        tipoServicoRepository.deleteById(idTipoServico);
    }

    @Override
    public List<TipoServico> findAllById(List<Long> tipoServicoIds) {
        return tipoServicoRepository.findAllById(tipoServicoIds).stream()
                .map(TipoServicoGatewayMapper::toDomain)
                .toList();
    }

    public static TipoServico toDomain(TipoServicoEntity entity) {
        TipoServico domain = new TipoServico();
        domain.setId(entity.getId());
        domain.setNome(entity.getNome());
        domain.setValor(entity.getValor());
        return domain;
    }

    public static TipoServicoEntity toEntity(TipoServico domain) {
        TipoServicoEntity entity = new TipoServicoEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setValor(domain.getValor());
        return entity;
    }
}
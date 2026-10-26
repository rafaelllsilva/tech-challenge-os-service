package com.fiap.techchallenge.servico.adapter.gateway.mapper;

import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;

public class TipoServicoGatewayMapper {
    public static TipoServicoEntity toEntity(TipoServico tipoServico) {
        TipoServicoEntity entity = new TipoServicoEntity();
        entity.setId(tipoServico.getId());
        entity.setNome(tipoServico.getNome());
        entity.setValor(tipoServico.getValor());
        return entity;
    }

    public static TipoServico toDomain(TipoServicoEntity entity) {
        TipoServico tipoServico = new TipoServico();
        tipoServico.setId(entity.getId());
        tipoServico.setNome(entity.getNome());
        tipoServico.setValor(entity.getValor());
        return tipoServico;
    }
}

package com.fiap.techchallenge.servico.adapter.gateway.mapper;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.servico.adapter.gateway.TipoServicoGatewayImpl;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import org.springframework.stereotype.Component;

@Component
public class ServicoGatewayMapper {

    public static ServicoEntity toEntity(Servico servico) {
        ServicoEntity entity = new ServicoEntity();
        entity.setId(servico.getId());
        entity.setDataInicio(servico.getDataInicio());
        entity.setDataFim(servico.getDataFim());
        entity.setStatus(servico.getStatus());
        entity.setTipoServico(TipoServicoGatewayImpl.toEntity(servico.getTipoServico()));

        if (servico.getOrdemDeServico() != null) {
            OrdemDeServicoEntity ose = new OrdemDeServicoEntity();
            ose.setId(servico.getOrdemDeServico().getId());
            entity.setOrdemDeServico(ose);
        }

        return entity;
    }

    public static Servico toDomain(ServicoEntity save) {
        Servico servico = new Servico();
        servico.setId(save.getId());
        servico.setDataInicio(save.getDataInicio());
        servico.setDataFim(save.getDataFim());
        servico.setStatus(save.getStatus());
        servico.setTipoServico(TipoServicoGatewayImpl.toDomain(save.getTipoServico()));

        if (save.getOrdemDeServico() != null) {
            OrdemDeServico os = new OrdemDeServico();
            os.setId(save.getOrdemDeServico().getId());
            servico.setOrdemDeServico(os);
        }

        return servico;
    }
}

package com.fiap.techchallenge.servico.adapter.controller.mapper;

import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoRequestDTO;
import com.fiap.techchallenge.servico.domain.TipoServico;
import com.fiap.techchallenge.servico.usecase.command.TipoServicoRequestsCommand;
import org.springframework.stereotype.Component;

@Component
public class TipoServicoMapper {
    public static TipoServicoDTO toDto(TipoServico entity) {
        return TipoServicoDTO.builder()
                        .id(entity.getId())
                        .nome(entity.getNome())
                        .valor(entity.getValor())
                        .build();
    }

    public static TipoServicoRequestsCommand toCommand(TipoServicoRequestDTO tipoServicoRequestDTO) {
        return new TipoServicoRequestsCommand(
                tipoServicoRequestDTO.getNome(),
                tipoServicoRequestDTO.getValor()
        );
    }
}

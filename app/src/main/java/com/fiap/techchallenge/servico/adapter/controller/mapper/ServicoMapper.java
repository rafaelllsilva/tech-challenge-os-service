package com.fiap.techchallenge.servico.adapter.controller.mapper;

import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestCreateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoRequestUpdateDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.ServicoResponseDTO;
import com.fiap.techchallenge.servico.adapter.controller.dto.TipoServicoDTO;
import com.fiap.techchallenge.servico.domain.Servico;
import com.fiap.techchallenge.servico.usecase.command.ServicoCreateCommand;
import com.fiap.techchallenge.servico.usecase.command.ServicoUpdateCommand;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public static ServicoResponseDTO toServicoDTO(Servico servico) {
        ServicoResponseDTO dto = ServicoResponseDTO.builder()
                .id(servico.getId())
                .dataInicio(servico.getDataInicio())
                .dataFim(servico.getDataFim())
                .status(servico.getStatus())
                .tipoServico(servico.getTipoServico() != null ? TipoServicoDTO.builder()
                        .id(servico.getTipoServico().getId())
                        .nome(servico.getTipoServico().getNome())
                        .valor(servico.getTipoServico().getValor())
                        .build() : null)
                .ordemDeServicoId(servico.getOrdemDeServico() != null ? servico.getOrdemDeServico().getId() : null)
                .build();
        return dto;
    }

    public static ServicoCreateCommand toCommandCreate(ServicoRequestCreateDTO dto) {
        return new ServicoCreateCommand(
                dto.getTipoServicoId(),
                dto.getOrdemDeServicoId()
        );
    }

    public static ServicoUpdateCommand toCommandUpdate(ServicoRequestUpdateDTO dto) {
        return new ServicoUpdateCommand(
                dto.getTipoServicoId(),
                dto.getOrdemDeServicoId()
        );
    }

}

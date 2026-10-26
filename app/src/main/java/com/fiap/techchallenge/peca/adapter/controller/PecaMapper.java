package com.fiap.techchallenge.peca.adapter.controller;

import com.fiap.techchallenge.peca.adapter.controller.dto.PecaResponse;
import com.fiap.techchallenge.peca.domain.Peca;
import org.springframework.stereotype.Component;

@Component
public class PecaMapper {

    public PecaResponse toResponse(Peca peca) {
        if (peca == null) return null;
        var dto = new PecaResponse();
        dto.setId(peca.getId());
        dto.setIdOs(peca.getOrdemDeServico() != null ? peca.getOrdemDeServico().getId().intValue() : null);
        dto.setIdTipoPeca(peca.getTipoPeca() != null ? peca.getTipoPeca().getId().intValue() : null);
        dto.setNomeTipoPeca(peca.getTipoPeca() != null ? peca.getTipoPeca().getNome() : null);
        dto.setQuantidade(peca.getQuantidade());
        return dto;
    }
}

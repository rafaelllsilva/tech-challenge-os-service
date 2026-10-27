package com.fiap.techchallenge.os.peca.adapter.controller;

import com.fiap.techchallenge.os.peca.adapter.controller.dto.TipoPecaResponse;
import com.fiap.techchallenge.os.peca.domain.TipoPeca;
import org.springframework.stereotype.Component;

@Component
public class TipoPecaMapper {

    public TipoPecaResponse toResponse(TipoPeca tipoPeca) {
        if (tipoPeca == null) return null;
        var dto = new TipoPecaResponse();
        dto.setId(tipoPeca.getId());
        dto.setNome(tipoPeca.getNome());
        dto.setValorUnitario(tipoPeca.getValorUnitario());
        dto.setQuantidadeEstoque(tipoPeca.getQuantidadeEstoque());
        return dto;
    }
}

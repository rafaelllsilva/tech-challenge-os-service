package com.fiap.techchallenge.peca.adapter.controller;

import com.fiap.techchallenge.peca.adapter.controller.dto.TipoPecaResponse;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TipoPecaMapperTest {

    private final TipoPecaMapper mapper = new TipoPecaMapper();

    @Test
    void toResponse_mapsAllFields() {
        TipoPeca tipoPeca = new TipoPeca();
        tipoPeca.setId(2L);
        tipoPeca.setNome("Filtro de Óleo");
        tipoPeca.setValorUnitario(BigDecimal.valueOf(50.0));
        tipoPeca.setQuantidadeEstoque(100);

        TipoPecaResponse dto = mapper.toResponse(tipoPeca);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getNome()).isEqualTo("Filtro de Óleo");
        assertThat(dto.getValorUnitario()).isEqualTo(BigDecimal.valueOf(50.0));
        assertThat(dto.getQuantidadeEstoque()).isEqualTo(100);
    }
}

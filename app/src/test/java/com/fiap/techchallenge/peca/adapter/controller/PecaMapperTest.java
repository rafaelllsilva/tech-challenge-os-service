package com.fiap.techchallenge.peca.adapter.controller;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.peca.adapter.controller.dto.PecaResponse;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.peca.domain.TipoPeca;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PecaMapperTest {

    private final PecaMapper mapper = new PecaMapper();

    @Test
    void toResponse_mapsAllFields() {
        TipoPeca tipoPeca = new TipoPeca();
        tipoPeca.setId(2L);
        tipoPeca.setNome("Filtro de Óleo");
        tipoPeca.setValorUnitario(BigDecimal.valueOf(50.0));
        tipoPeca.setQuantidadeEstoque(100);

        OrdemDeServico os = new OrdemDeServico();
        os.setId(3L);

        Peca peca = new Peca();
        peca.setId(1L);
        peca.setTipoPeca(tipoPeca);
        peca.setOrdemDeServico(os);
        peca.setQuantidade(4);

        PecaResponse dto = mapper.toResponse(peca);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getIdTipoPeca()).isEqualTo(2L);
        assertThat(dto.getIdOs()).isEqualTo(3L);
        assertThat(dto.getQuantidade()).isEqualTo(4);
    }
}

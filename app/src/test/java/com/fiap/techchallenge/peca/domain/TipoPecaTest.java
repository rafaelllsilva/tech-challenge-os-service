package com.fiap.techchallenge.peca.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TipoPecaTest {

    @Test
    void tipoPecaDomainProperties() {
        TipoPeca tipoPeca = new TipoPeca();
        tipoPeca.setId(2L);
        tipoPeca.setNome("Filtro");
        tipoPeca.setValorUnitario(BigDecimal.valueOf(25.0));
        tipoPeca.setQuantidadeEstoque(50);

        assertThat(tipoPeca.getId()).isEqualTo(2L);
        assertThat(tipoPeca.getNome()).isEqualTo("Filtro");
        assertThat(tipoPeca.getValorUnitario()).isEqualTo(BigDecimal.valueOf(25.0));
        assertThat(tipoPeca.getQuantidadeEstoque()).isEqualTo(50);
    }
}

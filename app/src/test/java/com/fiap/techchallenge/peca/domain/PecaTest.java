package com.fiap.techchallenge.peca.domain;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PecaTest {

    @Test
    void pecaDomainProperties() {
        TipoPeca tipoPeca = new TipoPeca();
        tipoPeca.setId(2L);
        tipoPeca.setNome("Pneu");
        tipoPeca.setValorUnitario(BigDecimal.valueOf(250.0));
        tipoPeca.setQuantidadeEstoque(10);

        // validar 
        OrdemDeServico os = new OrdemDeServico();
        os.setId(3L);

        Peca peca = new Peca();
        peca.setId(1L);
        peca.setTipoPeca(tipoPeca);
        peca.setOrdemDeServico(os);
        peca.setQuantidade(4);

        assertThat(peca.getId()).isEqualTo(1L);
        assertThat(peca.getTipoPeca().getNome()).isEqualTo("Pneu");
        assertThat(peca.getOrdemDeServico().getId()).isEqualTo(3L);
        assertThat(peca.getQuantidade()).isEqualTo(4);
    }
}

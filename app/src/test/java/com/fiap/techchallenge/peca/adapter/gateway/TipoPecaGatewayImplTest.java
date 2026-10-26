package com.fiap.techchallenge.peca.adapter.gateway;

import com.fiap.techchallenge.peca.domain.TipoPeca;
import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.repository.TipoPecaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoPecaGatewayImplTest {

    @Mock
    private TipoPecaRepository tipoPecaRepository;

    @InjectMocks
    private TipoPecaGatewayImpl tipoPecaGateway;

    private TipoPecaEntity criarTipoPecaEntity(Long id) {
        TipoPecaEntity jpa = new TipoPecaEntity();
        jpa.setId(id);
        jpa.setNome("Filtro de Óleo");
        jpa.setValorUnitario(BigDecimal.TEN);
        jpa.setQuantidadeEstoque(5);
        return jpa;
    }

    @Test
    void buscarTodosTiposPeca_retornaLista() {
        when(tipoPecaRepository.findAll()).thenReturn(List.of(criarTipoPecaEntity(1L)));

        List<TipoPeca> list = tipoPecaGateway.findAll();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void buscarTipoPecaPorId_quandoExiste_retornaDomain() {
        when(tipoPecaRepository.findById(1L)).thenReturn(Optional.of(criarTipoPecaEntity(1L)));

        Optional<TipoPeca> result = tipoPecaGateway.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void buscarTipoPecaPorId_quandoNaoExiste_retornaVazio() {
        when(tipoPecaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TipoPeca> result = tipoPecaGateway.findById(99L);

        assertThat(result).isEmpty();
    }
}

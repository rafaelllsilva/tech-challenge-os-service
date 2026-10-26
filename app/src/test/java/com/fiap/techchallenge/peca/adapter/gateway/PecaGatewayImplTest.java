package com.fiap.techchallenge.peca.adapter.gateway;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.peca.domain.Peca;
import com.fiap.techchallenge.peca.framework.persistence.entity.PecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.repository.PecaRepository;
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
class PecaGatewayImplTest {

    @Mock
    private PecaRepository pecaRepository;

    @InjectMocks
    private PecaGatewayImpl pecaGateway;

    private PecaEntity criarPecaEntity(Long id) {
        TipoPecaEntity tpJpa = new TipoPecaEntity();
        tpJpa.setId(2L);
        tpJpa.setNome("Pastilha de Freio");
        tpJpa.setValorUnitario(BigDecimal.TEN);
        tpJpa.setQuantidadeEstoque(10);

        OrdemDeServicoEntity os = new OrdemDeServicoEntity();
        os.setId(3L);

        PecaEntity jpa = new PecaEntity();
        jpa.setId(id);
        jpa.setQuantidade(5);
        jpa.setTipoPeca(tpJpa);
        jpa.setOrdemDeServico(os);
        return jpa;
    }

    @Test
    void buscarTodasPecas_retornaLista() {
        when(pecaRepository.findAll()).thenReturn(List.of(criarPecaEntity(1L)));

        List<Peca> list = pecaGateway.findAll();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void buscarPecaPorId_quandoExiste_retornaDomain() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(criarPecaEntity(1L)));

        Optional<Peca> result = pecaGateway.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void buscarPecaPorId_quandoNaoExiste_retornaVazio() {
        when(pecaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Peca> result = pecaGateway.findById(99L);

        assertThat(result).isEmpty();
    }
}

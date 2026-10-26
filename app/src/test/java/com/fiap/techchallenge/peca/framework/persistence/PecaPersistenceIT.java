package com.fiap.techchallenge.peca.framework.persistence;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.peca.framework.persistence.entity.PecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.repository.PecaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PecaPersistenceIT {

    @Autowired
    private PecaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void salvarPeca_geraIdAutomatico() {
        TipoPecaEntity tpJpa = new TipoPecaEntity();
        tpJpa.setNome("Pastilha de Freio");
        tpJpa.setValorUnitario(BigDecimal.TEN);
        tpJpa.setQuantidadeEstoque(10);
        entityManager.persist(tpJpa);

        // validar
        OrdemDeServicoEntity os = new OrdemDeServicoEntity();
        os.setId(1L);

        PecaEntity entity = new PecaEntity();
        entity.setQuantidade(5);
        entity.setTipoPeca(tpJpa);
        entity.setOrdemDeServico(os);

        PecaEntity saved = jpaRepository.save(entity);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void buscarPecaPorId_quandoExiste_retornaEntidade() {
        TipoPecaEntity tpJpa = new TipoPecaEntity();
        tpJpa.setNome("Filtro de Óleo");
        tpJpa.setValorUnitario(BigDecimal.valueOf(25.0));
        tpJpa.setQuantidadeEstoque(30);
        entityManager.persist(tpJpa);

        Optional<TipoPecaEntity> result = entityManager.createQuery(
                        "SELECT t FROM TipoPecaEntity t WHERE t.nome = :nome", TipoPecaEntity.class)
                .setParameter("nome", "Filtro de Óleo")
                .getResultList()
                .stream().findFirst();

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("Filtro de Óleo");
    }
}

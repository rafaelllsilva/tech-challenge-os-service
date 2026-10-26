package com.fiap.techchallenge.peca.framework.persistence;

import com.fiap.techchallenge.peca.framework.persistence.entity.TipoPecaEntity;
import com.fiap.techchallenge.peca.framework.persistence.repository.TipoPecaRepository;
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
class TipoPecaPersistenceIT {

    @Autowired
    private TipoPecaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void salvarTipoPeca_geraIdAutomatico() {
        TipoPecaEntity entity = new TipoPecaEntity();
        entity.setNome("Filtro de Ar");
        entity.setValorUnitario(BigDecimal.valueOf(45.0));
        entity.setQuantidadeEstoque(20);

        TipoPecaEntity saved = jpaRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
    }

    @Test
    void buscarTipoPecaPorId_quandoExiste_retornaEntidade() {
        TipoPecaEntity entity = new TipoPecaEntity();
        entity.setNome("Vela de Ignição");
        entity.setValorUnitario(BigDecimal.valueOf(30.0));
        entity.setQuantidadeEstoque(50);
        TipoPecaEntity saved = jpaRepository.save(entity);

        entityManager.flush();
        entityManager.clear();

        Optional<TipoPecaEntity> result = jpaRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("Vela de Ignição");
    }
}

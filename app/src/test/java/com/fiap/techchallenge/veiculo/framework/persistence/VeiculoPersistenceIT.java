package com.fiap.techchallenge.veiculo.framework.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class VeiculoPersistenceIT {

    @Autowired
    private VeiculoJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_assignsGeneratedId() {
        VeiculoEntity entity = new VeiculoEntity(null, "NEW1234", "Fiat", "Mobi", 2023);

        VeiculoEntity saved = jpaRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
    }

    @Test
    void save_persistsAllFields() {
        VeiculoEntity entity = new VeiculoEntity(null, "NEW1A23", "Renault", "Kwid", 2024);

        VeiculoEntity saved = jpaRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        VeiculoEntity reloaded = jpaRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getPlaca()).isEqualTo("NEW1A23");
        assertThat(reloaded.getMarca()).isEqualTo("Renault");
        assertThat(reloaded.getModelo()).isEqualTo("Kwid");
        assertThat(reloaded.getAno()).isEqualTo(2024);
    }

    @Test
    void findByPlaca_existingSeedRecord_returnsEntity() {
        Optional<VeiculoEntity> result = jpaRepository.findByPlaca("ABC-1234");

        assertThat(result).isPresent();
        assertThat(result.get().getMarca()).isEqualTo("Fiat");
        assertThat(result.get().getModelo()).isEqualTo("Uno");
    }

    @Test
    void findByPlaca_unknownPlaca_returnsEmpty() {
        Optional<VeiculoEntity> result = jpaRepository.findByPlaca("NOP3Q45");

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAtLeastSeedRecords() {
        List<VeiculoEntity> all = jpaRepository.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    void save_duplicatePlaca_violatesUniqueConstraint() {
        jpaRepository.saveAndFlush(new VeiculoEntity(null, "DUP1234", "Fiat", "Palio", 2015));

        assertThatThrownBy(() ->
                jpaRepository.saveAndFlush(new VeiculoEntity(null, "DUP1234", "Ford", "Ka", 2016)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_nullPlaca_violatesNotNullConstraint() {
        VeiculoEntity entity = new VeiculoEntity(null, null, "Fiat", "Uno", 2020);

        assertThatThrownBy(() -> jpaRepository.saveAndFlush(entity))
                .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    @Test
    void save_nullMarca_violatesNotNullConstraint() {
        VeiculoEntity entity = new VeiculoEntity(null, "NUL1234", null, "Uno", 2020);

        assertThatThrownBy(() -> jpaRepository.saveAndFlush(entity))
                .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    @Test
    void save_nullAno_violatesNotNullConstraint() {
        VeiculoEntity entity = new VeiculoEntity(null, "NUL1A23", "Fiat", "Uno", null);

        assertThatThrownBy(() -> jpaRepository.saveAndFlush(entity))
                .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    @Test
    void delete_removesRecordFromDatabase() {
        VeiculoEntity saved = jpaRepository.saveAndFlush(new VeiculoEntity(null, "DEL1234", "Honda", "Fit", 2019));
        Long id = saved.getId();

        jpaRepository.delete(saved);
        entityManager.flush();

        assertThat(jpaRepository.findById(id)).isEmpty();
    }
}

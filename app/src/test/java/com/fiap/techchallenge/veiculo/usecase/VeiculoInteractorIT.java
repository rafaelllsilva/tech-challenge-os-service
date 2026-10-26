package com.fiap.techchallenge.veiculo.usecase;

import com.fiap.techchallenge.exception.NotFoundException;
import com.fiap.techchallenge.veiculo.domain.Veiculo;
import com.fiap.techchallenge.veiculo.framework.persistence.VeiculoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class VeiculoInteractorIT {

    @Autowired
    private VeiculoUseCase veiculoUseCase;

    @Autowired
    private VeiculoJpaRepository jpaRepository;

    @Test
    void lifecycleById_createReadUpdateDelete() {
        Veiculo created = veiculoUseCase.create("UCI1234", "Toyota", "Etios", 2021);
        assertThat(created.getId()).isNotNull();

        Veiculo fetched = veiculoUseCase.getById(created.getId()).orElseThrow();
        assertThat(fetched.getPlaca()).isEqualTo("UCI1234");
        assertThat(fetched.getMarca()).isEqualTo("Toyota");

        Veiculo updated = veiculoUseCase.updateById(created.getId(), null, "Honda", "City", 2023);
        assertThat(updated.getMarca()).isEqualTo("Honda");
        assertThat(updated.getModelo()).isEqualTo("City");
        assertThat(updated.getAno()).isEqualTo(2023);
        assertThat(updated.getPlaca()).isEqualTo("UCI1234");

        Veiculo deleted = veiculoUseCase.deleteById(created.getId());
        assertThat(deleted).isNotNull();
        assertThat(veiculoUseCase.getById(created.getId())).isEmpty();
    }

    @Test
    void lifecycleByPlaca_createReadUpdateDelete() {
        veiculoUseCase.create("UCI1A23", "Toyota", "Etios", 2021);

        Veiculo fetched = veiculoUseCase.getByPlaca("UCI1A23").orElseThrow();
        assertThat(fetched.getMarca()).isEqualTo("Toyota");

        Veiculo updated = veiculoUseCase.updateByPlaca("UCI1A23", "Ford", "Fiesta", 2022);
        assertThat(updated.getMarca()).isEqualTo("Ford");
        assertThat(updated.getModelo()).isEqualTo("Fiesta");
        assertThat(updated.getAno()).isEqualTo(2022);
        assertThat(updated.getPlaca()).isEqualTo("UCI1A23");

        Veiculo deleted = veiculoUseCase.deleteByPlaca("UCI1A23");
        assertThat(deleted).isNotNull();
        assertThat(veiculoUseCase.getByPlaca("UCI1A23")).isEmpty();
    }

    @Test
    void updateById_partialRequest_preservesUntouchedFields() {
        Veiculo created = veiculoUseCase.create("PAT1234", "Toyota", "Etios", 2021);

        Veiculo updated = veiculoUseCase.updateById(created.getId(), null, "Volkswagen", null, null);

        assertThat(updated.getMarca()).isEqualTo("Volkswagen");
        assertThat(updated.getModelo()).isEqualTo("Etios");
        assertThat(updated.getAno()).isEqualTo(2021);
        assertThat(updated.getPlaca()).isEqualTo("PAT1234");
    }

    @Test
    void updateByPlaca_doesNotChangePlaca() {
        veiculoUseCase.create("KEE1234", "Toyota", "Etios", 2021);

        Veiculo updated = veiculoUseCase.updateByPlaca("KEE1234", "Nissan", null, null);

        assertThat(updated.getPlaca()).isEqualTo("KEE1234");
        assertThat(jpaRepository.findByPlaca("KEE1234")).isPresent();
    }

    @Test
    void create_duplicatePlaca_failsWithIntegrityViolation() {
        veiculoUseCase.create("DUP1A23", "Toyota", "Etios", 2021);

        assertThatThrownBy(() -> {
            veiculoUseCase.create("DUP1A23", "Toyota", "Etios", 2021);
            jpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updateById_missing_throwsNotFoundException() {
        long countBefore = jpaRepository.count();

        assertThatThrownBy(() -> veiculoUseCase.updateById(999_999L, null, null, null, null))
                .isInstanceOf(NotFoundException.class);
        assertThat(jpaRepository.count()).isEqualTo(countBefore);
    }

    @Test
    void deleteByPlaca_missing_throwsNotFoundException() {
        long countBefore = jpaRepository.count();

        assertThatThrownBy(() -> veiculoUseCase.deleteByPlaca("NOP3Q45"))
                .isInstanceOf(NotFoundException.class);
        assertThat(jpaRepository.count()).isEqualTo(countBefore);
    }

    @Test
    void getAll_reflectsInsertsInSameTransaction() {
        long countBefore = veiculoUseCase.getAll().size();

        veiculoUseCase.create("ALL1234", "Toyota", "Etios", 2021);
        veiculoUseCase.create("ALL1A23", "Toyota", "Etios", 2021);

        assertThat(veiculoUseCase.getAll()).hasSize((int) countBefore + 2);
    }
}

package com.fiap.techchallenge.veiculo.adapter.controller.dto.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AnoValidatorTest {

    private AnoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AnoValidator();
    }

    @Test
    void isValid_null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void isValid_currentYear_returnsTrue() {
        assertThat(validator.isValid(LocalDate.now().getYear(), null)).isTrue();
    }

    @Test
    void isValid_minimumYear_returnsTrue() {
        assertThat(validator.isValid(1886, null)).isTrue();
    }

    @Test
    void isValid_twoYearsAhead_returnsTrue() {
        assertThat(validator.isValid(LocalDate.now().getYear() + 2, null)).isTrue();
    }

    @Test
    void isValid_beforeMinimum_returnsFalse() {
        assertThat(validator.isValid(1885, null)).isFalse();
    }

    @Test
    void isValid_threeYearsAhead_returnsFalse() {
        assertThat(validator.isValid(LocalDate.now().getYear() + 3, null)).isFalse();
    }
}

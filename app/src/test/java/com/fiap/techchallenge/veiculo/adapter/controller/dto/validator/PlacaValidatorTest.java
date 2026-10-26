package com.fiap.techchallenge.veiculo.adapter.controller.dto.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlacaValidatorTest {

    private PlacaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PlacaValidator();
    }

    @Test
    void isValid_null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void isValid_oldFormat_withDash_returnsTrue() {
        assertThat(validator.isValid("ABC-1234", null)).isTrue();
    }

    @Test
    void isValid_oldFormat_withoutDash_returnsTrue() {
        assertThat(validator.isValid("ABC1234", null)).isTrue();
    }

    @Test
    void isValid_mercosulFormat_returnsTrue() {
        assertThat(validator.isValid("ABC1D23", null)).isTrue();
    }

    @Test
    void isValid_lowercase_returnsFalse() {
        assertThat(validator.isValid("abc1234", null)).isFalse();
    }

    @Test
    void isValid_tooShort_returnsFalse() {
        assertThat(validator.isValid("AB123", null)).isFalse();
    }

    @Test
    void isValid_wrongStructure_returnsFalse() {
        assertThat(validator.isValid("1234ABC", null)).isFalse();
    }
}

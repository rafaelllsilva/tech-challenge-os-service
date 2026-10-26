package com.fiap.techchallenge.veiculo.adapter.controller.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlacaValidator implements ConstraintValidator<ValidPlaca, String> {
    private static final String PLACA_PATTERN = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.matches(PLACA_PATTERN);
    }
}

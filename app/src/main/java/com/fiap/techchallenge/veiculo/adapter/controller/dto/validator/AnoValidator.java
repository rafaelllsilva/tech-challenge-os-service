package com.fiap.techchallenge.veiculo.adapter.controller.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AnoValidator implements ConstraintValidator<ValidAno, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true;
        int currentYear = LocalDate.now().getYear();
        return value >= 1886 && value <= (currentYear + 2);
    }
}

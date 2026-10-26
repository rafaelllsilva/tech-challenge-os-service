package com.fiap.techchallenge.veiculo.adapter.controller.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AnoValidator.class)
public @interface ValidAno {
    String message() default "Ano inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

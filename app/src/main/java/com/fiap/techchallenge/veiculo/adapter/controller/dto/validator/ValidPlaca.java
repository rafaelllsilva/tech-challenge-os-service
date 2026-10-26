package com.fiap.techchallenge.veiculo.adapter.controller.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlacaValidator.class)
@Documented
public @interface ValidPlaca {
    String message() default "Placa inválida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

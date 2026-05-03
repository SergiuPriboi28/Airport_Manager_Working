package com.example.airportManager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DifferentAirportsValidator.class)
@Documented
public @interface DifferentAirports {
    String message() default "Origin and destination airports must be different";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

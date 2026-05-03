package com.example.airportManager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ArrivalAfterDepartureValidator.class)
@Documented
public @interface ArrivalAfterDeparture {
    String message() default "Arrival time must be after departure time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

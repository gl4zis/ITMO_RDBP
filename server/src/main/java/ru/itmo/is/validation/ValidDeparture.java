package ru.itmo.is.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DepartureValidator.class)
public @interface ValidDeparture {
    String message() default "Invalid date range";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

package ru.itmo.is.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoomChangeValidator.class)
public @interface ValidRoomChange {
    String message() default "Invalid room change bid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

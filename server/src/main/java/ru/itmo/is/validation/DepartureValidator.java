package ru.itmo.is.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.itmo.is.dto.request.bid.DepartureRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DepartureValidator implements ConstraintValidator<ValidDeparture, DepartureRequest> {
    @Override
    public boolean isValid(DepartureRequest departureRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (departureRequest == null) {
            return false;
        }

        LocalDate from = departureRequest.getDayFrom();
        LocalDate to = departureRequest.getDayTo();
        LocalDate now = LocalDate.now();

        if (from.isBefore(now) || to.isBefore(now)) {
            return false;
        }

        long daysBetween = ChronoUnit.DAYS.between(from, to);
        return daysBetween >= 1 && daysBetween <= 60;
    }
}

package ru.itmo.is.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.DepartureRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DepartureValidatorTest {

    private DepartureValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        validator = new DepartureValidator();
    }

    @Test
    void testIsValid_WithNullRequest_ShouldReturnFalse() {
        assertFalse(validator.isValid(null, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithValidDates_ShouldReturnTrue() {
        DepartureRequest request = new DepartureRequest();
        request.setDayFrom(LocalDate.now().plusDays(1));
        request.setDayTo(LocalDate.now().plusDays(10));

        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithFromDateInPast_ShouldReturnFalse() {
        DepartureRequest request = new DepartureRequest();
        request.setDayFrom(LocalDate.now().minusDays(1));
        request.setDayTo(LocalDate.now().plusDays(10));

        assertFalse(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithToDateInPast_ShouldReturnFalse() {
        DepartureRequest request = new DepartureRequest();
        request.setDayFrom(LocalDate.now().plusDays(1));
        request.setDayTo(LocalDate.now().minusDays(1));

        assertFalse(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithSameDay_ShouldReturnFalse() {
        DepartureRequest request = new DepartureRequest();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        request.setDayFrom(tomorrow);
        request.setDayTo(tomorrow);

        assertFalse(validator.isValid(request, constraintValidatorContext)); // 0 days difference, needs >= 1
    }

    @Test
    void testIsValid_WithZeroDays_ShouldReturnFalse() {
        DepartureRequest request = new DepartureRequest();
        LocalDate date = LocalDate.now().plusDays(5);
        request.setDayFrom(date);
        request.setDayTo(date.minusDays(1));

        assertFalse(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithMoreThan60Days_ShouldReturnFalse() {
        DepartureRequest request = new DepartureRequest();
        request.setDayFrom(LocalDate.now().plusDays(1));
        request.setDayTo(LocalDate.now().plusDays(62)); // 61 days difference, > 60

        assertFalse(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithExactly60Days_ShouldReturnTrue() {
        DepartureRequest request = new DepartureRequest();
        request.setDayFrom(LocalDate.now().plusDays(1));
        request.setDayTo(LocalDate.now().plusDays(61)); // 60 days difference, exactly 60

        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithOneDayDifference_ShouldReturnTrue() {
        DepartureRequest request = new DepartureRequest();
        request.setDayFrom(LocalDate.now().plusDays(1));
        request.setDayTo(LocalDate.now().plusDays(2));

        assertTrue(validator.isValid(request, constraintValidatorContext));
    }
}


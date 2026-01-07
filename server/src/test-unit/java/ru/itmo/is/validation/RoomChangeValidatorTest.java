package ru.itmo.is.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.RoomChangeRequest;
import ru.itmo.is.dto.RoomType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RoomChangeValidatorTest {

    private RoomChangeValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        validator = new RoomChangeValidator();
    }

    @Test
    void testIsValid_WithNullRequest_ShouldReturnFalse() {
        assertFalse(validator.isValid(null, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithRoomToIdOnly_ShouldReturnTrue() {
        RoomChangeRequest request = new RoomChangeRequest();
        request.setRoomToId(1);
        request.setRoomPreferType(null);

        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithRoomPreferTypeOnly_ShouldReturnTrue() {
        RoomChangeRequest request = new RoomChangeRequest();
        request.setRoomToId(null);
        request.setRoomPreferType(RoomType.BLOCK);

        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithBothRoomToIdAndType_ShouldReturnFalse() {
        RoomChangeRequest request = new RoomChangeRequest();
        request.setRoomToId(1);
        request.setRoomPreferType(RoomType.BLOCK);

        assertFalse(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void testIsValid_WithNeitherRoomToIdNorType_ShouldReturnFalse() {
        RoomChangeRequest request = new RoomChangeRequest();
        request.setRoomToId(null);
        request.setRoomPreferType(null);

        assertFalse(validator.isValid(request, constraintValidatorContext));
    }
}


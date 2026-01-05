package ru.itmo.is.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.itmo.is.dto.RoomChangeRequest;
import ru.itmo.is.dto.RoomType;

public class RoomChangeValidator implements ConstraintValidator<ValidRoomChange, RoomChangeRequest> {
    @Override
    public boolean isValid(RoomChangeRequest roomChangeRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (roomChangeRequest == null) {
            return false;
        }

        Integer id = roomChangeRequest.getRoomToId();
        RoomType type = roomChangeRequest.getRoomPreferType();
        return (id == null) ^ (type == null);
    }
}

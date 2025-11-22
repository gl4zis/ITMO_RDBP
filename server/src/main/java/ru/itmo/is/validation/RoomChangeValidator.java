package ru.itmo.is.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.itmo.is.dto.request.bid.RoomChangeRequest;
import ru.itmo.is.entity.dorm.Room;

public class RoomChangeValidator implements ConstraintValidator<ValidRoomChange, RoomChangeRequest> {
    @Override
    public boolean isValid(RoomChangeRequest roomChangeRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (roomChangeRequest == null) {
            return false;
        }

        Integer id = roomChangeRequest.getRoomToId();
        Room.Type type = roomChangeRequest.getRoomPreferType();
        return (id == null) ^ (type == null);
    }
}

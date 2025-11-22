package ru.itmo.is.dto.request.bid;

import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.validation.ValidRoomChange;

@Getter
@Setter
@ValidRoomChange
public class RoomChangeRequest extends BidRequest {
    private Integer roomToId;
    private Room.Type roomPreferType;
}

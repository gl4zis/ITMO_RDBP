package ru.itmo.is.dto.response.bid;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.bid.RoomChangeBid;
import ru.itmo.is.entity.dorm.Room;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomChangeResponse extends BidResponse {
    Integer roomToId;
    Room.Type roomPreferType;

    public RoomChangeResponse(RoomChangeBid bid) {
        super(bid);
        Room roomTo = bid.getRoomTo();
        this.roomToId = roomTo == null ? null : roomTo.getId();
        this.roomPreferType = bid.getRoomPreferType();
    }
}

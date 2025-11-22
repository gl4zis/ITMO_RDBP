package ru.itmo.is.entity.bid;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.dorm.Room;

@Entity
@Getter
@Setter
public class RoomChangeBid extends Bid {

    public RoomChangeBid() {
        this.setType(Type.ROOM_CHANGE);
    }

    @ManyToOne
    @JoinColumn(name = "room_to_id")
    private Room roomTo;
    @Enumerated(EnumType.STRING)
    private Room.Type roomPreferType;
}

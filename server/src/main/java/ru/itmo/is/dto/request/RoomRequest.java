package ru.itmo.is.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.dorm.Room;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    int dormitoryId;
    int number;
    Room.Type type;
    int capacity;
    int floor;
    int cost;
}

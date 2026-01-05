package ru.itmo.is.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.is.dto.RoomResponse;
import ru.itmo.is.dto.RoomType;
import ru.itmo.is.entity.dorm.Room;

@Component
@RequiredArgsConstructor
public class RoomMapper {
    private final UserMapper userMapper;

    public RoomResponse roomToDto(Room room) {
        var response = new RoomResponse();
        response.setId(room.getId());
        response.setDormitoryId(room.getDormitory().getId());
        response.setNumber(room.getNumber());
        response.setType(mapRoomTypeToDto(room.getType()));
        response.setCapacity(room.getCapacity());
        response.setFloor(room.getFloor());
        response.setCost(room.getCost());
        response.setResidents(room.getResidents().stream().map(userMapper::mapUserResponse).toList());
        return response;
    }

    public Room.Type mapRoomTypeToModel(RoomType type) {
        return switch (type) {
            case AISLE -> Room.Type.AISLE;
            case BLOCK -> Room.Type.BLOCK;
        };
    }

    public RoomType mapRoomTypeToDto(Room.Type type) {
        return switch (type) {
            case AISLE -> RoomType.AISLE;
            case BLOCK -> RoomType.BLOCK;
        };
    }
}

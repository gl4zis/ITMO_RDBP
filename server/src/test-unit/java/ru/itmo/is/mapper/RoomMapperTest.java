package ru.itmo.is.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.RoomResponse;
import ru.itmo.is.dto.RoomType;
import ru.itmo.is.dto.UserResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.user.Resident;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomMapperTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RoomMapper roomMapper;

    private Room room;
    private Resident resident;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        Dormitory dormitory = new Dormitory();
        dormitory.setId(1);

        room = new Room();
        room.setId(1);
        room.setNumber(101);
        room.setDormitory(dormitory);
        room.setType(Room.Type.BLOCK);
        room.setCapacity(2);
        room.setFloor(1);
        room.setCost(1000);
        room.setResidents(new ArrayList<>());

        resident = new Resident();
        resident.setLogin("resident1");
        room.getResidents().add(resident);

        userResponse = new UserResponse();
        userResponse.setLogin("resident1");
    }

    @Test
    void testRoomToDto_ShouldMapCorrectly() {
        when(userMapper.mapUserResponse(any(Resident.class))).thenReturn(userResponse);

        RoomResponse result = roomMapper.roomToDto(room);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getDormitoryId());
        assertEquals(101, result.getNumber());
        assertEquals(RoomType.BLOCK, result.getType());
        assertEquals(2, result.getCapacity());
        assertEquals(1, result.getFloor());
        assertEquals(1000, result.getCost());
        assertEquals(1, result.getResidents().size());
        verify(userMapper).mapUserResponse(resident);
    }

    @Test
    void testMapRoomTypeToModel_ShouldMapAllTypes() {
        assertEquals(Room.Type.AISLE, roomMapper.mapRoomTypeToModel(RoomType.AISLE));
        assertEquals(Room.Type.BLOCK, roomMapper.mapRoomTypeToModel(RoomType.BLOCK));
    }

    @Test
    void testMapRoomTypeToDto_ShouldMapAllTypes() {
        assertEquals(RoomType.AISLE, roomMapper.mapRoomTypeToDto(Room.Type.AISLE));
        assertEquals(RoomType.BLOCK, roomMapper.mapRoomTypeToDto(Room.Type.BLOCK));
    }

    @Test
    void testRoomToDto_WithEmptyResidents_ShouldReturnEmptyList() {
        room.setResidents(new ArrayList<>());

        RoomResponse result = roomMapper.roomToDto(room);

        assertNotNull(result);
        assertTrue(result.getResidents().isEmpty());
        verify(userMapper, never()).mapUserResponse(any());
    }
}


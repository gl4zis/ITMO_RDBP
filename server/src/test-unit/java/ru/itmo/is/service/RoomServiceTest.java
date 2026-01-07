package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.RoomRequest;
import ru.itmo.is.dto.RoomResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.mapper.RoomMapper;
import ru.itmo.is.repository.DormitoryRepository;
import ru.itmo.is.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private DormitoryRepository dormitoryRepository;
    @Mock
    private RoomMapper roomMapper;
    @InjectMocks
    private RoomService roomService;

    private Room room;
    private RoomResponse roomResponse;
    private RoomRequest roomRequest;
    private Dormitory dormitory;
    private Resident resident;

    @BeforeEach
    void setUp() {
        dormitory = new Dormitory();
        dormitory.setId(1);

        room = new Room();
        room.setId(1);
        room.setNumber(101);
        room.setDormitory(dormitory);
        room.setResidents(new ArrayList<>());

        roomResponse = new RoomResponse();
        roomResponse.setId(1);
        roomResponse.setNumber(101);

        roomRequest = new RoomRequest();
        roomRequest.setDormitoryId(1);
        roomRequest.setNumber(101);
        roomRequest.setCapacity(2);
        roomRequest.setFloor(1);
        roomRequest.setCost(1000);

        resident = new Resident();
        resident.setLogin("resident1");
        resident.setRoom(room);
    }

    @Test
    void testGetAllRooms_ShouldReturnList() {
        when(roomRepository.findAllByOrderById()).thenReturn(List.of(room));
        when(roomMapper.roomToDto(room)).thenReturn(roomResponse);

        List<RoomResponse> result = roomService.getAllRooms();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetRoom_WhenExists_ShouldReturnRoom() {
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomMapper.roomToDto(room)).thenReturn(roomResponse);

        RoomResponse result = roomService.getRoom(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testGetRoom_WhenNotExists_ShouldThrowNotFoundException() {
        when(roomRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            roomService.getRoom(1);
        });
    }

    @Test
    void testGetForResident_ShouldReturnAvailableRooms() {
        Room otherRoom = new Room();
        otherRoom.setId(2);
        otherRoom.setDormitory(dormitory);
        otherRoom.setResidents(new ArrayList<>());
        otherRoom.setCapacity(2);
        
        when(userService.getCurrentResidentOrThrow()).thenReturn(resident);
        when(roomRepository.getInDormitory(1)).thenReturn(List.of(room, otherRoom));
        when(roomMapper.roomToDto(otherRoom)).thenReturn(roomResponse);

        List<RoomResponse> result = roomService.getForResident();

        assertNotNull(result);
        // Should filter out the resident's current room (id=1), so only otherRoom should be in result
        assertEquals(1, result.size());
    }

    @Test
    void testAddRoom_WithValidData_ShouldSaveRoom() {
        when(roomRepository.getByDormitoryIdAndNumber(1, 101)).thenReturn(Optional.empty());
        when(dormitoryRepository.findById(1)).thenReturn(Optional.of(dormitory));

        roomService.addRoom(roomRequest);

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testAddRoom_WhenRoomExists_ShouldThrowBadRequestException() {
        when(roomRepository.getByDormitoryIdAndNumber(1, 101)).thenReturn(Optional.of(room));

        assertThrows(BadRequestException.class, () -> {
            roomService.addRoom(roomRequest);
        });
    }

    @Test
    void testAddRoom_WhenDormitoryNotExists_ShouldThrowNotFoundException() {
        when(roomRepository.getByDormitoryIdAndNumber(1, 101)).thenReturn(Optional.empty());
        when(dormitoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            roomService.addRoom(roomRequest);
        });
    }

    @Test
    void testDeleteRoom_WhenExistsAndNoResidents_ShouldDeleteRoom() {
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1);

        verify(roomRepository).delete(room);
    }

    @Test
    void testDeleteRoom_WhenHasResidents_ShouldThrowBadRequestException() {
        room.getResidents().add(resident);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));

        assertThrows(BadRequestException.class, () -> {
            roomService.deleteRoom(1);
        });
    }

    @Test
    void testDeleteRoom_WhenNotExists_ShouldThrowNotFoundException() {
        when(roomRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            roomService.deleteRoom(1);
        });
    }
}


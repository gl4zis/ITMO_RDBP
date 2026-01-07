package ru.itmo.is.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Test
    void testGetByTypeAndDormitoryId_ShouldReturnRoomsOfType() {
        // Given
        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);

        Room blockRoom1 = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .withType(Room.Type.BLOCK)
                .withCapacity(2)
                .build();
        roomRepository.save(blockRoom1);

        Room blockRoom2 = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(102)
                .withType(Room.Type.BLOCK)
                .withCapacity(2)
                .build();
        roomRepository.save(blockRoom2);

        Room aisleRoom = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(201)
                .withType(Room.Type.AISLE)
                .withCapacity(4)
                .build();
        roomRepository.save(aisleRoom);

        flushAndClear();

        // When
        List<Room> blockRooms = roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, dormitory.getId());
        List<Room> aisleRooms = roomRepository.getByTypeAndDormitoryId(Room.Type.AISLE, dormitory.getId());

        // Then
        assertEquals(2, blockRooms.size());
        assertEquals(1, aisleRooms.size());
        assertTrue(blockRooms.stream().allMatch(r -> r.getType() == Room.Type.BLOCK));
        assertTrue(aisleRooms.stream().allMatch(r -> r.getType() == Room.Type.AISLE));
    }

    @Test
    void testGetByDormitoryIdAndNumber_ShouldReturnRoom() {
        // Given
        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room);
        flushAndClear();

        // When
        Room result = roomRepository.getByDormitoryIdAndNumber(dormitory.getId(), 101).orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(101, result.getNumber());
        assertEquals(dormitory.getId(), result.getDormitory().getId());
    }

    @Test
    void testGetByDormitoryIdAndNumber_WhenNotFound_ShouldReturnEmpty() {
        // Given
        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);
        flushAndClear();

        // When
        var result = roomRepository.getByDormitoryIdAndNumber(dormitory.getId(), 999);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByOrderById_ShouldReturnRoomsOrderedById() {
        // Given
        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);

        Room room1 = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room1);

        Room room2 = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(102)
                .build();
        roomRepository.save(room2);

        Room room3 = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(103)
                .build();
        roomRepository.save(room3);

        flushAndClear();

        // When
        List<Room> rooms = roomRepository.findAllByOrderById();

        // Then
        assertTrue(rooms.size() >= 3);
        // Verify ordering
        for (int i = 0; i < rooms.size() - 1; i++) {
            assertTrue(rooms.get(i).getId() <= rooms.get(i + 1).getId());
        }
    }
}


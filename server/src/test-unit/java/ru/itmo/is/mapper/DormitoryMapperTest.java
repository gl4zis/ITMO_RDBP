package ru.itmo.is.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.is.dto.DormitoryResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.Resident;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DormitoryMapperTest {

    private DormitoryMapper dormitoryMapper;
    private Dormitory dormitory;
    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        dormitoryMapper = new DormitoryMapper();

        University university = new University();
        university.setId(1);

        dormitory = new Dormitory();
        dormitory.setId(1);
        dormitory.setAddress("Test Address");
        dormitory.setUniversities(new ArrayList<>());
        dormitory.setRooms(new ArrayList<>());

        room1 = new Room();
        room1.setResidents(new ArrayList<>());
        Resident resident1 = new Resident();
        resident1.setLogin("r1");
        room1.getResidents().add(resident1);

        room2 = new Room();
        room2.setResidents(new ArrayList<>());
        Resident resident2 = new Resident();
        resident2.setLogin("r2");
        room2.getResidents().add(resident2);

        dormitory.getUniversities().add(university);
        dormitory.getRooms().add(room1);
        dormitory.getRooms().add(room2);
    }

    @Test
    void testToResponse_ShouldMapCorrectly() {
        DormitoryResponse result = dormitoryMapper.toResponse(dormitory);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Address", result.getAddress());
        assertEquals(1, result.getUniversityIds().size());
        assertEquals(1, result.getUniversityIds().get(0));
        assertEquals(2, result.getResidentNumber());
    }

    @Test
    void testToResponse_WithMultipleUniversities_ShouldMapAll() {
        University university2 = new University();
        university2.setId(2);
        dormitory.getUniversities().add(university2);

        DormitoryResponse result = dormitoryMapper.toResponse(dormitory);

        assertEquals(2, result.getUniversityIds().size());
        assertTrue(result.getUniversityIds().contains(1));
        assertTrue(result.getUniversityIds().contains(2));
    }

    @Test
    void testToResponse_WithNoResidents_ShouldReturnZero() {
        room1.setResidents(new ArrayList<>());
        room2.setResidents(new ArrayList<>());

        DormitoryResponse result = dormitoryMapper.toResponse(dormitory);

        assertEquals(0, result.getResidentNumber());
    }

    @Test
    void testToResponse_WithMultipleResidentsPerRoom_ShouldSumCorrectly() {
        Resident resident3 = new Resident();
        resident3.setLogin("r3");
        room1.getResidents().add(resident3);

        DormitoryResponse result = dormitoryMapper.toResponse(dormitory);

        assertEquals(3, result.getResidentNumber());
    }
}


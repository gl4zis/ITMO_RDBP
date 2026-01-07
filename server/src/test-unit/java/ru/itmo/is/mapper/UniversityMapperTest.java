package ru.itmo.is.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.is.dto.UniversityResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.University;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UniversityMapperTest {

    private UniversityMapper universityMapper;
    private University university;
    private Dormitory dormitory1;

    @BeforeEach
    void setUp() {
        universityMapper = new UniversityMapper();

        university = new University();
        university.setId(1);
        university.setName("Test University");
        university.setAddress("Test Address");
        university.setDormitories(new ArrayList<>());

        dormitory1 = new Dormitory();
        dormitory1.setId(1);

        Dormitory dormitory2 = new Dormitory();
        dormitory2.setId(2);

        university.getDormitories().add(dormitory1);
        university.getDormitories().add(dormitory2);
    }

    @Test
    void testToResponse_ShouldMapCorrectly() {
        UniversityResponse result = universityMapper.toResponse(university);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test University", result.getName());
        assertEquals("Test Address", result.getAddress());
        assertEquals(2, result.getDormitoryIds().size());
        assertTrue(result.getDormitoryIds().contains(1));
        assertTrue(result.getDormitoryIds().contains(2));
    }

    @Test
    void testToResponse_WithNoDormitories_ShouldReturnEmptyList() {
        university.setDormitories(new ArrayList<>());

        UniversityResponse result = universityMapper.toResponse(university);

        assertNotNull(result);
        assertTrue(result.getDormitoryIds().isEmpty());
    }

    @Test
    void testToResponse_WithSingleDormitory_ShouldMapCorrectly() {
        university.setDormitories(new ArrayList<>());
        university.getDormitories().add(dormitory1);

        UniversityResponse result = universityMapper.toResponse(university);

        assertEquals(1, result.getDormitoryIds().size());
        assertEquals(1, result.getDormitoryIds().get(0));
    }
}


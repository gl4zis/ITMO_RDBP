package ru.itmo.is.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.dto.DormitoryRequest;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.repository.DormitoryRepository;
import ru.itmo.is.repository.UniversityRepository;
import ru.itmo.is.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class DormitoryControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllDormitories_ShouldReturnList() throws Exception {
        // Given
        dormitoryRepository.deleteAll();
        University university = testDataBuilder.university()
                .withName("Test University")
                .build();
        universityRepository.save(university);
        flushAndClear();

        Dormitory dormitory1 = testDataBuilder.dormitory()
                .withAddress("Address 1")
                .build();
        dormitory1.getUniversities().add(university);
        dormitoryRepository.save(dormitory1);

        Dormitory dormitory2 = testDataBuilder.dormitory()
                .withAddress("Address 2")
                .build();
        dormitory2.getUniversities().add(university);
        dormitoryRepository.save(dormitory2);
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/dormitory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetDormitory_WhenExists_ShouldReturnDormitory() throws Exception {
        // Given
        University university = testDataBuilder.university()
                .withName("Test University")
                .build();
        universityRepository.save(university);
        flushAndClear();

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Address")
                .build();
        dormitory.getUniversities().add(university);
        dormitoryRepository.save(dormitory);
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/dormitory/{id}", dormitory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dormitory.getId()))
                .andExpect(jsonPath("$.address").value("Test Address"))
                .andExpect(jsonPath("$.universityIds").isArray());
    }

    @Test
    void testAddDormitory_AsManager_ShouldCreateDormitory() throws Exception {
        // Given
        dormitoryRepository.deleteAll();
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        University university = testDataBuilder.university()
                .withName("Test University")
                .build();
        universityRepository.save(university);
        flushAndClear();

        DormitoryRequest request = new DormitoryRequest();
        request.setAddress("New Dormitory Address");
        request.setUniversityIds(List.of(university.getId()));

        // When/Then
        mockMvc.perform(post("/dormitory")
                        .header("Authorization", jwtHelper.generateAuthHeader(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify dormitory was created
        flushAndClear();
        Iterable<Dormitory> allDormitories = dormitoryRepository.findAll();

        assertEquals(1, allDormitories.spliterator().getExactSizeIfKnown());
    }

    @Test
    void testUpdateDormitory_AsManager_ShouldUpdateDormitory() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        University university = testDataBuilder.university()
                .withName("Test University")
                .build();
        universityRepository.save(university);
        flushAndClear();

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Old Address")
                .build();
        dormitory.getUniversities().add(university);
        dormitoryRepository.save(dormitory);
        flushAndClear();

        DormitoryRequest request = new DormitoryRequest();
        request.setAddress("New Address");
        request.setUniversityIds(List.of(university.getId()));

        // When/Then
        mockMvc.perform(put("/dormitory/{id}", dormitory.getId())
                        .header("Authorization", jwtHelper.generateAuthHeader(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify dormitory was updated
        flushAndClear();
        Dormitory updated = dormitoryRepository.findById(dormitory.getId()).orElseThrow();
        assertEquals("New Address", updated.getAddress());
    }

    @Test
    void testDeleteDormitory_AsManager_ShouldDeleteDormitory() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        University university = testDataBuilder.university()
                .withName("Test University")
                .build();
        universityRepository.save(university);
        flushAndClear();

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("To Delete")
                .build();
        dormitory.getUniversities().add(university);
        dormitoryRepository.save(dormitory);
        flushAndClear();

        // When/Then
        mockMvc.perform(delete("/dormitory/{id}", dormitory.getId())
                        .header("Authorization", jwtHelper.generateAuthHeader(manager)))
                .andExpect(status().isOk());

        // Verify dormitory was deleted
        flushAndClear();
        assertFalse(dormitoryRepository.existsById(dormitory.getId()));
    }
}


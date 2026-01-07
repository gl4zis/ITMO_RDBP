package ru.itmo.is.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.dto.UniversityRequest;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.repository.UniversityRepository;
import ru.itmo.is.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UniversityControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllUniversities_ShouldReturnList() throws Exception {
        // Given
        University university1 = testDataBuilder.university()
                .withName("University 1")
                .withAddress("Address 1")
                .build();
        universityRepository.save(university1);

        University university2 = testDataBuilder.university()
                .withName("University 2")
                .withAddress("Address 2")
                .build();
        universityRepository.save(university2);
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/university"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetUniversity_WhenExists_ShouldReturnUniversity() throws Exception {
        // Given
        University university = testDataBuilder.university()
                .withName("Test University")
                .withAddress("Test Address")
                .build();
        universityRepository.save(university);
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/university/{id}", university.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(university.getId()))
                .andExpect(jsonPath("$.name").value("Test University"))
                .andExpect(jsonPath("$.address").value("Test Address"));
    }

    @Test
    void testGetUniversity_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/university/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddUniversity_AsManager_ShouldCreateUniversity() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);
        flushAndClear();

        UniversityRequest request = new UniversityRequest();
        request.setName("New University");
        request.setAddress("New Address");

        // When/Then
        mockMvc.perform(post("/university")
                        .header("Authorization", jwtHelper.generateAuthHeader(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify university was created
        flushAndClear();
        Iterable<University> allUniversities = universityRepository.findAll();
        University saved = null;
        for (University u : allUniversities) {
            if (u.getName().equals("New University")) {
                saved = u;
                break;
            }
        }
        assertNotNull(saved);
        assertEquals("New Address", saved.getAddress());
    }

    @Test
    void testAddUniversity_AsNonManager_ShouldReturnForbidden() throws Exception {
        // Given
        User resident = testDataBuilder.user()
                .withLogin("resident1")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(resident);
        flushAndClear();

        UniversityRequest request = new UniversityRequest();
        request.setName("New University");
        request.setAddress("New Address");

        // When/Then
        mockMvc.perform(post("/university")
                        .header("Authorization", jwtHelper.generateAuthHeader(resident))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateUniversity_AsManager_ShouldUpdateUniversity() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        University university = testDataBuilder.university()
                .withName("Old Name")
                .withAddress("Old Address")
                .build();
        universityRepository.save(university);
        flushAndClear();

        UniversityRequest request = new UniversityRequest();
        request.setName("New Name");
        request.setAddress("New Address");

        // When/Then
        mockMvc.perform(put("/university/{id}", university.getId())
                        .header("Authorization", jwtHelper.generateAuthHeader(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify university was updated
        flushAndClear();
        University updated = universityRepository.findById(university.getId()).orElseThrow();
        assertEquals("New Name", updated.getName());
        assertEquals("New Address", updated.getAddress());
    }

    @Test
    void testDeleteUniversity_AsManager_ShouldDeleteUniversity() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        University university = testDataBuilder.university()
                .withName("To Delete")
                .build();
        universityRepository.save(university);
        flushAndClear();

        // When/Then
        mockMvc.perform(delete("/university/{id}", university.getId())
                        .header("Authorization", jwtHelper.generateAuthHeader(manager)))
                .andExpect(status().isOk());

        // Verify university was deleted
        flushAndClear();
        assertFalse(universityRepository.existsById(university.getId()));
    }
}


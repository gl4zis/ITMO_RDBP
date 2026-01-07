package ru.itmo.is.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.dto.RoomRequest;
import ru.itmo.is.dto.RoomType;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.repository.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RoomControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllRooms_ShouldReturnList() throws Exception {
        // Given
        roomRepository.deleteAll();
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
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetRoom_WhenExists_ShouldReturnRoom() throws Exception {
        // Given
        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);
        flushAndClear();

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .withType(Room.Type.BLOCK)
                .withCapacity(2)
                .withCost(5000)
                .build();
        roomRepository.save(room);
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/room/{id}", room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(room.getId()))
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.cost").value(5000));
    }

    @Test
    void testAddRoom_AsManager_ShouldCreateRoom() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);
        flushAndClear();

        RoomRequest request = new RoomRequest();
        request.setDormitoryId(dormitory.getId());
        request.setNumber(201);
        request.setType(RoomType.BLOCK);
        request.setCapacity(2);
        request.setFloor(2);
        request.setCost(5000);

        // When/Then
        mockMvc.perform(post("/room")
                        .header("Authorization", jwtHelper.generateAuthHeader(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify room was created
        flushAndClear();
        Room saved = roomRepository.getByDormitoryIdAndNumber(dormitory.getId(), 201).orElse(null);
        assertNotNull(saved);
        assertEquals(Room.Type.BLOCK, saved.getType());
        assertEquals(2, saved.getCapacity());
        assertEquals(5000, saved.getCost());
    }

    @Test
    void testDeleteRoom_AsManager_WhenNoResidents_ShouldDeleteRoom() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);
        flushAndClear();

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room);
        flushAndClear();

        // When/Then
        mockMvc.perform(delete("/room/{id}", room.getId())
                        .header("Authorization", jwtHelper.generateAuthHeader(manager)))
                .andExpect(status().isOk());

        // Verify room was deleted
        flushAndClear();
        assertFalse(roomRepository.existsById(room.getId()));
    }

    @Test
    void testDeleteRoom_AsManager_WhenHasResidents_ShouldReturnBadRequest() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);

        University university = testDataBuilder.university().build();
        universityRepository.save(university);

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .addUniversity(university)
                .build();
        dormitoryRepository.save(dormitory);

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room);

        User user = testDataBuilder.user()
                .withLogin("resident1")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);
        residentRepository.userIsResidentNow(user.getLogin(), university.getId(), room.getId());

        flushAndClear();

        // When/Then
        mockMvc.perform(delete("/room/{id}", room.getId())
                        .header("Authorization", jwtHelper.generateAuthHeader(manager)))
                .andExpect(status().isBadRequest());
    }
}


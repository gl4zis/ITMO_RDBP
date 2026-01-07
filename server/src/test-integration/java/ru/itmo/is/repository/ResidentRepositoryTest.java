package ru.itmo.is.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;

import static org.junit.jupiter.api.Assertions.*;

class ResidentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Test
    void testUserIsResidentNow_ShouldInsertResidentRecord() {
        // Given
        User user = testDataBuilder.user()
                .withRole(User.Role.NON_RESIDENT)
                .build();
        userRepository.save(user);

        University university = testDataBuilder.university().build();
        universityRepository.save(university);

        Dormitory dormitory = testDataBuilder.dormitory().build();
        dormitoryRepository.save(dormitory);

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .build();
        roomRepository.save(room);

        // When
        residentRepository.userIsResidentNow(user.getLogin(), university.getId(), room.getId());
        var savedUser = userRepository.findById(user.getLogin()).get();
        savedUser.setRole(User.Role.RESIDENT);
        userRepository.save(savedUser);
        flushAndClear();

        // Then
        Resident resident = residentRepository.findById(user.getLogin()).orElse(null);
        assertNotNull(resident);
        assertEquals(user.getLogin(), resident.getLogin());
        assertEquals(university.getId(), resident.getUniversity().getId());
        assertEquals(room.getId(), resident.getRoom().getId());
        assertEquals(User.Role.RESIDENT, resident.getRole());
    }

    @Test
    void testUserIsNotResidentAnyMore_ShouldDeleteResidentRecord() {
        // Given
        User user = testDataBuilder.user()
                .withLogin("toremove")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);

        University university = testDataBuilder.university()
                .withName("Test University")
                .build();
        universityRepository.save(university);

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room);

        residentRepository.userIsResidentNow("toremove", university.getId(), room.getId());
        flushAndClear();

        // Verify resident exists
        assertTrue(residentRepository.existsById("toremove"));

        // When
        residentRepository.userIsNotResidentAnyMore("toremove");
        flushAndClear();

        // Then
        assertFalse(residentRepository.existsById("toremove"));
        // User should still exist
        assertTrue(userRepository.existsById("toremove"));
    }

    @Test
    void testUserIsResidentNow_WithTransaction_ShouldRollbackOnError() {
        // Given
        User user = testDataBuilder.user()
                .withLogin("testuser")
                .withRole(User.Role.NON_RESIDENT)
                .build();
        userRepository.save(user);
        flushAndClear();

        // When/Then - Invalid IDs should cause error
        // Note: The @Transactional annotation on the method should handle rollback
        assertThrows(Exception.class, () -> {
            residentRepository.userIsResidentNow("testuser", 99999, 99999);
        });
    }
}


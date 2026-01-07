package ru.itmo.is.entity.user;

import org.junit.jupiter.api.Test;
import ru.itmo.is.entity.user.User;

import static org.junit.jupiter.api.Assertions.*;

class ResidentTest {

    @Test
    void testOf_ShouldCreateResidentFromUser() {
        User user = new User();
        user.setLogin("testuser");
        user.setPassword("password123");
        user.setName("Test");
        user.setSurname("User");
        user.setRole(User.Role.NON_RESIDENT);

        Resident resident = Resident.of(user);

        assertNotNull(resident);
        assertEquals("testuser", resident.getLogin());
        assertEquals("password123", resident.getPassword());
        assertEquals("Test", resident.getName());
        assertEquals("User", resident.getSurname());
        assertEquals(User.Role.RESIDENT, resident.getRole());
    }

    @Test
    void testOf_ShouldCreateNewInstance() {
        User user = new User();
        user.setLogin("testuser");

        Resident resident1 = Resident.of(user);
        Resident resident2 = Resident.of(user);

        assertNotSame(resident1, resident2);
    }
}


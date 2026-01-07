package ru.itmo.is.entity.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setLogin("user1");

        user2 = new User();
        user2.setLogin("user1");

        user3 = new User();
        user3.setLogin("user2");
    }

    @Test
    void testEquals_WithSameLogin_ShouldReturnTrue() {
        assertEquals(user1, user2);
    }

    @Test
    void testEquals_WithDifferentLogin_ShouldReturnFalse() {
        assertNotEquals(user1, user3);
    }

    @Test
    void testEquals_WithNull_ShouldReturnFalse() {
        assertNotNull(user1);
    }

    @Test
    void testHashCode_WithSameLogin_ShouldReturnSameHash() {
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testHashCode_WithDifferentLogin_ShouldReturnDifferentHash() {
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }
}


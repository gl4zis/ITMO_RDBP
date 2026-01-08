package ru.itmo.is.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.is.entity.user.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContextTest {

    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        securityContext = new SecurityContext();
    }

    @Test
    void testSetContext_WithValidCredentials_ShouldSetUsernameAndRole() {
        String username = "testuser";
        User.Role role = User.Role.RESIDENT;

        securityContext.setContext(Optional.of(username), Optional.of(role));

        assertEquals(username, securityContext.getUsername());
        assertEquals(role, securityContext.getRole());
    }

    @Test
    void testSetContext_WithNullUsername_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            securityContext.setContext(Optional.empty(), Optional.of(User.Role.RESIDENT));
        });
    }

    @Test
    void testSetContext_WithNullRole_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            securityContext.setContext(Optional.of("testuser"), Optional.empty());
        });
    }

    @Test
    void testSetAnonymous_ShouldClearUsernameAndRole() {
        securityContext.setContext(Optional.of("testuser"), Optional.of(User.Role.RESIDENT));
        securityContext.setAnonymous();

        assertNull(securityContext.getUsername());
        assertNull(securityContext.getRole());
    }

    @Test
    void testIsAnonymous_WhenContextIsSet_ShouldReturnFalse() {
        securityContext.setContext(Optional.of("testuser"), Optional.of(User.Role.RESIDENT));

        assertFalse(securityContext.isAnonymous());
    }

    @Test
    void testIsAnonymous_WhenAnonymous_ShouldReturnTrue() {
        securityContext.setAnonymous();

        assertTrue(securityContext.isAnonymous());
    }

    @Test
    void testIsAnonymous_WhenNewInstance_ShouldReturnTrue() {
        assertTrue(securityContext.isAnonymous());
    }
}


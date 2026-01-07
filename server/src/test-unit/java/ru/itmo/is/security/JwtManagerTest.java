package ru.itmo.is.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.entity.user.User;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtManagerTest {

    private JwtManager jwtManager;

    private static final String TEST_KEY = "aa7sr4lm21fr5fq0y0qyhjv8221r8ro3ee1iwj6sj4i1qppjin";
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtManager = new JwtManager(TEST_KEY);

        testUser = new User();
        testUser.setLogin("testuser");
        testUser.setRole(User.Role.RESIDENT);
    }

    @Test
    void testCreateToken_ShouldCreateValidToken() {
        String token = jwtManager.createToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testCreateToken_ShouldContainCorrectSubject() {
        String token = jwtManager.createToken(testUser);
        String login = jwtManager.getLogin(token);

        assertEquals(testUser.getLogin(), login);
    }

    @Test
    void testCreateToken_ShouldContainCorrectRole() {
        String token = jwtManager.createToken(testUser);
        User.Role role = jwtManager.getRole(token);

        assertEquals(testUser.getRole(), role);
    }

    @Test
    void testGetLogin_WithValidToken_ShouldReturnLogin() {
        String token = jwtManager.createToken(testUser);
        String login = jwtManager.getLogin(token);

        assertEquals("testuser", login);
    }

    @Test
    void testGetLogin_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token.here";
        String login = jwtManager.getLogin(invalidToken);

        assertNull(login);
    }

    @Test
    void testGetRole_WithValidToken_ShouldReturnRole() {
        testUser.setRole(User.Role.MANAGER);
        String token = jwtManager.createToken(testUser);
        User.Role role = jwtManager.getRole(token);

        assertEquals(User.Role.MANAGER, role);
    }

    @Test
    void testGetRole_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token.here";
        User.Role role = jwtManager.getRole(invalidToken);

        assertNull(role);
    }

    @Test
    void testCreateToken_WithDifferentRoles_ShouldCreateDifferentTokens() {
        testUser.setRole(User.Role.MANAGER);
        String token1 = jwtManager.createToken(testUser);

        testUser.setRole(User.Role.RESIDENT);
        String token2 = jwtManager.createToken(testUser);

        assertNotEquals(token1, token2);
    }
}


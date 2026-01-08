package ru.itmo.is.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.entity.user.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtManagerTest {
    private static final String TEST_ACCESS_KEY = "test-access-key-with-sufficient-length-for-hmac-sha256-algorithm";
    private static final String TEST_REFRESH_KEY = "test-refresh-key-with-sufficient-length-for-hmac-sha256-algorithm";

    private JwtManager jwtManager;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtManager = new JwtManager(TEST_ACCESS_KEY, TEST_REFRESH_KEY);

        testUser = new User();
        testUser.setLogin("testuser");
        testUser.setRole(User.Role.RESIDENT);
    }

    @Test
    void testCreateToken_ShouldCreateValidToken() {
        String token = jwtManager.createAccessToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testCreateToken_ShouldContainCorrectSubject() {
        String token = jwtManager.createAccessToken(testUser);
        Optional<String> login = jwtManager.getLoginFromAccessToken(token);

        assertTrue(login.isPresent());
        assertEquals(testUser.getLogin(), login.get());
    }

    @Test
    void testCreateToken_ShouldContainCorrectRole() {
        String token = jwtManager.createAccessToken(testUser);
        Optional<User.Role> role = jwtManager.getRoleFromAccessToken(token);

        assertTrue(role.isPresent());
        assertEquals(testUser.getRole(), role.get());
    }

    @Test
    void testGetLogin_WithValidToken_ShouldReturnLogin() {
        String token = jwtManager.createAccessToken(testUser);
        Optional<String> login = jwtManager.getLoginFromAccessToken(token);

        assertTrue(login.isPresent());
        assertEquals("testuser", login.get());
    }

    @Test
    void testGetLogin_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token.here";
        Optional<String> login = jwtManager.getLoginFromAccessToken(invalidToken);

        assertTrue(login.isEmpty());
    }

    @Test
    void testGetRole_WithValidToken_ShouldReturnRole() {
        testUser.setRole(User.Role.MANAGER);
        String token = jwtManager.createAccessToken(testUser);
        Optional<User.Role> role = jwtManager.getRoleFromAccessToken(token);

        assertTrue(role.isPresent());
        assertEquals(User.Role.MANAGER, role.get());
    }

    @Test
    void testGetRole_WithInvalidToken_ShouldReturnNull() {
        String invalidToken = "invalid.token.here";
        Optional<User.Role> role = jwtManager.getRoleFromAccessToken(invalidToken);

        assertTrue(role.isEmpty());
    }

    @Test
    void testCreateToken_WithDifferentRoles_ShouldCreateDifferentTokens() {
        testUser.setRole(User.Role.MANAGER);
        String token1 = jwtManager.createAccessToken(testUser);

        testUser.setRole(User.Role.RESIDENT);
        String token2 = jwtManager.createAccessToken(testUser);

        assertNotEquals(token1, token2);
    }
}


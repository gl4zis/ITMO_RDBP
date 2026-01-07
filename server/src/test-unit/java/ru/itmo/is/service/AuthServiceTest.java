package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.ConflictException;
import ru.itmo.is.exception.UnauthorizedException;
import ru.itmo.is.mapper.UserMapper;
import ru.itmo.is.repository.UserRepository;
import ru.itmo.is.security.JwtManager;
import ru.itmo.is.security.PasswordManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtManager jwtManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private PasswordChangeRequest passwordChangeRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("testuser");
        user.setPassword(PasswordManager.hash("password123"));
        user.setRole(User.Role.RESIDENT);

        registerRequest = new RegisterRequest();
        registerRequest.setLogin("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test");
        registerRequest.setSurname("User");
        registerRequest.setRole(UserRole.RESIDENT);

        loginRequest = new LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("password123");

        passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setOldPassword("password123");
        passwordChangeRequest.setNewPassword("newpassword123");
    }

    @Test
    void testRegister_WithNonResidentRole_ShouldReturnToken() {
        registerRequest.setRole(UserRole.NON_RESIDENT);
        user.setRole(User.Role.NON_RESIDENT);
        when(userMapper.toUserModel(registerRequest)).thenReturn(user);
        when(userRepository.existsByLogin("testuser")).thenReturn(false);
        when(jwtManager.createToken(any(User.class))).thenReturn("token123");

        StringData result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("token123", result.getData());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_WithManagerRole_WhenNoManagerExists_ShouldReturnToken() {
        registerRequest.setRole(UserRole.MANAGER);
        user.setRole(User.Role.MANAGER);
        when(userMapper.toUserModel(registerRequest)).thenReturn(user);
        when(userRepository.countByRole(User.Role.MANAGER)).thenReturn(0L);
        when(userRepository.existsByLogin("testuser")).thenReturn(false);
        when(jwtManager.createToken(any(User.class))).thenReturn("token123");

        StringData result = authService.register(registerRequest);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_WithManagerRole_WhenManagerExists_ShouldThrowUnauthorizedException() {
        registerRequest.setRole(UserRole.MANAGER);
        user.setRole(User.Role.MANAGER);
        when(userMapper.toUserModel(registerRequest)).thenReturn(user);
        when(userRepository.countByRole(User.Role.MANAGER)).thenReturn(1L);

        assertThrows(UnauthorizedException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void testRegister_WithInvalidRole_ShouldThrowBadRequestException() {
        registerRequest.setRole(UserRole.GUARD);

        assertThrows(BadRequestException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void testRegister_WhenUserExists_ShouldThrowConflictException() {
        registerRequest.setRole(UserRole.NON_RESIDENT);
        when(userMapper.toUserModel(registerRequest)).thenReturn(user);
        when(userRepository.existsByLogin("testuser")).thenReturn(true);

        assertThrows(ConflictException.class, () -> {
            authService.register(registerRequest);
        });
    }

    @Test
    void testLogin_WithValidCredentials_ShouldReturnToken() {
        when(userRepository.findById("testuser")).thenReturn(Optional.of(user));
        when(jwtManager.createToken(user)).thenReturn("token123");

        StringData result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("token123", result.getData());
    }

    @Test
    void testLogin_WithInvalidLogin_ShouldThrowUnauthorizedException() {
        when(userRepository.findById("testuser")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void testLogin_WithInvalidPassword_ShouldThrowUnauthorizedException() {
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findById("testuser")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void testChangePassword_WithValidOldPassword_ShouldUpdatePassword() {
        when(userService.getCurrentUserOrThrow()).thenReturn(user);

        authService.changePassword(passwordChangeRequest);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_WithInvalidOldPassword_ShouldThrowBadRequestException() {
        passwordChangeRequest.setOldPassword("wrongpassword");
        when(userService.getCurrentUserOrThrow()).thenReturn(user);

        assertThrows(BadRequestException.class, () -> {
            authService.changePassword(passwordChangeRequest);
        });
    }

    @Test
    void testGetProfile_ShouldReturnProfile() {
        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setLogin("testuser");
        when(userService.getCurrentUserOrThrow()).thenReturn(user);
        when(userMapper.mapToProfile(user)).thenReturn(profileResponse);

        ProfileResponse result = authService.getProfile();

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
    }
}


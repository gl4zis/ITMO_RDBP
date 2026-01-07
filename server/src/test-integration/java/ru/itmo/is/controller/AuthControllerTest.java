package ru.itmo.is.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.repository.UserRepository;
import ru.itmo.is.security.PasswordManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        User user = testDataBuilder.user()
                .withLogin("testuser")
                .withPassword("password123")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);
        flushAndClear();

        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("password123");

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        User user = testDataBuilder.user()
                .withLogin("testuser")
                .withPassword("password123")
                .build();
        userRepository.save(user);
        flushAndClear();

        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("wrongpassword");

        // When/Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegister_WithNewUser_ShouldReturnToken() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setLogin("newuser");
        request.setPassword("password123");
        request.setName("New");
        request.setSurname("User");
        request.setRole(UserRole.NON_RESIDENT);

        // When/Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        // Verify user was created
        assertTrue(userRepository.existsById("newuser"));
        User savedUser = userRepository.findById("newuser").orElseThrow();
        assertTrue(PasswordManager.matches("password123", savedUser.getPassword()));
    }

    @Test
    void testRegister_WithExistingUser_ShouldReturnConflict() throws Exception {
        // Given
        User existingUser = testDataBuilder.user()
                .withLogin("existinguser")
                .build();
        userRepository.save(existingUser);
        flushAndClear();

        RegisterRequest request = new RegisterRequest();
        request.setLogin("existinguser");
        request.setPassword("password123");
        request.setName("Existing");
        request.setSurname("User");
        request.setRole(UserRole.NON_RESIDENT);

        // When/Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testRegisterOther_AsManager_ShouldCreateUser() throws Exception {
        // Given
        User manager = testDataBuilder.user()
                .withLogin("manager1")
                .withRole(User.Role.MANAGER)
                .build();
        userRepository.save(manager);
        flushAndClear();

        RegisterRequest request = new RegisterRequest();
        request.setLogin("newresident");
        request.setPassword("password123");
        request.setName("New");
        request.setSurname("Resident");
        request.setRole(UserRole.RESIDENT);

        // When/Then
        mockMvc.perform(post("/auth/register-other")
                        .header("Authorization", jwtHelper.generateAuthHeader(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify user was created
        assertTrue(userRepository.existsById("newresident"));
    }

    @Test
    void testRegisterOther_AsNonManager_ShouldReturnForbidden() throws Exception {
        // Given
        User resident = testDataBuilder.user()
                .withLogin("resident1")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(resident);
        flushAndClear();

        RegisterRequest request = new RegisterRequest();
        request.setLogin("newuser");
        request.setPassword("password123");
        request.setName("New");
        request.setSurname("User");
        request.setRole(UserRole.NON_RESIDENT);

        // When/Then
        mockMvc.perform(post("/auth/register-other")
                        .header("Authorization", jwtHelper.generateAuthHeader(resident))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetProfile_WhenAuthenticated_ShouldReturnProfile() throws Exception {
        // Given
        User user = testDataBuilder.user()
                .withLogin("testuser")
                .withName("Test")
                .withSurname("User")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);
        flushAndClear();

        // When/Then
        mockMvc.perform(get("/auth/profile")
                        .header("Authorization", jwtHelper.generateAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.surname").value("User"));
    }

    @Test
    void testGetProfile_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePassword_WithValidOldPassword_ShouldUpdatePassword() throws Exception {
        // Given
        User user = testDataBuilder.user()
                .withLogin("testuser")
                .withPassword("oldpassword")
                .build();
        userRepository.save(user);
        flushAndClear();

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");

        // When/Then
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", jwtHelper.generateAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify password was updated
        flushAndClear();
        User updatedUser = userRepository.findById("testuser").orElseThrow();
        assertTrue(PasswordManager.matches("newpassword", updatedUser.getPassword()));
    }

    @Test
    void testChangePassword_WithInvalidOldPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        User user = testDataBuilder.user()
                .withLogin("testuser")
                .withPassword("oldpassword")
                .build();
        userRepository.save(user);
        flushAndClear();

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword");

        // When/Then
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", jwtHelper.generateAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}


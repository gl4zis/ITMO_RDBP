package ru.itmo.is.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.PasswordManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private UniversityMapper universityMapper;

    @Mock
    private DormitoryMapper dormitoryMapper;

    @InjectMocks
    private UserMapper userMapper;

    private User user;
    private Resident resident;
    private RegisterRequest registerRequest;
    private UniversityResponse universityResponse;
    private DormitoryResponse dormitoryResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("testuser");
        user.setName("Test");
        user.setSurname("User");
        user.setRole(User.Role.RESIDENT);

        resident = new Resident();
        resident.setLogin("testuser");
        resident.setName("Test");
        resident.setSurname("User");
        resident.setRole(User.Role.RESIDENT);

        University university = new University();
        university.setId(1);
        university.setName("Test University");
        resident.setUniversity(university);

        Dormitory dormitory = new Dormitory();
        dormitory.setId(1);
        dormitory.setAddress("Test Address");

        Room room = new Room();
        room.setNumber(101);
        room.setDormitory(dormitory);
        resident.setRoom(room);

        registerRequest = new RegisterRequest();
        registerRequest.setLogin("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test");
        registerRequest.setSurname("User");
        registerRequest.setRole(UserRole.RESIDENT);

        universityResponse = new UniversityResponse();
        universityResponse.setId(1);
        universityResponse.setName("Test University");

        dormitoryResponse = new DormitoryResponse();
        dormitoryResponse.setId(1);
        dormitoryResponse.setAddress("Test Address");
    }

    @Test
    void testToUserModel_ShouldMapCorrectly() {
        User result = userMapper.toUserModel(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("Test", result.getName());
        assertEquals("User", result.getSurname());
        assertEquals(User.Role.RESIDENT, result.getRole());
        assertNotNull(result.getPassword());
        assertTrue(PasswordManager.matches("password123", result.getPassword()));
    }

    @Test
    void testToUserRoleDto_ShouldMapAllRoles() {
        assertEquals(UserRole.NON_RESIDENT, userMapper.toUserRoleDto(User.Role.NON_RESIDENT));
        assertEquals(UserRole.RESIDENT, userMapper.toUserRoleDto(User.Role.RESIDENT));
        assertEquals(UserRole.MANAGER, userMapper.toUserRoleDto(User.Role.MANAGER));
        assertEquals(UserRole.GUARD, userMapper.toUserRoleDto(User.Role.GUARD));
    }

    @Test
    void testMapToProfile_WithRegularUser_ShouldReturnProfile() {
        ProfileResponse result = userMapper.mapToProfile(user);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("Test", result.getName());
        assertEquals("User", result.getSurname());
        assertEquals(UserRole.RESIDENT, result.getRole());
    }

    @Test
    void testMapToProfile_WithResident_ShouldReturnProfileWithResidentInfo() {
        ProfileResponse result = userMapper.mapToProfile(resident);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("Test University", result.getUniversity());
        assertEquals("Test Address", result.getDormitory());
        assertEquals(101, result.getRoomNumber());
    }

    @Test
    void testMapUserResponse_ShouldMapCorrectly() {
        UserResponse result = userMapper.mapUserResponse(user);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("Test", result.getName());
        assertEquals("User", result.getSurname());
        assertEquals(UserRole.RESIDENT, result.getRole());
    }

    @Test
    void testToResidentResponse_ShouldMapCorrectly() {
        when(universityMapper.toResponse(any(University.class))).thenReturn(universityResponse);
        when(dormitoryMapper.toResponse(any(Dormitory.class))).thenReturn(dormitoryResponse);

        LocalDateTime lastCameOut = LocalDateTime.now();
        ResidentResponse result = userMapper.toResidentResponse(resident, 100, lastCameOut);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals(100, result.getDebt());
        assertEquals(lastCameOut, result.getLastCameOut());
    }

    @Test
    void testNonPaymentEvictResponse_ShouldCreateResponse() {
        ToEvictionResidentResponse result = userMapper.nonPaymentEvictResponse(user);

        assertNotNull(result);
        assertNotNull(result.getResident());
        assertEquals(EvictionReason.NON_PAYMENT, result.getReason());
        assertEquals("testuser", result.getResident().getLogin());
    }

    @Test
    void testNonResidenceEvictResponse_ShouldCreateResponse() {
        ToEvictionResidentResponse result = userMapper.nonResidenceEvictResponse(user);

        assertNotNull(result);
        assertNotNull(result.getResident());
        assertEquals(EvictionReason.NON_RESIDENCE, result.getReason());
        assertEquals("testuser", result.getResident().getLogin());
    }

    @Test
    void testRuleViolationEvictResponse_ShouldCreateResponse() {
        ToEvictionResidentResponse result = userMapper.ruleViolationEvictResponse(user);

        assertNotNull(result);
        assertNotNull(result.getResident());
        assertEquals(EvictionReason.RULE_VIOLATION, result.getReason());
        assertEquals("testuser", result.getResident().getLogin());
    }
}


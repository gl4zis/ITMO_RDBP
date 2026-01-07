package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.ResidentResponse;
import ru.itmo.is.dto.ToEvictionResidentResponse;
import ru.itmo.is.dto.UserResponse;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.ForbiddenException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.exception.UnauthorizedException;
import ru.itmo.is.mapper.DormitoryMapper;
import ru.itmo.is.mapper.UniversityMapper;
import ru.itmo.is.mapper.UserMapper;
import ru.itmo.is.repository.EventRepository;
import ru.itmo.is.repository.ResidentRepository;
import ru.itmo.is.repository.UserRepository;
import ru.itmo.is.security.SecurityContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private EventService eventService;
    private UserService userService;

    private User user;
    private Resident resident;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("testuser");
        user.setRole(User.Role.RESIDENT);

        resident = new Resident();
        resident.setLogin("testuser");
        resident.setRole(User.Role.RESIDENT);

        UniversityMapper universityMapper = new UniversityMapper();
        DormitoryMapper dormitoryMapper = new DormitoryMapper();
        UserMapper userMapper = new UserMapper(universityMapper, dormitoryMapper);
        userService = new UserService(
                userRepository,
                eventRepository,
                securityContext,
                residentRepository,
                userMapper,
                eventService
        );
    }

    @Test
    void testGetResidentByLogin_WhenExists_ShouldReturnResident() {
        when(residentRepository.findById("testuser")).thenReturn(Optional.of(resident));

        Resident result = userService.getResidentByLogin("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
    }

    @Test
    void testGetResidentByLogin_WhenNotExists_ShouldThrowNotFoundException() {
        when(residentRepository.findById("testuser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            userService.getResidentByLogin("testuser");
        });
    }

    @Test
    void testGetCurrentResidentOrThrow_WhenUserIsResident_ShouldReturnResident() {
        when(securityContext.getUsername()).thenReturn("testuser");
        when(userRepository.findById("testuser")).thenReturn(Optional.of(user));
        when(residentRepository.findById("testuser")).thenReturn(Optional.of(resident));

        Resident result = userService.getCurrentResidentOrThrow();

        assertNotNull(result);
    }

    @Test
    void testGetCurrentResidentOrThrow_WhenUserIsNotResident_ShouldThrowForbiddenException() {
        when(securityContext.getUsername()).thenReturn("testuser");
        when(userRepository.findById("testuser")).thenReturn(Optional.of(user));
        when(residentRepository.findById("testuser")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> {
            userService.getCurrentResidentOrThrow();
        });
    }

    @Test
    void testGetCurrentUserOrThrow_WhenLoggedIn_ShouldReturnUser() {
        when(securityContext.getUsername()).thenReturn("testuser");
        when(userRepository.findById("testuser")).thenReturn(Optional.of(user));

        User result = userService.getCurrentUserOrThrow();

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
    }

    @Test
    void testGetCurrentUserOrThrow_WhenNotLoggedIn_ShouldThrowUnauthorizedException() {
        when(securityContext.getUsername()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            userService.getCurrentUserOrThrow();
        });
    }

    @Test
    void testGetCurrentUserOrThrow_WhenUserNotFound_ShouldThrowUnauthorizedException() {
        when(securityContext.getUsername()).thenReturn("testuser");
        when(userRepository.findById("testuser")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> {
            userService.getCurrentUserOrThrow();
        });
    }

    @Test
    void testGetStaff_ShouldReturnStaffList() {
        User guard = new User();
        guard.setLogin("guard1");
        guard.setName("Guard");
        guard.setSurname("One");
        guard.setRole(User.Role.GUARD);

        when(userRepository.getUsersByRoleIn(List.of(User.Role.GUARD, User.Role.MANAGER)))
                .thenReturn(List.of(guard));

        List<UserResponse> result = userService.getStaff();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("guard1", result.get(0).getLogin());
    }

    @Test
    void testGetResidents_ShouldReturnResidentsList() {
        resident.setName("Test");
        resident.setSurname("User");
        
        // Set up university and room for the resident
        University university = new University();
        university.setId(1);
        university.setName("Test University");
        university.setAddress("Test Address");
        university.setDormitories(new java.util.ArrayList<>());
        resident.setUniversity(university);
        
        Dormitory dormitory = new Dormitory();
        dormitory.setId(1);
        dormitory.setAddress("Test Address");
        dormitory.setUniversities(new java.util.ArrayList<>());
        dormitory.setRooms(new java.util.ArrayList<>());
        dormitory.getUniversities().add(university);
        university.getDormitories().add(dormitory);
        
        Room room = new Room();
        room.setNumber(101);
        room.setDormitory(dormitory);
        room.setResidents(new java.util.ArrayList<>());
        dormitory.getRooms().add(room);
        resident.setRoom(room);
        
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT)))
                .thenReturn(List.of(resident));
        when(eventService.calculateResidentDebt("testuser")).thenReturn(0);
        when(eventRepository.getLastInOutEvent("testuser")).thenReturn(Optional.empty());

        List<ResidentResponse> result = userService.getResidents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getLogin());
    }

    @Test
    void testFire_WhenGuardExists_ShouldDeleteUser() {
        User guard = new User();
        guard.setLogin("guard1");
        guard.setRole(User.Role.GUARD);
        when(userRepository.findById("guard1")).thenReturn(Optional.of(guard));

        userService.fire("guard1");

        verify(userRepository).delete(guard);
    }

    @Test
    void testFire_WhenUserNotExists_ShouldThrowNotFoundException() {
        when(userRepository.findById("guard1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            userService.fire("guard1");
        });
    }

    @Test
    void testFire_WhenWrongRole_ShouldThrowBadRequestException() {
        when(userRepository.findById("testuser")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> {
            userService.fire("testuser");
        });
    }

    @Test
    void testGetResidentsToEviction_WithDebtResidents_ShouldReturnEvictionList() {
        User resident1 = new Resident();
        resident1.setLogin("resident1");
        resident1.setRole(User.Role.RESIDENT);

        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of("resident1"));
        when(userRepository.getByLoginIn(List.of("resident1"))).thenReturn(List.of(resident1));
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of());

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetResidentsToEviction_WithNonResidenceResidents_ShouldReturnEvictionList() {
        User resident1 = new Resident();
        resident1.setLogin("resident1");
        resident1.setRole(User.Role.RESIDENT);

        Event outEvent = new Event();
        outEvent.setType(Event.Type.OUT);
        outEvent.setTimestamp(LocalDateTime.now().minusDays(10));

        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of());
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of(resident1));
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(outEvent));

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("resident1", result.get(0).getResident().getLogin());
    }

    @Test
    void testGetResidentsToEviction_WithRuleViolationResidents_ShouldReturnEvictionList() {
        User resident1 = new Resident();
        resident1.setLogin("resident1");
        resident1.setRole(User.Role.RESIDENT);

        Event inEvent = new Event();
        inEvent.setType(Event.Type.IN);
        inEvent.setTimestamp(LocalDateTime.of(LocalDate.now(), LocalTime.of(3, 0)));

        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of());
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of(resident1));
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(inEvent));

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("resident1", result.get(0).getResident().getLogin());
    }

    @Test
    void testGetResidentsToEviction_WithMultipleReasons_ShouldNotDuplicate() {
        User resident1 = new Resident();
        resident1.setLogin("resident1");
        resident1.setRole(User.Role.RESIDENT);

        Event outEvent = new Event();
        outEvent.setType(Event.Type.OUT);
        outEvent.setTimestamp(java.time.LocalDateTime.of(
                java.time.LocalDate.now().minusDays(10), java.time.LocalTime.of(3, 0)));

        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of("resident1"));
        when(userRepository.getByLoginIn(List.of("resident1"))).thenReturn(List.of(resident1));
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of(resident1));
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(outEvent));

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("resident1", result.get(0).getResident().getLogin());
    }

    @Test
    void testGetResidentsToEviction_WithNoResidents_ShouldReturnEmptyList() {
        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of());
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of());

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetResidentsToEviction_WithRecentOutEvent_ShouldNotInclude() {
        User resident1 = new Resident();
        resident1.setLogin("resident1");
        resident1.setRole(User.Role.RESIDENT);

        Event outEvent = new Event();
        outEvent.setType(Event.Type.OUT);
        outEvent.setTimestamp(java.time.LocalDateTime.now().minusDays(5)); // Less than 7 days

        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of());
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of(resident1));
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(outEvent));

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetResidentsToEviction_WithNormalTimeEvent_ShouldNotInclude() {
        User resident1 = new Resident();
        resident1.setLogin("resident1");
        resident1.setRole(User.Role.RESIDENT);

        Event inEvent = new Event();
        inEvent.setType(Event.Type.IN);
        inEvent.setTimestamp(java.time.LocalDateTime.of(
                java.time.LocalDate.now(), java.time.LocalTime.of(10, 0))); // Normal time

        when(eventService.getResidentsToEvictionByDebt()).thenReturn(List.of());
        when(userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT))).thenReturn(List.of(resident1));
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(inEvent));

        List<ToEvictionResidentResponse> result = userService.getResidentsToEviction();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.GuardHistory;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.mapper.EventMapper;
import ru.itmo.is.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserService userService;
    @Mock
    private EventMapper eventMapper;
    @InjectMocks
    private GuardService guardService;

    private Resident resident;
    private Event lastEvent;
    private GuardHistory guardHistory;

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setLogin("resident1");

        lastEvent = new Event();
        lastEvent.setType(Event.Type.OUT);
        lastEvent.setTimestamp(LocalDateTime.now());

        guardHistory = new GuardHistory();
        guardHistory.setType(GuardHistory.TypeEnum.IN);
        guardHistory.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testEntry_WhenLastEventWasOut_ShouldCreateInEvent() {
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(lastEvent));
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);

        guardService.entry("resident1");

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testEntry_WhenNoLastEvent_ShouldCreateInEvent() {
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.empty());
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);

        guardService.entry("resident1");

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testEntry_WhenLastEventWasIn_ShouldThrowBadRequestException() {
        lastEvent.setType(Event.Type.IN);
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(lastEvent));

        assertThrows(BadRequestException.class, () -> {
            guardService.entry("resident1");
        });

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testExit_WhenLastEventWasIn_ShouldCreateOutEvent() {
        lastEvent.setType(Event.Type.IN);
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(lastEvent));
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);

        guardService.exit("resident1");

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testExit_WhenNoLastEvent_ShouldCreateOutEvent() {
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.empty());
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);

        guardService.exit("resident1");

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testExit_WhenLastEventWasOut_ShouldThrowBadRequestException() {
        when(eventRepository.getLastInOutEvent("resident1")).thenReturn(Optional.of(lastEvent));

        assertThrows(BadRequestException.class, () -> {
            guardService.exit("resident1");
        });

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testGetHistory_ShouldReturnGuardHistory() {
        Event event = new Event();
        event.setType(Event.Type.IN);
        event.setTimestamp(LocalDateTime.now());

        when(userService.getResidentByLogin("resident1")).thenReturn(resident);
        when(eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.IN, Event.Type.OUT), "resident1")).thenReturn(List.of(event));
        when(eventMapper.mapGuardEvent(event)).thenReturn(guardHistory);

        List<GuardHistory> result = guardService.getHistory("resident1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).getResidentByLogin("resident1");
    }

    @Test
    void testGetSelfHistory_ShouldReturnGuardHistory() {
        User user = new User();
        user.setLogin("resident1");
        Event event = new Event();
        event.setType(Event.Type.OUT);
        event.setTimestamp(LocalDateTime.now());

        when(userService.getCurrentUserOrThrow()).thenReturn(user);
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);
        when(eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.IN, Event.Type.OUT), "resident1")).thenReturn(List.of(event));
        when(eventMapper.mapGuardEvent(event)).thenReturn(guardHistory);

        List<GuardHistory> result = guardService.getSelfHistory();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}


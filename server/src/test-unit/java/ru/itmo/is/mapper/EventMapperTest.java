package ru.itmo.is.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.is.dto.GuardHistory;
import ru.itmo.is.entity.Event;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    private EventMapper eventMapper;
    private Event event;

    @BeforeEach
    void setUp() {
        eventMapper = new EventMapper();

        event = new Event();
        event.setType(Event.Type.IN);
        event.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testMapGuardEvent_WithInType_ShouldMapCorrectly() {
        event.setType(Event.Type.IN);

        GuardHistory result = eventMapper.mapGuardEvent(event);

        assertNotNull(result);
        assertEquals(GuardHistory.TypeEnum.IN, result.getType());
        assertEquals(event.getTimestamp(), result.getTimestamp());
    }

    @Test
    void testMapGuardEvent_WithOutType_ShouldMapCorrectly() {
        event.setType(Event.Type.OUT);

        GuardHistory result = eventMapper.mapGuardEvent(event);

        assertNotNull(result);
        assertEquals(GuardHistory.TypeEnum.OUT, result.getType());
        assertEquals(event.getTimestamp(), result.getTimestamp());
    }

    @Test
    void testMapGuardEvent_WithInvalidType_ShouldThrowException() {
        event.setType(Event.Type.PAYMENT);

        assertThrows(IllegalArgumentException.class, () -> {
            eventMapper.mapGuardEvent(event);
        });
    }
}


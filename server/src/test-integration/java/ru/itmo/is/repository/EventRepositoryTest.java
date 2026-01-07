package ru.itmo.is.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itmo.is.AbstractIntegrationTest;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Test
    void testGetLastInOutEvent_WhenEventsExist_ShouldReturnLatest() {
        // Given
        User user = testDataBuilder.user()
                .withLogin("resident1")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);

        University university = testDataBuilder.university()
                .build();
        universityRepository.save(university);

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .addUniversity(university)
                .build();
        dormitoryRepository.save(dormitory);

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room);

        residentRepository.userIsResidentNow(user.getLogin(), university.getId(), room.getId());

        Event outEvent = testDataBuilder.event()
                .withType(Event.Type.OUT)
                .withUser(user)
                .withRoom(room)
                .withTimestamp(LocalDateTime.now().minusDays(5))
                .build();
        eventRepository.save(outEvent);

        Event inEvent = testDataBuilder.event()
                .withType(Event.Type.IN)
                .withUser(user)
                .withRoom(room)
                .withTimestamp(LocalDateTime.now().minusDays(2))
                .build();
        eventRepository.save(inEvent);

        flushAndClear();

        // When
        Event result = eventRepository.getLastInOutEvent("resident1").orElse(null);

        // Then
        assertNotNull(result);
        assertEquals(Event.Type.IN, result.getType());
        assertEquals("resident1", result.getUsr().getLogin());
    }

    @Test
    void testGetLastInOutEvent_WhenNoEvents_ShouldReturnEmpty() {
        // Given
        User user = testDataBuilder.user()
                .withLogin("resident2")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);
        flushAndClear();

        // When
        var result = eventRepository.getLastInOutEvent("resident2");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByTypeInAndUsrLoginOrderByTimestampDesc_ShouldReturnOrderedEvents() {
        // Given
        User user = testDataBuilder.user()
                .withLogin("resident3")
                .withRole(User.Role.RESIDENT)
                .build();
        userRepository.save(user);

        Dormitory dormitory = testDataBuilder.dormitory()
                .withAddress("Test Dorm")
                .build();
        dormitoryRepository.save(dormitory);

        Room room = testDataBuilder.room()
                .withDormitory(dormitory)
                .withNumber(101)
                .build();
        roomRepository.save(room);

        Event event1 = testDataBuilder.event()
                .withType(Event.Type.PAYMENT)
                .withUser(user)
                .withRoom(room)
                .withPaymentSum(5000)
                .withTimestamp(LocalDateTime.now().minusDays(10))
                .build();
        eventRepository.save(event1);

        Event event2 = testDataBuilder.event()
                .withType(Event.Type.PAYMENT)
                .withUser(user)
                .withRoom(room)
                .withPaymentSum(3000)
                .withTimestamp(LocalDateTime.now().minusDays(5))
                .build();
        eventRepository.save(event2);

        Event event3 = testDataBuilder.event()
                .withType(Event.Type.PAYMENT)
                .withUser(user)
                .withRoom(room)
                .withPaymentSum(2000)
                .withTimestamp(LocalDateTime.now().minusDays(1))
                .build();
        eventRepository.save(event3);

        flushAndClear();

        // When
        List<Event> result = eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.PAYMENT), "resident3");

        // Then
        assertEquals(3, result.size());
        assertEquals(event3.getId(), result.get(0).getId()); // Most recent first
        assertEquals(event2.getId(), result.get(1).getId());
        assertEquals(event1.getId(), result.get(2).getId());
    }
}


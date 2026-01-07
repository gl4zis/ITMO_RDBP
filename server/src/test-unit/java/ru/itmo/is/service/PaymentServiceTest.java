package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.PaymentHistoryRecord;
import ru.itmo.is.dto.PaymentRequest;
import ru.itmo.is.dto.PaymentResponse;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventService eventService;
    @InjectMocks
    private PaymentService paymentService;

    private Resident resident;
    private Room room;
    private PaymentRequest paymentRequest;
    private Event paymentEvent;

    @BeforeEach
    void setUp() {
        Dormitory dormitory = new Dormitory();
        dormitory.setAddress("Test Dormitory");

        room = new Room();
        room.setId(1);
        room.setNumber(101);
        room.setCost(1000);
        room.setDormitory(dormitory);

        resident = new Resident();
        resident.setLogin("resident1");
        resident.setRoom(room);

        paymentRequest = new PaymentRequest();
        paymentRequest.setSum(500);

        paymentEvent = new Event();
        paymentEvent.setType(Event.Type.PAYMENT);
        paymentEvent.setTimestamp(LocalDateTime.now());
        paymentEvent.setRoom(room);
        paymentEvent.setPaymentSum(500);
    }

    @Test
    void testGetSelfPaymentInfo_ShouldReturnPaymentInfo() {
        when(userService.getCurrentResidentOrThrow()).thenReturn(resident);
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);
        when(eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.PAYMENT), "resident1")).thenReturn(new ArrayList<>());
        when(eventRepository.calculateResidentDebt("resident1")).thenReturn(500);
        when(eventService.getLastPaymentTime("resident1")).thenReturn(null);

        PaymentResponse result = paymentService.getSelfPaymentInfo();

        assertNotNull(result);
        assertEquals(500, result.getDebt());
        assertEquals(1000, result.getRoomCost());
        assertNull(result.getLastPaymentTime());
    }

    @Test
    void testGetPaymentInfo_ShouldReturnPaymentInfo() {
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);
        when(eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.PAYMENT), "resident1")).thenReturn(List.of(paymentEvent));
        when(eventRepository.calculateResidentDebt("resident1")).thenReturn(500);
        when(eventService.getLastPaymentTime("resident1")).thenReturn(LocalDateTime.now());

        PaymentResponse result = paymentService.getPaymentInfo("resident1");

        assertNotNull(result);
        assertEquals(500, result.getDebt());
        assertEquals(1000, result.getRoomCost());
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());
    }

    @Test
    void testCurrentUserPay_WithCorrectDebt_ShouldSaveEvent() {
        Integer debt = 500;
        when(userService.getCurrentResidentOrThrow()).thenReturn(resident);
        when(eventRepository.calculateResidentDebt("resident1")).thenReturn(debt);
        paymentRequest.setSum(debt); // Use same Integer instance

        paymentService.currentUserPay(paymentRequest);

        verify(eventRepository).save(argThat(event ->
                event.getType() == Event.Type.PAYMENT &&
                event.getUsr().equals(resident) &&
                event.getRoom().equals(room) &&
                event.getPaymentSum().equals(500))
        );
    }

    @Test
    void testCurrentUserPay_WithIncorrectDebt_ShouldThrowBadRequestException() {
        when(userService.getCurrentResidentOrThrow()).thenReturn(resident);
        when(eventRepository.calculateResidentDebt("resident1")).thenReturn(500);
        paymentRequest.setSum(300);

        assertThrows(BadRequestException.class, () -> {
            paymentService.currentUserPay(paymentRequest);
        });

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testGetPaymentInfo_WithPaymentHistory_ShouldMapCorrectly() {
        when(userService.getResidentByLogin("resident1")).thenReturn(resident);
        when(eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.PAYMENT), "resident1")).thenReturn(List.of(paymentEvent));
        when(eventRepository.calculateResidentDebt("resident1")).thenReturn(0);
        when(eventService.getLastPaymentTime("resident1")).thenReturn(null);

        PaymentResponse result = paymentService.getPaymentInfo("resident1");

        assertNotNull(result);
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());
        PaymentHistoryRecord record = result.getHistory().get(0);
        assertEquals("Test Dormitory", record.getDormitory());
        assertEquals(101, record.getRoomNumber());
        assertEquals(500, record.getSum());
    }
}


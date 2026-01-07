package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.PaymentHistoryRecord;
import ru.itmo.is.dto.PaymentRequest;
import ru.itmo.is.dto.PaymentResponse;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final UserService userService;
    private final EventService eventService;
    private final EventRepository eventRepository;

    public PaymentResponse getSelfPaymentInfo() {
        Resident resident = userService.getCurrentResidentOrThrow();
        return getPaymentInfo(resident.getLogin());
    }

    public PaymentResponse getPaymentInfo(String login) {
        Resident resident = userService.getResidentByLogin(login); // To make sure that this is resident
        List<Event> paymentEvents = eventRepository
                .getByTypeInAndUsrLoginOrderByTimestampDesc(List.of(Event.Type.PAYMENT), resident.getLogin());
        List<PaymentHistoryRecord> history = paymentEvents.stream().map(this::mapHistory).toList();
        Integer debt = eventRepository.calculateResidentDebt(resident.getLogin());
        LocalDateTime lastPaymentTime = eventService.getLastPaymentTime(resident.getLogin());

        return new PaymentResponse(debt, resident.getRoom().getCost(), lastPaymentTime, history);
    }

    public void currentUserPay(PaymentRequest req) {
        Resident resident = userService.getCurrentResidentOrThrow();
        int debt = eventRepository.calculateResidentDebt(resident.getLogin());
        if (req.getSum() != debt) {
            throw new BadRequestException("You can pay not equals to your debt sum");
        }
        var event = new Event();
        event.setType(Event.Type.PAYMENT);
        event.setUsr(resident);
        event.setRoom(resident.getRoom());
        event.setPaymentSum(req.getSum());
        eventRepository.save(event);
    }

    private PaymentHistoryRecord mapHistory(Event event) {
        return new PaymentHistoryRecord(
                event.getTimestamp(),
                event.getRoom().getDormitory().getAddress(),
                event.getRoom().getNumber(),
                event.getPaymentSum()
        );
    }
}

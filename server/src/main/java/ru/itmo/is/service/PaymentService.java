package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.request.PaymentRequest;
import ru.itmo.is.dto.response.PaymentResponse;
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
    private final EventRepository eventRepository;

    public PaymentResponse getSelfPaymentInfo() {
        Resident resident = userService.getCurrentResidentOrThrow();
        return getPaymentInfo(resident.getLogin());
    }

    public PaymentResponse getPaymentInfo(String login) {
        Resident resident = userService.getResidentByLogin(login); // To make sure that this is resident
        List<Event> paymentEvents = eventRepository
                .getByTypeInAndUsrLoginOrderByTimestampDesc(List.of(Event.Type.PAYMENT), resident.getLogin());
        List<PaymentResponse.History> history = paymentEvents.stream().map(this::mapHistory).toList();
        Integer debt = eventRepository.calculateResidentDebt(resident.getLogin());
        LocalDateTime lastPaymentTime = eventRepository.getLastPaymentTime(resident.getLogin());

        return new PaymentResponse(debt, resident.getRoom().getCost(), lastPaymentTime, history);
    }

    public void currentUserPay(PaymentRequest req) {
        Resident resident = userService.getCurrentResidentOrThrow();
        Integer debt = eventRepository.calculateResidentDebt(resident.getLogin());
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

    private PaymentResponse.History mapHistory(Event event) {
        return new PaymentResponse.History(
                event.getTimestamp(),
                event.getRoom().getDormitory().getAddress(),
                event.getRoom().getNumber(),
                event.getPaymentSum()
        );
    }
}

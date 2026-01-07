package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.entity.Event;
import ru.itmo.is.repository.EventRepository;
import ru.itmo.is.repository.RoomRepository;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;

    public LocalDateTime getLastPaymentTime(String login) {
        List<Event> events = eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.PAYMENT, Event.Type.OCCUPATION),
                login
        );

        if (events.isEmpty()) {
            throw new RuntimeException("Resident has no payment or occupation events");
        }
        
        return events.get(0).getTimestamp();
    }

    public Integer calculateResidentDebt(String login) {
        return roomRepository.getResidentRoomCost(login) * calcDebtInMonths(getLastPaymentTime(login));
    }

    public List<String> getResidentsToEvictionByDebt() {
        List<String> residentsToEvict = new ArrayList<>();

        eventRepository.getLastEventTimesForAllResidents(List.of(Event.Type.PAYMENT, Event.Type.OCCUPATION))
                .stream()
                .map(row -> new ResidentPaymentTime((String) row[0], (LocalDateTime) row[1]))
                .forEach(paymentRecord -> {
                    if (calcDebtInMonths(paymentRecord.timestamp()) > 6) {
                        residentsToEvict.add(paymentRecord.login());
                    }
                });
        
        return residentsToEvict;
    }

    private int calcDebtInMonths(LocalDateTime lastPaymentTime) {
        Period period = Period.between(lastPaymentTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        return period.getYears() * 12 + period.getMonths();
    }

    record ResidentPaymentTime(
            String login,
            LocalDateTime timestamp
    ) { }
}

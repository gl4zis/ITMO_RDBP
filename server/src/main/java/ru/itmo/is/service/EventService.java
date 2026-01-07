package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.entity.Event;
import ru.itmo.is.repository.EventRepository;
import ru.itmo.is.repository.RoomRepository;

import java.time.LocalDateTime;
import java.time.Period;
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
        LocalDateTime lastPaymentTime = getLastPaymentTime(login);
        Period period = Period.between(lastPaymentTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        int debtForMonths = period.getYears() * 12 + period.getMonths();

        int roomCost = roomRepository.getResidentRoomCost(login);
        
        return roomCost * debtForMonths;
    }
}

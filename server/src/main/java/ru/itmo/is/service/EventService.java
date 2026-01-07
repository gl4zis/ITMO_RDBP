package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.entity.Event;
import ru.itmo.is.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public LocalDateTime getLastPaymentTime(String login) {
        List<Event> events = eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.PAYMENT, Event.Type.OCCUPATION),
                login
        );
        
        return events.isEmpty() ? null : events.get(0).getTimestamp();
    }
}

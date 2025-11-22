package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.response.GuardHistory;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.repository.EventRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuardService {
    private final EventRepository eventRepository;
    private final UserService userService;

    public void entry(String login) {
        Optional<Event> lastInOutEventO = eventRepository.getLastInOutEvent(login);
        if (lastInOutEventO.isPresent() && lastInOutEventO.get().getType().equals(Event.Type.IN)) {
            throw new BadRequestException("Last guard event was the same");
        }

        createGuardEvent(login, Event.Type.IN);
    }

    public void exit(String login) {
        Optional<Event> lastInOutEventO = eventRepository.getLastInOutEvent(login);
        if (lastInOutEventO.isPresent() && lastInOutEventO.get().getType().equals(Event.Type.OUT)) {
            throw new BadRequestException("Last guard event was the same");
        }

        createGuardEvent(login, Event.Type.OUT);
    }

    public List<GuardHistory> getHistory(String login) {
        userService.getResidentByLogin(login); // To check that it is resident
        List<Event> events = eventRepository.getByTypeInAndUsrLoginOrderByTimestampDesc(
                List.of(Event.Type.IN, Event.Type.OUT), login
        );
        return events.stream().map(this::mapGuardEvent).toList();
    }

    public List<GuardHistory> getSelfHistory() {
        User resident = userService.getCurrentUserOrThrow();
        return getHistory(resident.getLogin());
    }

    private void createGuardEvent(String login, Event.Type type) {
        var event = new Event();
        event.setUsr(userService.getResidentByLogin(login));
        event.setType(type);
        eventRepository.save(event);
    }

    private GuardHistory mapGuardEvent(Event event) {
        return GuardHistory.builder()
                .type(GuardHistory.Type.fromEventType(event.getType()))
                .timestamp(event.getTimestamp())
                .build();
    }
}

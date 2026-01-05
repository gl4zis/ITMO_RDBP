package ru.itmo.is.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.is.dto.GuardHistory;
import ru.itmo.is.entity.Event;

@Component
public class EventMapper {
    public GuardHistory mapGuardEvent(Event event) {
        var history = new GuardHistory();
        history.setType(mapGuardEventType(event.getType()));
        history.setTimestamp(event.getTimestamp());
        return history;
    }

    private GuardHistory.TypeEnum mapGuardEventType(Event.Type type) {
        return switch (type) {
            case IN -> GuardHistory.TypeEnum.IN;
            case OUT -> GuardHistory.TypeEnum.OUT;
            default -> throw new IllegalArgumentException("Invalid event type");
        };
    }
}

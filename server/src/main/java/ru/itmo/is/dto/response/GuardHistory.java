package ru.itmo.is.dto.response;

import lombok.Builder;
import lombok.Getter;
import ru.itmo.is.entity.Event;

import java.time.LocalDateTime;

@Builder
@Getter
public class GuardHistory {
    private LocalDateTime timestamp;
    private Type type;

    public enum Type {
        IN,
        OUT;

        public static Type fromEventType(Event.Type type) {
            return switch (type) {
                case IN -> Type.IN;
                case OUT -> Type.OUT;
                default -> throw new IllegalArgumentException("Invalid event type");
            };
        }
    }
}

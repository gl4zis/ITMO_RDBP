package ru.itmo.is.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "eventSeq", sequenceName = "event_id_seq", allocationSize = 1)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventSeq")
    private Long id;
    @Enumerated(EnumType.STRING)
    private Type type;
    private LocalDateTime timestamp = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne
    @JoinColumn(name = "usr")
    private User usr;
    private Integer paymentSum;

    public enum Type {
        PAYMENT,
        IN,
        OUT,
        OCCUPATION,
        EVICTION,
        ROOM_CHANGE
    }
}

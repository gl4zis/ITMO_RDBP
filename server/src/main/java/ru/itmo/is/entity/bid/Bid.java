package ru.itmo.is.entity.bid;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.user.User;

import java.util.List;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "bidSeq", sequenceName = "bid_id_seq", allocationSize = 1)
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bidSeq")
    private Long id;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_PROCESS;
    private String text;
    @ManyToOne
    @JoinColumn(name = "sender")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "manager")
    private User manager;
    private String comment;
    @OneToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private Event event;
    @OneToMany(mappedBy = "bid")
    private List<BidFile> files;

    public enum Type {
        OCCUPATION,
        DEPARTURE,
        ROOM_CHANGE,
        EVICTION
    }

    @Getter
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor
    public enum Status {
        IN_PROCESS(true),
        PENDING_REVISION(true),
        ACCEPTED(false),
        DENIED(false);

        boolean editable;
    }
}

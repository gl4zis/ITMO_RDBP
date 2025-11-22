package ru.itmo.is.entity.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.user.User;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "notificationSeq", sequenceName = "notification_id_seq", allocationSize = 1)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificationSeq")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "bid_id")
    private Bid bid;
    @ManyToOne
    @JoinColumn(name = "receiver")
    private User receiver;

    private String text;
    @Enumerated(EnumType.STRING)
    private Status status = Status.CREATED;

    public enum Status {
        CREATED,
        READ
    }
}

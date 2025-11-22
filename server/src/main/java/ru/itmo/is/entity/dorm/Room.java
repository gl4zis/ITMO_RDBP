package ru.itmo.is.entity.dorm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.user.Resident;

import java.util.List;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "roomSeq", sequenceName = "room_id_seq", allocationSize = 1)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roomSeq")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "dormitory_id")
    private Dormitory dormitory;
    private int number;
    @Enumerated(EnumType.STRING)
    private Type type;
    private int capacity;
    private int floor;
    private int cost;
    @OneToMany(mappedBy = "room")
    private List<Resident> residents;
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Event> events;

    public enum Type {
        BLOCK,
        AISLE
    }
}

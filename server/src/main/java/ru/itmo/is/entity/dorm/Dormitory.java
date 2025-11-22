package ru.itmo.is.entity.dorm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "dormitorySeq", sequenceName = "dormitory_id_seq", allocationSize = 1)
public class Dormitory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dormitorySeq")
    private Integer id;
    private String address;

    @ManyToMany
    @JoinTable(
            name = "university_dormitory",
            joinColumns = @JoinColumn(name = "dormitory_id"),
            inverseJoinColumns = @JoinColumn(name = "university_id")
    )
    private List<University> universities;

    @OneToMany(mappedBy = "dormitory")
    private List<Room> rooms;
}

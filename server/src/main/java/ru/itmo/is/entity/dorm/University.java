package ru.itmo.is.entity.dorm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.user.Resident;

import java.util.List;

@Entity
@Getter
@Setter
@SequenceGenerator(name = "universitySeq", sequenceName = "university_id_seq", allocationSize = 1)
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "universitySeq")
    private Integer id;
    private String name;
    private String address;

    @ManyToMany
    @JoinTable(
            name = "university_dormitory",
            joinColumns = @JoinColumn(name = "university_id"),
            inverseJoinColumns = @JoinColumn(name = "dormitory_id")
    )
    private List<Dormitory> dormitories;

    @OneToMany(mappedBy = "university")
    private List<Resident> students;
}

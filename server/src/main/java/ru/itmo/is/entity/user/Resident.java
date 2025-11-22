package ru.itmo.is.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;

@Entity
@Getter
@Setter
public class Resident extends User {
    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public static Resident of(User user) {
        Resident resident = new Resident();
        resident.setLogin(user.getLogin());
        resident.setPassword(user.getPassword());
        resident.setRole(Role.RESIDENT);
        resident.setName(user.getName());
        resident.setSurname(user.getSurname());
        return resident;
    }
}

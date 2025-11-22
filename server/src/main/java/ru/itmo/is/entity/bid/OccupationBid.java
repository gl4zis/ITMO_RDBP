package ru.itmo.is.entity.bid;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.University;

@Entity
@Getter
@Setter
public class OccupationBid extends Bid {

    public OccupationBid() {
        this.setType(Type.OCCUPATION);
    }

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;
    @ManyToOne
    @JoinColumn(name = "dormitory_id")
    private Dormitory dormitory;
}

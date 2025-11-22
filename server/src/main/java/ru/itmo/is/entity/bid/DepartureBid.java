package ru.itmo.is.entity.bid;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class DepartureBid extends Bid {

    public DepartureBid() {
        this.setType(Type.DEPARTURE);
    }

    private LocalDate dayFrom;
    private LocalDate dayTo;
}

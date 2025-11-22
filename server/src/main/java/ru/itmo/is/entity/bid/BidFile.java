package ru.itmo.is.entity.bid;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BidFile {
    @Id
    private String key;
    @ManyToOne
    @JoinColumn(name = "bid_id")
    private Bid bid;
    private String name;
}

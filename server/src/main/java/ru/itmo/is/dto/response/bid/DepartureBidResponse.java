package ru.itmo.is.dto.response.bid;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.bid.DepartureBid;

import java.time.LocalDate;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartureBidResponse extends BidResponse {
    LocalDate dayFrom;
    LocalDate dayTo;

    public DepartureBidResponse(DepartureBid bid) {
        super(bid);
        this.dayFrom = bid.getDayFrom();
        this.dayTo = bid.getDayTo();
    }
}

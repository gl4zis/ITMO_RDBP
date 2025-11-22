package ru.itmo.is.dto.request.bid;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.validation.ValidDeparture;

import java.time.LocalDate;

@Getter
@Setter
@ValidDeparture
public class DepartureRequest extends BidRequest {
    @NotNull
    private LocalDate dayFrom;
    @NotNull
    private LocalDate dayTo;
}

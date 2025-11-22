package ru.itmo.is.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentResponse(
        Integer debt,
        Integer roomCost,
        LocalDateTime lastPaymentTime,
        List<History> history
) {
    public record History(LocalDateTime timestamp, String dormitory, int roomNumber, int sum) {}
}

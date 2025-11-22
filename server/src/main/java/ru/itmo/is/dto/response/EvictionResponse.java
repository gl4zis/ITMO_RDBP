package ru.itmo.is.dto.response;

import ru.itmo.is.dto.response.user.UserResponse;
import ru.itmo.is.entity.user.User;

public record EvictionResponse(UserResponse resident, Reason reason) {

    public static EvictionResponse nonPayment(User user) {
        return new EvictionResponse(new UserResponse(user), Reason.NON_PAYMENT);
    }

    public static EvictionResponse nonResidence(User user) {
        return new EvictionResponse(new UserResponse(user), Reason.NON_RESIDENCE);
    }

    public static EvictionResponse ruleViolation(User user) {
        return new EvictionResponse(new UserResponse(user), Reason.RULE_VIOLATION);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof EvictionResponse)) return false;
        EvictionResponse other = (EvictionResponse) obj;
        return other.resident.getLogin().equals(resident.getLogin());
    }

    public enum Reason {
        NON_PAYMENT,
        NON_RESIDENCE,
        RULE_VIOLATION,
    }
}

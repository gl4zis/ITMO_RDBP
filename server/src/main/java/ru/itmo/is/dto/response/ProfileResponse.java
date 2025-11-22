package ru.itmo.is.dto.response;

import jakarta.annotation.Nullable;
import ru.itmo.is.entity.user.User;

public record ProfileResponse(
        String name,
        String surname,
        User.Role role,
        @Nullable String university,
        @Nullable String dormitory,
        @Nullable Integer roomNumber
) { }

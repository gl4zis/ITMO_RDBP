package ru.itmo.is.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.is.entity.user.User;


@Getter
@Setter
public class RegisterRequest extends LoginRequest {
    @NotNull
    private String name;
    @NotNull
    private String surname;
    @NotNull
    private User.Role role;
}

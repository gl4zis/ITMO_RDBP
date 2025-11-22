package ru.itmo.is.dto.response.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.user.User;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserResponse {
    String login;
    String name;
    String surname;
    User.Role role;

    public UserResponse(User user) {
        this.login = user.getLogin();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.role = user.getRole();
    }
}

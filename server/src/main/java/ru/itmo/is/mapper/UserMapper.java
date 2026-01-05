package ru.itmo.is.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.is.dto.ProfileResponse;
import ru.itmo.is.dto.RegisterRequest;
import ru.itmo.is.dto.UserResponse;
import ru.itmo.is.dto.UserRole;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.PasswordManager;

@Component
public class UserMapper {

    public User toUserModel(RegisterRequest req) {
        var user = new User();
        user.setLogin(req.getLogin());
        user.setPassword(PasswordManager.hash(req.getPassword()));
        user.setName(req.getName());
        user.setSurname(req.getSurname());
        user.setRole(toUserRoleModel(req.getRole()));
        return user;
    }

    public UserRole toUserRoleDto(User.Role role) {
        return switch (role) {
            case NON_RESIDENT -> UserRole.NON_RESIDENT;
            case RESIDENT -> UserRole.RESIDENT;
            case MANAGER -> UserRole.MANAGER;
            case GUARD -> UserRole.GUARD;
        };
    }

    public ProfileResponse mapToProfile(User user) {
        if (user instanceof Resident resident) {
            return mapResidentToProfile(resident);
        }

        return new ProfileResponse(
                user.getLogin(),
                user.getName(),
                user.getSurname(),
                toUserRoleDto(user.getRole())
        );
    }

    public UserResponse mapUserResponse(User user) {
        return new UserResponse(
                user.getLogin(),
                user.getName(),
                user.getSurname(),
                toUserRoleDto(user.getRole())
        );
    }

    private ProfileResponse mapResidentToProfile(Resident resident) {
        var profile = new ProfileResponse(
                resident.getLogin(),
                resident.getName(),
                resident.getSurname(),
                toUserRoleDto(resident.getRole())
        );

        profile.setUniversity(resident.getUniversity().getName());
        profile.setDormitory(resident.getRoom().getDormitory().getAddress());
        profile.setRoomNumber(resident.getRoom().getNumber());

        return profile;
    }

    private User.Role toUserRoleModel(UserRole role) {
        return switch (role) {
            case NON_RESIDENT -> User.Role.NON_RESIDENT;
            case RESIDENT -> User.Role.RESIDENT;
            case MANAGER -> User.Role.MANAGER;
            case GUARD -> User.Role.GUARD;
        };
    }
}

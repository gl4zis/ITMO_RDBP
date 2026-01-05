package ru.itmo.is.mapper;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.PasswordManager;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final UniversityMapper universityMapper;
    private final DormitoryMapper dormitoryMapper;

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

    public ResidentResponse toResidentResponse(Resident resident, int debt, @Nullable LocalDateTime lastCameOut) {
        var response = new ResidentResponse();
        response.setLogin(resident.getLogin());
        response.setName(resident.getName());
        response.setSurname(resident.getSurname());
        response.setRole(toUserRoleDto(resident.getRole()));
        response.setUniversity(universityMapper.toResponse(resident.getUniversity()));
        response.setDormitory(dormitoryMapper.toResponse(resident.getRoom().getDormitory()));
        response.setRoomNumber(resident.getRoom().getNumber());
        response.setDebt(debt);
        response.setLastCameOut(lastCameOut);
        return response;
    }

    public ToEvictionResidentResponse nonPaymentEvictResponse(User user) {
        return new ToEvictionResidentResponse(mapUserResponse(user), EvictionReason.NON_PAYMENT);
    }

    public ToEvictionResidentResponse nonResidenceEvictResponse(User user) {
        return new ToEvictionResidentResponse(mapUserResponse(user), EvictionReason.NON_RESIDENCE);
    }

    public ToEvictionResidentResponse ruleViolationEvictResponse(User user) {
        return new ToEvictionResidentResponse(mapUserResponse(user), EvictionReason.RULE_VIOLATION);
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

package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.*;
import ru.itmo.is.dto.RegisterRequest;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.ConflictException;
import ru.itmo.is.exception.UnauthorizedException;
import ru.itmo.is.mapper.UserMapper;
import ru.itmo.is.repository.UserRepository;
import ru.itmo.is.security.JwtManager;
import ru.itmo.is.security.PasswordManager;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtManager jwtManager;
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final UserService userService;

    public OneFieldString register(RegisterRequest req) {
        return switch (req.getRole()) {
            case MANAGER -> registerManager(mapper.toUser(req));
            case NON_RESIDENT -> saveAndGetToken(mapper.toUser(req));
            default -> throw new BadRequestException("Invalid role");
        };
    }

    public OneFieldString login(LoginRequest req) {
        Optional<User> userO = userRepository.findById(req.getLogin());
        if (userO.isEmpty() || !PasswordManager.matches(req.getPassword(), userO.get().getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return new OneFieldString(jwtManager.createToken(userO.get()));
    }

    public void registerOther(RegisterRequest req) {
        saveAndGetToken(mapper.toUser(req));
    }

    public void changePassword(PasswordChangeRequest req) {
        User user = userService.getCurrentUserOrThrow();
        if (!PasswordManager.matches(req.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid old password");
        }
        user.setPassword(PasswordManager.hash(req.getNewPassword()));
        userRepository.save(user);
    }

    public ProfileResponse getProfile() {
        return mapToProfile(userService.getCurrentUserOrThrow());
    }

    private OneFieldString registerManager(User user) {
        if (isManagerExists()) {
            throw new UnauthorizedException("Invalid role");
        }
        return saveAndGetToken(user);
    }

    private OneFieldString saveAndGetToken(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new ConflictException("User already exists");
        }
        userRepository.save(user);
        return new OneFieldString(jwtManager.createToken(user));
    }

    private boolean isManagerExists() {
        return userRepository.countByRole(User.Role.MANAGER) > 0;
    }

    private ProfileResponse mapToProfile(User user) {
        if (user instanceof Resident resident) {
            return mapResidentToProfile(resident);
        }

        return new ProfileResponse(
                user.getName(),
                user.getSurname(),
                mapProfileRole(user.getRole())
        );
    }

    private ProfileResponse mapResidentToProfile(Resident resident) {
        var profile = new ProfileResponse(
                resident.getName(),
                resident.getSurname(),
                mapProfileRole(resident.getRole())
        );

        profile.setUniversity(resident.getUniversity().getName());
        profile.setDormitory(resident.getRoom().getDormitory().getAddress());
        profile.setRoomNumber(resident.getRoom().getNumber());

        return profile;
    }

    private UserRole mapProfileRole(User.Role role) {
        return switch (role) {
            case MANAGER -> UserRole.MANAGER;
            case NON_RESIDENT -> UserRole.NON_RESIDENT;
            case GUARD -> UserRole.GUARD;
            case RESIDENT -> UserRole.RESIDENT;
        };
    }
}

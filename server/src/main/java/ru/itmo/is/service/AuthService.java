package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.*;
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
    private final UserMapper userMapper;
    private final UserService userService;

    public AuthTokens register(RegisterRequest req) {
        return switch (req.getRole()) {
            case MANAGER -> registerManager(userMapper.toUserModel(req));
            case NON_RESIDENT -> saveAndGetToken(userMapper.toUserModel(req));
            default -> throw new BadRequestException("Invalid role");
        };
    }

    public AuthTokens login(LoginRequest req) {
        Optional<User> userO = userRepository.findById(req.getLogin());
        if (userO.isEmpty() || !PasswordManager.matches(req.getPassword(), userO.get().getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        User user = userO.get();
        return new AuthTokens(jwtManager.createAccessToken(user), jwtManager.createRefreshToken(user));
    }

    public void registerOther(RegisterRequest req) {
        saveAndGetToken(userMapper.toUserModel(req));
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
        return userMapper.mapToProfile(userService.getCurrentUserOrThrow());
    }

    private AuthTokens registerManager(User user) {
        if (isManagerExists()) {
            throw new UnauthorizedException("Invalid role");
        }
        return saveAndGetToken(user);
    }

    private AuthTokens saveAndGetToken(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new ConflictException("User already exists");
        }
        userRepository.save(user);
        return new AuthTokens(jwtManager.createAccessToken(user), jwtManager.createRefreshToken(user));
    }

    public AuthTokens refresh(String refreshToken) {
        Optional<String> login = jwtManager.getLoginFromRefreshToken(refreshToken);

        if (login.isEmpty()) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        Optional<User> userO = userRepository.findById(login.get());
        if (userO.isEmpty()) {
            throw new UnauthorizedException("User not found");
        }

        User user = userO.get();
        return new AuthTokens(jwtManager.createAccessToken(user), jwtManager.createRefreshToken(user));
    }

    private boolean isManagerExists() {
        return userRepository.countByRole(User.Role.MANAGER) > 0;
    }
}

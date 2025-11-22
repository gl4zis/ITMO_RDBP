package ru.itmo.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.request.LoginRequest;
import ru.itmo.is.dto.request.PasswordChangeRequest;
import ru.itmo.is.dto.request.RegisterRequest;
import ru.itmo.is.dto.response.ProfileResponse;
import ru.itmo.is.dto.OneFieldDto;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.security.Anonymous;
import ru.itmo.is.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Anonymous
    @PostMapping("/register")
    public OneFieldDto<String> register(@RequestBody @Valid RegisterRequest req) {
        return authService.register(req);
    }

    @Anonymous
    @PostMapping("/login")
    public OneFieldDto<String> login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping("/register-other")
    public void registerOther(@RequestBody @Valid RegisterRequest req) {
        authService.registerOther(req);
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody @Valid PasswordChangeRequest req) {
        authService.changePassword(req);
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile() {
        return authService.getProfile();
    }
}

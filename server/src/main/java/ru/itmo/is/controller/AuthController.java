package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.api.AuthApi;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.Anonymous;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final AuthService authService;

    @Override
    public ResponseEntity<Void> changePassword(PasswordChangeRequest passwordChangeRequest) {
        authService.changePassword(passwordChangeRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(authService.getProfile());
    }

    @Override
    @Anonymous
    public ResponseEntity<OneFieldString> login(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    @Anonymous
    public ResponseEntity<OneFieldString> register(RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> registerOther(RegisterRequest registerRequest) {
        authService.registerOther(registerRequest);
        return ResponseEntity.ok().build();
    }
}

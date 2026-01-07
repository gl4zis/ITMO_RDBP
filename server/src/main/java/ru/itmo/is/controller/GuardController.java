package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.api.GuardApi;
import ru.itmo.is.dto.GuardHistory;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.GuardService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GuardController implements GuardApi {
    private final GuardService guardService;

    @Override
    @RolesAllowed(User.Role.GUARD)
    public ResponseEntity<Void> entry(String login) {
        guardService.entry(login);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.GUARD)
    public ResponseEntity<Void> exit(String login) {
        guardService.exit(login);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<List<GuardHistory>> getHistory(String login) {
        return ResponseEntity.ok(guardService.getHistory(login));
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<List<GuardHistory>> getSelfHistory() {
        return ResponseEntity.ok(guardService.getSelfHistory());
    }
}

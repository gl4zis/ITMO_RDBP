package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.api.UserApi;
import ru.itmo.is.dto.ResidentResponse;
import ru.itmo.is.dto.ToEvictionResidentResponse;
import ru.itmo.is.dto.UserResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.BidService;
import ru.itmo.is.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RolesAllowed(User.Role.MANAGER)
public class UserController implements UserApi {
    private final UserService userService;
    private final BidService bidService;

    @Override
    public ResponseEntity<List<UserResponse>> getStaff() {
        return ResponseEntity.ok(userService.getStaff());
    }

    @Override
    public ResponseEntity<Void> fire(String login) {
        userService.fire(login);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ResidentResponse>> getResidents() {
        return ResponseEntity.ok(userService.getResidents());
    }

    @Override
    public ResponseEntity<List<ToEvictionResidentResponse>> getToEviction() {
        return ResponseEntity.ok(userService.getResidentsToEviction());
    }

    @Override
    public ResponseEntity<Void> evict(String login) {
        bidService.evictResident(login);
        return ResponseEntity.ok().build();
    }
}

package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.response.EvictionResponse;
import ru.itmo.is.dto.response.user.ResidentResponse;
import ru.itmo.is.dto.response.user.UserResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.BidService;
import ru.itmo.is.service.UserService;

import java.util.List;
import java.util.Set;

@RolesAllowed(User.Role.MANAGER)
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final BidService bidService;

    @GetMapping("/staff")
    public List<UserResponse> getStaff() {
        return userService.getStaff();
    }

    @DeleteMapping("/fire")
    public void fire(@RequestParam("login") String login) {
        userService.fire(login);
    }

    @GetMapping("/residents")
    public List<ResidentResponse> getResidents() {
        return userService.getResidents();
    }

    @GetMapping("/residents/to-eviction")
    public Set<EvictionResponse> getToEviction() {
        return userService.getResidentsToEviction();
    }

    @PostMapping("/residents/evict")
    public void evict(@RequestParam("login") String login) {
        bidService.evictResident(login);
    }
}

package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.response.GuardHistory;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.GuardService;

import java.util.List;

@RestController
@RequestMapping("/guard")
@RequiredArgsConstructor
public class GuardController {
    private final GuardService guardService;

    @RolesAllowed(User.Role.GUARD)
    @PostMapping("/entry")
    public void entry(@RequestParam("login") String login) {
        guardService.entry(login);
    }

    @RolesAllowed(User.Role.GUARD)
    @PostMapping("/exit")
    public void exit(@RequestParam("login") String login) {
        guardService.exit(login);
    }

    @RolesAllowed(User.Role.MANAGER)
    @GetMapping("/history")
    public List<GuardHistory> getHistory(@RequestParam("login") String login) {
        return guardService.getHistory(login);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @GetMapping("/history/self")
    public List<GuardHistory> getSelfHistory() {
        return guardService.getSelfHistory();
    }
}

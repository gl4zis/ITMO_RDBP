package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.response.EvictionResponse;
import ru.itmo.is.dto.response.user.ResidentResponse;
import ru.itmo.is.dto.response.user.UserResponse;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.ForbiddenException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.EventRepository;
import ru.itmo.is.repository.ResidentRepository;
import ru.itmo.is.repository.UserRepository;
import ru.itmo.is.security.SecurityContext;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SecurityContext securityContext;
    private final ResidentRepository residentRepository;

    public Resident getResidentByLogin(String login) {
        return residentRepository.findById(login)
                .orElseThrow(() -> new NotFoundException("Resident not found"));
    }

    public Resident getCurrentResidentOrThrow() {
        try {
            return getResidentByLogin(getCurrentUserOrThrow().getLogin());
        } catch (NotFoundException e) {
            throw new ForbiddenException("You are not resident");
        }
    }

    public User getCurrentUserOrThrow() {
        String login = securityContext.getUsername();
        if (login == null) {
            throw new ForbiddenException("You are not logged in");
        }
        return userRepository.findById(login).orElseThrow(() -> new ForbiddenException("You are not logged in"));
    }

    public List<UserResponse> getStaff() {
        return userRepository.getUsersByRoleIn(List.of(User.Role.GUARD, User.Role.MANAGER))
                .stream().map(UserResponse::new).toList();
    }

    public List<ResidentResponse> getResidents() {
        List<User> users = userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT));
        return users.stream()
                .map(user -> (Resident) user)
                .map(resident -> {
                    int debt = eventRepository.calculateResidentDebt(resident.getLogin());

                    LocalDateTime lastCameOut = eventRepository.getLastInOutEvent(resident.getLogin())
                            .map(Event::getTimestamp)
                            .orElse(null);

                    return new ResidentResponse(
                            resident,
                            debt,
                            lastCameOut
                    );
                })
                .toList();
    }

    public void fire(String login) {
        Optional<User> userO = userRepository.findById(login);
        if (userO.isEmpty()) {
            throw new NotFoundException("User not found");
        }
        if (userO.get().getRole() != User.Role.GUARD) {
            throw new BadRequestException("Wrong role");
        }
        userRepository.delete(userO.get());
    }

    public Set<EvictionResponse> getResidentsToEviction() {
        Set<EvictionResponse> response = new HashSet<>();

        List<String> toEvictionByDebtLogins = eventRepository.getResidentsToEvictionByDebt();
        List<User> toEvictionByDebt = userRepository.getByLoginIn(toEvictionByDebtLogins);
        toEvictionByDebt.forEach(u -> response.add(EvictionResponse.nonPayment(u)));

        List<UserEvent> userLastInOutEvents = userRepository.getUsersByRoleIn(List.of(User.Role.RESIDENT)).stream()
                .map(u -> new UserOptionalEvent(u, eventRepository.getLastInOutEvent(u.getLogin())))
                .filter(ue -> ue.event().isPresent())
                .map(uoe -> new UserEvent(uoe.user(), uoe.event().get()))
                .toList();

        List<User> toEvictionByResidence = userLastInOutEvents.stream()
                .filter(ue -> ue.event().getType().equals(Event.Type.OUT))
                .filter(ue -> ue.event().getTimestamp().isBefore(LocalDateTime.now().minusDays(7)))
                .map(UserEvent::user)
                .toList();
        toEvictionByResidence.forEach(u -> response.add(EvictionResponse.nonResidence(u)));

        List<User> toEvictionByRules = userLastInOutEvents.stream()
                .map(ue -> new UserTime(ue.user(), ue.event().getTimestamp().toLocalTime()))
                .filter(ut -> !ut.time().isBefore(LocalTime.MIDNIGHT) && ut.time.isBefore(LocalTime.of(6, 0)))
                .map(UserTime::user)
                .toList();
        toEvictionByRules.forEach(u -> response.add(EvictionResponse.ruleViolation(u)));

        return response;
    }

    private record UserOptionalEvent(User user, Optional<Event> event) {}
    private record UserEvent(User user, Event event) {}
    private record UserTime(User user, LocalTime time) {}
}

package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.request.UniversityRequest;
import ru.itmo.is.dto.response.UniversityResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.UniversityService;

import java.util.List;

@RestController
@RequestMapping("/university")
@RequiredArgsConstructor
public class UniversityController {
    private final UniversityService universityService;

    @GetMapping
    public List<UniversityResponse> getAllUniversities() {
        return universityService.getAllUniversities();
    }

    @GetMapping("/{id}")
    public UniversityResponse getUniversity(@PathVariable("id") int id) {
        return universityService.getUniversity(id);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping
    public void addUniversity(@RequestBody UniversityRequest req) {
        universityService.addUniversity(req);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PutMapping("/{id}")
    public void updateUniversity(@PathVariable("id") int id, @RequestBody UniversityRequest req) {
        universityService.updateUniversity(id, req);
    }

    @RolesAllowed(User.Role.MANAGER)
    @DeleteMapping("/{id}")
    public void deleteUniversity(@PathVariable("id") int id) {
        universityService.deleteUniversity(id);
    }
}

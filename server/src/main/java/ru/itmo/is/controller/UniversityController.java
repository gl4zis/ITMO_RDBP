package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.api.UniversityApi;
import ru.itmo.is.dto.UniversityRequest;
import ru.itmo.is.dto.UniversityResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.UniversityService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UniversityController implements UniversityApi {
    private final UniversityService universityService;

    @Override
    public ResponseEntity<List<UniversityResponse>> getAllUniversities() {
        return ResponseEntity.ok(universityService.getAllUniversities());
    }

    @Override
    public ResponseEntity<UniversityResponse> getUniversity(Integer id) {
        return ResponseEntity.ok(universityService.getUniversity(id));
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> addUniversity(UniversityRequest req) {
        universityService.addUniversity(req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> updateUniversity(Integer id, UniversityRequest req) {
        universityService.updateUniversity(id, req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> deleteUniversity(Integer id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.ok().build();
    }
}

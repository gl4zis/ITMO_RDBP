package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.api.DormitoryApi;
import ru.itmo.is.dto.DormitoryRequest;
import ru.itmo.is.dto.DormitoryResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.DormitoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DormitoryController implements DormitoryApi {
    private final DormitoryService dormitoryService;

    @Override
    public ResponseEntity<List<DormitoryResponse>> getAllDormitories() {
        return ResponseEntity.ok(dormitoryService.getAllDormitories());
    }

    @Override
    public ResponseEntity<DormitoryResponse> getDormitory(Integer id) {
        return ResponseEntity.ok(dormitoryService.getDormitory(id));
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> addDormitory(DormitoryRequest req) {
        dormitoryService.addDormitory(req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> updateDormitory(Integer id, DormitoryRequest req) {
        dormitoryService.updateDormitory(id, req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> deleteDormitory(Integer id) {
        dormitoryService.deleteDormitory(id);
        return ResponseEntity.ok().build();
    }
}

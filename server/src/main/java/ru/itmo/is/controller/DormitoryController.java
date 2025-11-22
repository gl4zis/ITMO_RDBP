package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.request.DormitoryRequest;
import ru.itmo.is.dto.response.DormitoryResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.DormitoryService;

import java.util.List;

@RestController
@RequestMapping("/dormitory")
@RequiredArgsConstructor
public class DormitoryController {
    private final DormitoryService dormitoryService;

    @GetMapping
    public List<DormitoryResponse> getAllDormitories() {
        return dormitoryService.getAllDormitories();
    }

    @GetMapping("/{id}")
    public DormitoryResponse getDormitory(@PathVariable("id") int id) {
        return dormitoryService.getDormitory(id);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping
    public void addDormitory(@RequestBody DormitoryRequest req) {
        dormitoryService.addDormitory(req);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PutMapping("/{id}")
    public void updateDormitory(@PathVariable("id") int id, @RequestBody DormitoryRequest req) {
        dormitoryService.updateDormitory(id, req);
    }

    @RolesAllowed(User.Role.MANAGER)
    @DeleteMapping("/{id}")
    public void deleteDormitory(@PathVariable("id") int id) {
        dormitoryService.deleteDormitory(id);
    }
}

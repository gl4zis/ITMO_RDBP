package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.request.RoomRequest;
import ru.itmo.is.dto.response.RoomResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllRooms();
    }

    @RolesAllowed({User.Role.RESIDENT})
    @GetMapping("/for-resident")
    public List<RoomResponse> getForResident() {
        return roomService.getForResident();
    }

    @GetMapping("/{id}")
    public RoomResponse getRoom(@PathVariable("id") int id) {
        return roomService.getRoom(id);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping
    public void addRoom(@RequestBody RoomRequest roomRequest) {
        roomService.addRoom(roomRequest);
    }

    @RolesAllowed(User.Role.MANAGER)
    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable("id") int id) {
        roomService.deleteRoom(id);
    }
}

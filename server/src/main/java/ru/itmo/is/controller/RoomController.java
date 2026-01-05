package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.api.RoomApi;
import ru.itmo.is.dto.RoomRequest;
import ru.itmo.is.dto.RoomResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.RoomService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController implements RoomApi {
    private final RoomService roomService;

    @Override
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<List<RoomResponse>> getForResident() {
        return ResponseEntity.ok(roomService.getForResident());
    }

    @Override
    public ResponseEntity<RoomResponse> getRoom(Integer id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> addRoom(RoomRequest roomRequest) {
        roomService.addRoom(roomRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> deleteRoom(Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }
}

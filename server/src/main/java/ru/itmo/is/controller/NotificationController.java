package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.is.api.NotificationApi;
import ru.itmo.is.dto.NotificationResponse;
import ru.itmo.is.service.NotificationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    @Override
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        return ResponseEntity.ok(notificationService.getUnreadNotifications());
    }

    @Override
    public ResponseEntity<Void> markAsRead(Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}

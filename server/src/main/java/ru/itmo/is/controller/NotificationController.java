package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.response.NotificationResponse;
import ru.itmo.is.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationService.getUnreadNotifications();
    }

    @PostMapping("/mark-as-read")
    public void markAsRead(@RequestParam("id") long id) {
        notificationService.markAsRead(id);
    }

    @PostMapping("/mark-all-as-read")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }
}

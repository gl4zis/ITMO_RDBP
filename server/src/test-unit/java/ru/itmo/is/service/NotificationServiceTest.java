package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.NotificationResponse;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.notification.Notification;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.ForbiddenException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private NotificationService notificationService;

    private User currentUser;
    private User otherUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setLogin("user1");

        otherUser = new User();
        otherUser.setLogin("user2");

        Bid bid = new Bid();
        bid.setId(1L);

        notification = new Notification();
        notification.setId(1L);
        notification.setBid(bid);
        notification.setText("Test notification");
        notification.setReceiver(currentUser);
        notification.setStatus(Notification.Status.CREATED);
    }

    @Test
    void testGetUnreadNotifications_ShouldReturnNotifications() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(notificationRepository.getByReceiverLoginAndStatus(
                "user1", Notification.Status.CREATED)).thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getUnreadNotifications();

        assertNotNull(result);
        assertEquals(1, result.size());
        NotificationResponse response = result.get(0);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getBidId());
        assertEquals("Test notification", response.getText());
    }

    @Test
    void testGetUnreadNotifications_WithNoNotifications_ShouldReturnEmptyList() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(notificationRepository.getByReceiverLoginAndStatus(
                "user1", Notification.Status.CREATED)).thenReturn(List.of());

        List<NotificationResponse> result = notificationService.getUnreadNotifications();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMarkAsRead_WhenNotificationExistsAndBelongsToUser_ShouldUpdateStatus() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        verify(notificationRepository).setReadStatus(1L);
    }

    @Test
    void testMarkAsRead_WhenNotificationNotFound_ShouldThrowNotFoundException() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            notificationService.markAsRead(1L);
        });

        verify(notificationRepository, never()).setReadStatus(anyLong());
    }

    @Test
    void testMarkAsRead_WhenNotificationBelongsToOtherUser_ShouldThrowForbiddenException() {
        notification.setReceiver(otherUser);
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class, () -> {
            notificationService.markAsRead(1L);
        });

        verify(notificationRepository, never()).setReadStatus(anyLong());
    }

    @Test
    void testMarkAllAsRead_ShouldUpdateAllNotifications() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);

        notificationService.markAllAsRead();

        verify(notificationRepository).setAllReadStatus("user1");
    }
}


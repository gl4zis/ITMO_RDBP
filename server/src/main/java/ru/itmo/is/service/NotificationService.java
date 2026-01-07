package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.NotificationResponse;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.notification.Notification;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.ForbiddenException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.NotificationRepository;
import ru.itmo.is.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<NotificationResponse> getUnreadNotifications() {
        User receiver = userService.getCurrentUserOrThrow();
        List<Notification> notifications = notificationRepository
                .getByReceiverLoginAndStatus(receiver.getLogin(), Notification.Status.CREATED);
        return notifications.stream()
                .map(this::mapNotification)
                .toList();
    }

    public void markAsRead(long id) {
        User receiver = userService.getCurrentUserOrThrow();
        Optional<Notification> notificationO = notificationRepository.findById(id);
        if (notificationO.isEmpty()) {
            throw new NotFoundException("Notification not found");
        }

        if (!notificationO.get().getReceiver().equals(receiver)) {
            throw new ForbiddenException("No rights");
        }

        notificationRepository.setReadStatus(id);
    }

    public void markAllAsRead() {
        User receiver = userService.getCurrentUserOrThrow();
        notificationRepository.setAllReadStatus(receiver.getLogin());
    }

    public void notifyManagersAboutNewBid(Bid bid) {
        if (bid.getStatus() == Bid.Status.IN_PROCESS) {
            List<Notification> notifications = userRepository.getUsersByRoleIn(List.of(User.Role.MANAGER))
                    .stream()
                    .map(manager -> {
                        Notification notification = new Notification();
                        notification.setBid(bid);
                        notification.setReceiver(manager);
                        notification.setText("Появилась новая заявка");
                        notification.setStatus(Notification.Status.CREATED);
                        return notification;
                    })
                    .toList();
            notificationRepository.saveAll(notifications);
        }
    }

    public void notifySenderAboutBidStatus(Bid bid) {
        String statusText = switch (bid.getStatus()) {
            case ACCEPTED -> "одобрена :)";
            case DENIED -> "отклонена ;(";
            default -> null;
        };

        if (statusText == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setBid(bid);
        notification.setReceiver(bid.getSender());
        notification.setText(String.format("Ваша заявка %s", statusText));
        notification.setStatus(Notification.Status.CREATED);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapNotification(Notification entity) {
        return new NotificationResponse(entity.getId(), entity.getBid().getId(), entity.getText());
    }
}

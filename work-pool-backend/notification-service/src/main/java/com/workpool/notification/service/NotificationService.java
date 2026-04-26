package com.workpool.notification.service;

import com.workpool.common.event.NotificationEvent;
import com.workpool.notification.model.Notification;
import com.workpool.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Notification createAndSend(NotificationEvent event) {
        Notification notification = Notification.builder()
                .recipientUserId(event.getRecipientUserId())
                .type(event.getType())
                .title(event.getTitle())
                .message(event.getMessage())
                .metadata(event.getMetadata())
                .read(false)
                .build();

        notification = notificationRepository.save(notification);

        // Push via WebSocket to the user's personal channel
        try {
            messagingTemplate.convertAndSendToUser(
                    event.getRecipientUserId(),
                    "/queue/notifications",
                    notification);
        } catch (Exception ex) {
            log.warn("WebSocket delivery failed for user {}: {}", event.getRecipientUserId(), ex.getMessage());
        }

        log.info("Notification sent to user {}: {}", event.getRecipientUserId(), event.getType());
        return notification;
    }

    public Page<Notification> getNotifications(String userId, boolean unreadOnly, Pageable pageable) {
        return unreadOnly
                ? notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientUserIdAndReadFalse(userId);
    }

    public void markAsRead(String notificationId, String userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipientUserId().equals(userId)) {
                n.setRead(true);
                n.setReadAt(Instant.now());
                notificationRepository.save(n);
            }
        });
    }

    public void markAllAsRead(String userId) {
        Page<Notification> unread = notificationRepository
                .findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId, Pageable.unpaged());
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
        });
        notificationRepository.saveAll(unread);
    }
}

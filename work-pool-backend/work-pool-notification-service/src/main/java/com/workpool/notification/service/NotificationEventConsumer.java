package com.workpool.notification.service;

import com.workpool.common.event.NotificationEvent;
import com.workpool.common.util.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_SEND, groupId = "notification-service")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event for user {}: {}", event.getRecipientUserId(), event.getType());
        notificationService.createAndSend(event);
    }
}

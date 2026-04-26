package com.workpool.notification.service;

import com.workpool.common.enums.NotificationType;
import com.workpool.common.event.NotificationEvent;
import com.workpool.notification.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Test
    void handleNotificationEvent_callsCreateAndSend() {
        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId("user-1")
                .type(NotificationType.BID_RECEIVED)
                .title("New bid")
                .message("You got a bid!")
                .metadata(Map.of("taskId", "task-1"))
                .createdAt(Instant.now())
                .build();

        Notification notif = Notification.builder()
                .id("notif-1")
                .recipientUserId("user-1")
                .build();
        when(notificationService.createAndSend(any())).thenReturn(notif);

        consumer.handleNotificationEvent(event);

        verify(notificationService).createAndSend(event);
    }
}

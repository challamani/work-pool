package com.workpool.notification.service;

import com.workpool.common.enums.NotificationType;
import com.workpool.common.event.NotificationEvent;
import com.workpool.notification.model.Notification;
import com.workpool.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationEvent buildEvent() {
        return NotificationEvent.builder()
                .recipientUserId("user-1")
                .type(NotificationType.BID_RECEIVED)
                .title("New bid")
                .message("You got a bid!")
                .metadata(Map.of("taskId", "task-1"))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void createAndSend_happyPath_savesAndSendsWebSocket() {
        NotificationEvent event = buildEvent();
        Notification saved = Notification.builder()
                .id("notif-1")
                .recipientUserId("user-1")
                .type(NotificationType.BID_RECEIVED)
                .build();
        when(notificationRepository.save(any())).thenReturn(saved);

        Notification result = notificationService.createAndSend(event);

        assertNotNull(result);
        verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void createAndSend_websocketFails_logsAndReturns() {
        NotificationEvent event = buildEvent();
        Notification saved = Notification.builder().id("notif-1").recipientUserId("user-1").build();
        when(notificationRepository.save(any())).thenReturn(saved);
        doThrow(new RuntimeException("WS error"))
                .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        Notification result = notificationService.createAndSend(event);

        assertNotNull(result);
    }

    @Test
    void getNotifications_unreadOnly_callsUnreadRepo() {
        Page<Notification> page = new PageImpl<>(List.of());
        when(notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(
                anyString(), any())).thenReturn(page);

        Page<Notification> result = notificationService.getNotifications(
                "user-1", true, Pageable.unpaged());

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getNotifications_all_callsAllRepo() {
        Page<Notification> page = new PageImpl<>(List.of());
        when(notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(
                anyString(), any())).thenReturn(page);

        Page<Notification> result = notificationService.getNotifications(
                "user-1", false, Pageable.unpaged());

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByRecipientUserIdAndReadFalse("user-1")).thenReturn(5L);

        long count = notificationService.getUnreadCount("user-1");

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_existingNotification_setsReadTrue() {
        Notification notif = Notification.builder()
                .id("notif-1")
                .recipientUserId("user-1")
                .read(false)
                .build();
        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any())).thenReturn(notif);

        notificationService.markAsRead("notif-1", "user-1");

        verify(notificationRepository).save(any());
    }

    @Test
    void markAsRead_differentUser_doesNotSave() {
        Notification notif = Notification.builder()
                .id("notif-1")
                .recipientUserId("user-2")
                .read(false)
                .build();
        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notif));

        notificationService.markAsRead("notif-1", "user-1");

        verify(notificationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void markAsRead_notificationNotFound_doesNothing() {
        when(notificationRepository.findById("missing")).thenReturn(Optional.empty());

        notificationService.markAsRead("missing", "user-1");

        verify(notificationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void markAllAsRead_marksAllUnreadNotifications() {
        Notification notif1 = Notification.builder().id("n1").recipientUserId("user-1")
                .read(false).build();
        Notification notif2 = Notification.builder().id("n2").recipientUserId("user-1")
                .read(false).build();
        Page<Notification> unread = new PageImpl<>(List.of(notif1, notif2));
        when(notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(
                anyString(), any())).thenReturn(unread);

        notificationService.markAllAsRead("user-1");

        verify(notificationRepository).saveAll(any());
    }
}

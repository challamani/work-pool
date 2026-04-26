package com.workpool.common.event;

import com.workpool.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String recipientUserId;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, String> metadata;
    private Instant createdAt;
}

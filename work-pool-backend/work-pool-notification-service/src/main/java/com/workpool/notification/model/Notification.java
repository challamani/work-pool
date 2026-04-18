package com.workpool.notification.model;

import com.workpool.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Indexed
    private String recipientUserId;

    private NotificationType type;
    private String title;
    private String message;
    private Map<String, String> metadata;

    @Builder.Default
    private boolean read = false;

    @CreatedDate
    private Instant createdAt;

    private Instant readAt;
}

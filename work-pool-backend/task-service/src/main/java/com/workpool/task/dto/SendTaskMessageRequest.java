package com.workpool.task.dto;

import jakarta.validation.constraints.NotBlank;

public record SendTaskMessageRequest(
        @NotBlank(message = "Recipient user id is required")
        String recipientUserId,
        @NotBlank(message = "Message is required")
        String message
) {
}

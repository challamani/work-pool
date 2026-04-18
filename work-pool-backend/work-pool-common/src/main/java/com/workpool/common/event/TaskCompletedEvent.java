package com.workpool.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletedEvent {
    private String taskId;
    private String publisherId;
    private String finisherId;
    private String bidId;
    private Instant completedAt;
}

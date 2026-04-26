package com.workpool.task.dto;

import com.workpool.common.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private String id;
    private String taskId;
    private String finisherId;
    private String finisherName;
    private BigDecimal proposedAmount;
    private String coverNote;
    private int estimatedDurationHours;
    private BidStatus status;
    private Instant createdAt;
}

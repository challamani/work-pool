package com.workpool.task.dto;

import com.workpool.common.enums.TaskCategory;
import com.workpool.common.enums.TaskStatus;
import com.workpool.common.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String publisherId;
    private String title;
    private String description;
    private TaskCategory category;
    private List<String> requiredSkills;
    private GeoLocation location;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private TaskStatus status;
    private Instant scheduledStart;
    private Instant scheduledEnd;
    private String assignedFinisherId;
    private BigDecimal agreedAmount;
    private List<String> tags;
    private long bidCount;
    private Instant createdAt;
    private Instant updatedAt;
}

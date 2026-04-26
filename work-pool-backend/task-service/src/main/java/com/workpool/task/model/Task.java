package com.workpool.task.model;

import com.workpool.common.enums.TaskCategory;
import com.workpool.common.enums.TaskStatus;
import com.workpool.common.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    @Indexed
    private String publisherId;

    private String title;
    private String description;

    @Indexed
    private TaskCategory category;

    private List<String> requiredSkills = new ArrayList<>();

    @Indexed
    private GeoLocation location;

    /** Estimated budget in INR */
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;

    @Indexed
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    private Instant scheduledStart;
    private Instant scheduledEnd;

    /** Accepted bid details */
    private String assignedFinisherId;
    private String acceptedBidId;
    private BigDecimal agreedAmount;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private String completionProofUrl;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

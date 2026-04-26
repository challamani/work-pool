package com.workpool.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private String id;
    private String taskId;
    private String raterId;
    private String ratedUserId;
    private int stars;
    private String review;
    private Instant createdAt;
}

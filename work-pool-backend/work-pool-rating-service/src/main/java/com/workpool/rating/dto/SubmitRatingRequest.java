package com.workpool.rating.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SubmitRatingRequest {

    @NotBlank
    private String taskId;

    @NotBlank
    private String ratedUserId;

    @Min(1) @Max(5)
    private int stars;

    @Size(max = 1000)
    private String review;
}

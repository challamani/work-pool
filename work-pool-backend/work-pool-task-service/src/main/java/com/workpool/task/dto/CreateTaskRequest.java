package com.workpool.task.dto;

import com.workpool.common.enums.TaskCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CreateTaskRequest {

    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String description;

    @NotNull
    private TaskCategory category;

    private List<String> requiredSkills;

    @NotBlank
    private String city;

    @NotBlank
    private String district;

    @NotBlank
    private String state;

    private String pincode;

    private double latitude;
    private double longitude;

    @NotNull
    @DecimalMin("100.00")
    private BigDecimal budgetMin;

    @NotNull
    @DecimalMax("1000000.00")
    private BigDecimal budgetMax;

    private Instant scheduledStart;
    private Instant scheduledEnd;

    private List<String> tags;
}

package com.workpool.task.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceBidRequest {

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal proposedAmount;

    @Size(max = 1000)
    private String coverNote;

    @Min(1) @Max(720)
    private int estimatedDurationHours;
}

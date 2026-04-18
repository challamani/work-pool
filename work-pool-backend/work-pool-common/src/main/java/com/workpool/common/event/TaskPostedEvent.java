package com.workpool.common.event;

import com.workpool.common.enums.TaskCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskPostedEvent {
    private String taskId;
    private String publisherId;
    private String title;
    private TaskCategory category;
    private List<String> requiredSkills;
    private String city;
    private String district;
    private String state;
    private double latitude;
    private double longitude;
    private BigDecimal budget;
    private Instant postedAt;
}

package com.workpool.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingSummary {
    private String userId;
    private double averageRating;
    private int totalRatings;
    private int fiveStars;
    private int fourStars;
    private int threeStars;
    private int twoStars;
    private int oneStar;
}

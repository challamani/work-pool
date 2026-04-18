package com.workpool.user.dto;

import com.workpool.common.enums.UserRole;
import com.workpool.common.enums.VerificationStatus;
import com.workpool.common.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;
    private Set<UserRole> roles;
    private Set<String> skills;
    private GeoLocation location;
    private int serviceRadiusKm;
    private String bio;
    private VerificationStatus aadhaarVerification;
    private double averageRating;
    private int totalRatings;
    private boolean emailVerified;
    private Instant createdAt;
}

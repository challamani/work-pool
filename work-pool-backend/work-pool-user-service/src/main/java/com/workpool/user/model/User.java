package com.workpool.user.model;

import com.workpool.common.enums.UserRole;
import com.workpool.common.enums.VerificationStatus;
import com.workpool.common.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fullName;

    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    private String profileImageUrl;

    private String aadhaarNumber;

    @Builder.Default
    private VerificationStatus aadhaarVerification = VerificationStatus.UNVERIFIED;

    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @Builder.Default
    private Set<String> skills = new HashSet<>();

    private GeoLocation location;

    /** Radius (km) within which the user is willing to travel for work */
    @Builder.Default
    private int serviceRadiusKm = 20;

    private String bio;

    /** OAuth2 provider (google / facebook) */
    private String oauthProvider;
    private String oauthProviderId;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean emailVerified = false;

    /** Running average rating as a task finisher */
    @Builder.Default
    private double averageRating = 0.0;

    @Builder.Default
    private int totalRatings = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

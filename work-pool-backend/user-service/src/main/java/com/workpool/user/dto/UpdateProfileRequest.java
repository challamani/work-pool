package com.workpool.user.dto;

import com.workpool.common.model.GeoLocation;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String fullName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phoneNumber;

    private String profileImageUrl;

    @Size(max = 12, min = 12, message = "Aadhaar number must be 12 digits")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar must contain only digits")
    private String aadhaarNumber;

    private Set<String> skills;

    private GeoLocation location;

    @Min(1) @Max(200)
    private Integer serviceRadiusKm;

    @Size(max = 500)
    private String bio;
}

package com.workpool.user.controller;

import com.workpool.common.dto.ApiResponse;
import com.workpool.user.dto.UpdateProfileRequest;
import com.workpool.user.dto.UserProfileResponse;
import com.workpool.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "Manage user profile and skills")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @Operation(summary = "Update current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(userId, request), "Profile updated"));
    }

    @Operation(summary = "Get public profile of a user")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getPublicProfile(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }
}

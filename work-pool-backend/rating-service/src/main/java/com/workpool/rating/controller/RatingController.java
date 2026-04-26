package com.workpool.rating.controller;

import com.workpool.common.dto.ApiResponse;
import com.workpool.rating.dto.RatingResponse;
import com.workpool.rating.dto.SubmitRatingRequest;
import com.workpool.rating.dto.UserRatingSummary;
import com.workpool.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ratings & Reviews", description = "Post-completion ratings and trust profile")
@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "Submit a rating for a task finisher", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SubmitRatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ratingService.submitRating(userId, request)));
    }

    @Operation(summary = "Get all ratings for a user")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getRatings(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                ratingService.getRatingsForUser(userId, PageRequest.of(page, size))));
    }

    @Operation(summary = "Get rating summary for a user")
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<ApiResponse<UserRatingSummary>> getSummary(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(ratingService.getSummaryForUser(userId)));
    }
}

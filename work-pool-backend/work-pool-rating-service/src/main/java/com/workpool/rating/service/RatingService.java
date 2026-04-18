package com.workpool.rating.service;

import com.workpool.common.enums.NotificationType;
import com.workpool.common.event.NotificationEvent;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.common.util.KafkaTopics;
import com.workpool.rating.dto.RatingResponse;
import com.workpool.rating.dto.SubmitRatingRequest;
import com.workpool.rating.dto.UserRatingSummary;
import com.workpool.rating.model.Rating;
import com.workpool.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RatingResponse submitRating(String raterId, SubmitRatingRequest request) {
        if (ratingRepository.existsByTaskIdAndRaterId(request.getTaskId(), raterId)) {
            throw new WorkPoolException("ALREADY_RATED", "You have already rated this task");
        }

        Rating rating = Rating.builder()
                .taskId(request.getTaskId())
                .raterId(raterId)
                .ratedUserId(request.getRatedUserId())
                .stars(request.getStars())
                .review(request.getReview())
                .build();

        rating = ratingRepository.save(rating);

        // Publish event for user-service to update aggregate rating
        kafkaTemplate.send(KafkaTopics.RATING_SUBMITTED, rating);

        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(request.getRatedUserId())
                .type(NotificationType.RATING_RECEIVED)
                .title("You received a rating!")
                .message("You received a " + request.getStars() + "-star rating.")
                .metadata(Map.of("taskId", request.getTaskId(), "stars", String.valueOf(request.getStars())))
                .createdAt(Instant.now())
                .build());

        log.info("Rating submitted for user {} by {}: {} stars", request.getRatedUserId(), raterId, request.getStars());
        return toResponse(rating);
    }

    public Page<RatingResponse> getRatingsForUser(String userId, Pageable pageable) {
        return ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public UserRatingSummary getSummaryForUser(String userId) {
        List<Rating> ratings = ratingRepository.findStarsByRatedUserId(userId);

        if (ratings.isEmpty()) {
            return UserRatingSummary.builder().userId(userId).build();
        }

        double avg = ratings.stream().mapToInt(Rating::getStars).average().orElse(0);
        Map<Integer, Long> counts = ratings.stream()
                .collect(Collectors.groupingBy(Rating::getStars, Collectors.counting()));

        return UserRatingSummary.builder()
                .userId(userId)
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalRatings(ratings.size())
                .fiveStars(counts.getOrDefault(5, 0L).intValue())
                .fourStars(counts.getOrDefault(4, 0L).intValue())
                .threeStars(counts.getOrDefault(3, 0L).intValue())
                .twoStars(counts.getOrDefault(2, 0L).intValue())
                .oneStar(counts.getOrDefault(1, 0L).intValue())
                .build();
    }

    private RatingResponse toResponse(Rating r) {
        return RatingResponse.builder()
                .id(r.getId())
                .taskId(r.getTaskId())
                .raterId(r.getRaterId())
                .ratedUserId(r.getRatedUserId())
                .stars(r.getStars())
                .review(r.getReview())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

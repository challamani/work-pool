package com.workpool.rating.service;

import com.workpool.common.exception.WorkPoolException;
import com.workpool.rating.dto.RatingResponse;
import com.workpool.rating.dto.SubmitRatingRequest;
import com.workpool.rating.dto.UserRatingSummary;
import com.workpool.rating.model.Rating;
import com.workpool.rating.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private RatingService ratingService;

    private SubmitRatingRequest buildRequest() {
        SubmitRatingRequest request = new SubmitRatingRequest();
        request.setTaskId("task-1");
        request.setRatedUserId("fin-1");
        request.setStars(5);
        request.setReview("Excellent work!");
        return request;
    }

    @Test
    void submitRating_happyPath_returnsRatingResponse() {
        SubmitRatingRequest request = buildRequest();
        when(ratingRepository.existsByTaskIdAndRaterId("task-1", "rater-1")).thenReturn(false);

        Rating saved = Rating.builder()
                .id("rating-1")
                .taskId("task-1")
                .raterId("rater-1")
                .ratedUserId("fin-1")
                .stars(5)
                .review("Excellent work!")
                .build();
        when(ratingRepository.save(any())).thenReturn(saved);

        RatingResponse response = ratingService.submitRating("rater-1", request);

        assertNotNull(response);
        assertEquals("rating-1", response.getId());
        verify(kafkaTemplate).send(anyString(), any());
    }

    @Test
    void submitRating_alreadyRated_throwsWorkPoolException() {
        SubmitRatingRequest request = buildRequest();
        when(ratingRepository.existsByTaskIdAndRaterId("task-1", "rater-1")).thenReturn(true);

        WorkPoolException ex = assertThrows(WorkPoolException.class,
                () -> ratingService.submitRating("rater-1", request));
        assertEquals("ALREADY_RATED", ex.getErrorCode());
    }

    @Test
    void getRatingsForUser_returnsPage() {
        Rating rating = Rating.builder().id("r-1").ratedUserId("user-1").stars(4).build();
        Page<Rating> page = new PageImpl<>(List.of(rating));
        when(ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(page);

        Page<RatingResponse> result = ratingService.getRatingsForUser("user-1", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getSummaryForUser_noRatings_returnsEmptySummary() {
        when(ratingRepository.findStarsByRatedUserId("user-1")).thenReturn(Collections.emptyList());

        UserRatingSummary summary = ratingService.getSummaryForUser("user-1");

        assertNotNull(summary);
        assertEquals("user-1", summary.getUserId());
        assertEquals(0, summary.getTotalRatings());
    }

    @Test
    void getSummaryForUser_withRatings_returnsSummary() {
        Rating r1 = Rating.builder().id("r1").ratedUserId("user-1").stars(5).build();
        Rating r2 = Rating.builder().id("r2").ratedUserId("user-1").stars(4).build();
        Rating r3 = Rating.builder().id("r3").ratedUserId("user-1").stars(3).build();
        Rating r4 = Rating.builder().id("r4").ratedUserId("user-1").stars(2).build();
        Rating r5 = Rating.builder().id("r5").ratedUserId("user-1").stars(1).build();
        when(ratingRepository.findStarsByRatedUserId("user-1"))
                .thenReturn(List.of(r1, r2, r3, r4, r5));

        UserRatingSummary summary = ratingService.getSummaryForUser("user-1");

        assertNotNull(summary);
        assertEquals(5, summary.getTotalRatings());
        assertEquals(1, summary.getFiveStars());
        assertEquals(1, summary.getFourStars());
        assertEquals(1, summary.getThreeStars());
        assertEquals(1, summary.getTwoStars());
        assertEquals(1, summary.getOneStar());
    }

    @Test
    void getSummaryForUser_onlyFiveStars_averageIs5() {
        Rating r1 = Rating.builder().id("r1").ratedUserId("user-1").stars(5).build();
        Rating r2 = Rating.builder().id("r2").ratedUserId("user-1").stars(5).build();
        when(ratingRepository.findStarsByRatedUserId("user-1")).thenReturn(List.of(r1, r2));

        UserRatingSummary summary = ratingService.getSummaryForUser("user-1");

        assertEquals(5.0, summary.getAverageRating());
    }
}

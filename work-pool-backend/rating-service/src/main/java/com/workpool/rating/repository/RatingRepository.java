package com.workpool.rating.repository;

import com.workpool.rating.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface RatingRepository extends MongoRepository<Rating, String> {

    Page<Rating> findByRatedUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Rating> findByTaskIdAndRaterId(String taskId, String raterId);

    boolean existsByTaskIdAndRaterId(String taskId, String raterId);

    @Query(value = "{ 'ratedUserId': ?0 }", fields = "{ 'stars': 1 }")
    java.util.List<Rating> findStarsByRatedUserId(String userId);
}

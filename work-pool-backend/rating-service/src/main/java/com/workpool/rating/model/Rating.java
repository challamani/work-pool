package com.workpool.rating.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ratings")
@CompoundIndex(name = "task_rater_idx", def = "{'taskId': 1, 'raterId': 1}", unique = true)
public class Rating {

    @Id
    private String id;

    @Indexed
    private String taskId;

    /** Who gave the rating (publisher rates finisher) */
    private String raterId;

    /** Who received the rating (finisher being rated) */
    @Indexed
    private String ratedUserId;

    /** 1-5 star rating */
    private int stars;

    /** Optional written review */
    private String review;

    @CreatedDate
    private Instant createdAt;
}

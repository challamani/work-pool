package com.workpool.task.repository;

import com.workpool.common.enums.BidStatus;
import com.workpool.task.model.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends MongoRepository<Bid, String> {

    List<Bid> findByTaskId(String taskId);

    List<Bid> findByFinisherId(String finisherId);

    Optional<Bid> findByTaskIdAndFinisherId(String taskId, String finisherId);

    boolean existsByTaskIdAndFinisherId(String taskId, String finisherId);

    long countByTaskIdAndStatus(String taskId, BidStatus status);
}

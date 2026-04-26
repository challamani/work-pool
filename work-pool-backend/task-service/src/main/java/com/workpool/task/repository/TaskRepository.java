package com.workpool.task.repository;

import com.workpool.common.enums.TaskCategory;
import com.workpool.common.enums.TaskStatus;
import com.workpool.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByStatusIn(List<TaskStatus> statuses, Pageable pageable);

    Page<Task> findByPublisherId(String publisherId, Pageable pageable);

    Page<Task> findByAssignedFinisherId(String finisherId, Pageable pageable);

    /** Returns tasks with status OPEN or BIDDING (biddable) filtered by state. */
    @Query("{ 'status': { $in: ['OPEN', 'BIDDING'] }, 'location.state': ?0 }")
    Page<Task> findBiddableTasksByState(String state, Pageable pageable);

    /** Returns tasks with status OPEN or BIDDING (biddable) filtered by city. */
    @Query("{ 'status': { $in: ['OPEN', 'BIDDING'] }, 'location.city': ?0 }")
    Page<Task> findBiddableTasksByCity(String city, Pageable pageable);

    /** Returns tasks with status OPEN or BIDDING (biddable) matching the given skills and state. */
    @Query("{ 'status': { $in: ['OPEN', 'BIDDING'] }, 'requiredSkills': { $in: ?0 }, 'location.state': ?1 }")
    Page<Task> findBiddableTasksBySkillsAndState(List<String> skills, String state, Pageable pageable);

    /** Returns tasks with status OPEN or BIDDING (biddable) filtered by category and state. */
    @Query("{ 'status': { $in: ['OPEN', 'BIDDING'] }, 'category': ?0, 'location.state': ?1 }")
    Page<Task> findBiddableTasksByCategoryAndState(TaskCategory category, String state, Pageable pageable);

    long countByPublisherIdAndStatus(String publisherId, TaskStatus status);
}

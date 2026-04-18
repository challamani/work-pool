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

    Page<Task> findByPublisherId(String publisherId, Pageable pageable);

    Page<Task> findByAssignedFinisherId(String finisherId, Pageable pageable);

    @Query("{ 'status': 'OPEN', 'location.state': ?0 }")
    Page<Task> findOpenTasksByState(String state, Pageable pageable);

    @Query("{ 'status': 'OPEN', 'location.city': ?0 }")
    Page<Task> findOpenTasksByCity(String city, Pageable pageable);

    @Query("{ 'status': 'OPEN', 'requiredSkills': { $in: ?0 }, 'location.state': ?1 }")
    Page<Task> findMatchingTasksBySkillsAndState(List<String> skills, String state, Pageable pageable);

    @Query("{ 'status': 'OPEN', 'category': ?0, 'location.state': ?1 }")
    Page<Task> findOpenTasksByCategoryAndState(TaskCategory category, String state, Pageable pageable);

    long countByPublisherIdAndStatus(String publisherId, TaskStatus status);
}

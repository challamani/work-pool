package com.workpool.task.service;

import com.workpool.common.enums.BidStatus;
import com.workpool.common.enums.TaskStatus;
import com.workpool.common.event.NotificationEvent;
import com.workpool.common.event.TaskCompletedEvent;
import com.workpool.common.event.TaskPostedEvent;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.UnauthorizedException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.common.model.GeoLocation;
import com.workpool.common.enums.NotificationType;
import com.workpool.common.util.KafkaTopics;
import com.workpool.task.dto.*;
import com.workpool.task.model.Bid;
import com.workpool.task.model.Task;
import com.workpool.task.repository.BidRepository;
import com.workpool.task.repository.TaskRepository;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final BidRepository bidRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TaskResponse createTask(String publisherId, CreateTaskRequest request) {
        GeoLocation location = new GeoLocation(
                request.getLatitude(), request.getLongitude(),
                request.getCity(), request.getDistrict(),
                request.getState(), request.getPincode());

        Task task = Task.builder()
                .publisherId(publisherId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .requiredSkills(request.getRequiredSkills())
                .location(location)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .tags(request.getTags())
                .status(TaskStatus.OPEN)
                .build();

        task = taskRepository.save(task);
        publishTaskPostedEvent(task);
        log.info("Task created: {} by publisher: {}", task.getId(), publisherId);
        return toTaskResponse(task);
    }

    public Page<TaskResponse> getOpenTasks(String state, Pageable pageable) {
        return state != null
                ? taskRepository.findOpenTasksByState(state, pageable).map(this::toTaskResponse)
                : taskRepository.findByStatus(TaskStatus.OPEN, pageable).map(this::toTaskResponse);
    }

    public Page<TaskResponse> getMyPublishedTasks(String publisherId, Pageable pageable) {
        return taskRepository.findByPublisherId(publisherId, pageable).map(this::toTaskResponse);
    }

    public Page<TaskResponse> getMyAssignedTasks(String finisherId, Pageable pageable) {
        return taskRepository.findByAssignedFinisherId(finisherId, pageable).map(this::toTaskResponse);
    }

    public TaskResponse getTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        return toTaskResponse(task);
    }

    public BidResponse placeBid(String taskId, String finisherId, String finisherName, PlaceBidRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (task.getStatus() != TaskStatus.OPEN && task.getStatus() != TaskStatus.BIDDING) {
            throw new WorkPoolException("TASK_NOT_OPEN", "Task is not accepting bids");
        }
        if (task.getPublisherId().equals(finisherId)) {
            throw new WorkPoolException("SELF_BID", "Publisher cannot bid on their own task");
        }
        if (bidRepository.existsByTaskIdAndFinisherId(taskId, finisherId)) {
            throw new WorkPoolException("DUPLICATE_BID", "You have already placed a bid on this task");
        }

        Bid bid = Bid.builder()
                .taskId(taskId)
                .finisherId(finisherId)
                .finisherName(finisherName)
                .proposedAmount(request.getProposedAmount())
                .coverNote(request.getCoverNote())
                .estimatedDurationHours(request.getEstimatedDurationHours())
                .status(BidStatus.PENDING)
                .build();
        bid = bidRepository.save(bid);

        // Move task to BIDDING state if still OPEN
        if (task.getStatus() == TaskStatus.OPEN) {
            task.setStatus(TaskStatus.BIDDING);
            taskRepository.save(task);
        }

        // Notify publisher
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(task.getPublisherId())
                .type(NotificationType.BID_RECEIVED)
                .title("New bid on your task")
                .message(finisherName + " placed a bid of ₹" + request.getProposedAmount() + " on your task: " + task.getTitle())
                .metadata(Map.of("taskId", taskId, "bidId", bid.getId()))
                .createdAt(Instant.now())
                .build());

        kafkaTemplate.send(KafkaTopics.BID_PLACED, bid);
        return toBidResponse(bid);
    }

    public BidResponse acceptBid(String taskId, String publisherId, String bidId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (!task.getPublisherId().equals(publisherId)) {
            throw new UnauthorizedException("Only the task publisher can accept bids");
        }
        if (task.getStatus() != TaskStatus.OPEN && task.getStatus() != TaskStatus.BIDDING) {
            throw new WorkPoolException("TASK_NOT_OPEN", "Task is not in a state to accept bids");
        }

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid", bidId));

        if (!bid.getTaskId().equals(taskId)) {
            throw new WorkPoolException("BID_MISMATCH", "Bid does not belong to this task");
        }

        // Accept this bid, reject all others
        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);

        bidRepository.findByTaskId(taskId).stream()
                .filter(b -> !b.getId().equals(bidId) && b.getStatus() == BidStatus.PENDING)
                .forEach(b -> {
                    b.setStatus(BidStatus.REJECTED);
                    bidRepository.save(b);
                    kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                            .recipientUserId(b.getFinisherId())
                            .type(NotificationType.BID_REJECTED)
                            .title("Bid not selected")
                            .message("Your bid on '" + task.getTitle() + "' was not selected.")
                            .metadata(Map.of("taskId", taskId))
                            .createdAt(Instant.now())
                            .build());
                });

        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedFinisherId(bid.getFinisherId());
        task.setAcceptedBidId(bidId);
        task.setAgreedAmount(bid.getProposedAmount());
        taskRepository.save(task);

        kafkaTemplate.send(KafkaTopics.BID_ACCEPTED, bid);
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(bid.getFinisherId())
                .type(NotificationType.BID_ACCEPTED)
                .title("Your bid was accepted!")
                .message("Your bid on '" + task.getTitle() + "' was accepted. Start working!")
                .metadata(Map.of("taskId", taskId, "bidId", bidId))
                .createdAt(Instant.now())
                .build());

        return toBidResponse(bid);
    }

    public TaskResponse markComplete(String taskId, String finisherId, String completionProofUrl) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (!finisherId.equals(task.getAssignedFinisherId())) {
            throw new UnauthorizedException("Only the assigned finisher can mark task complete");
        }
        if (task.getStatus() != TaskStatus.ASSIGNED && task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new WorkPoolException("INVALID_STATUS", "Task cannot be marked complete in current state");
        }

        task.setStatus(TaskStatus.PENDING_REVIEW);
        task.setCompletionProofUrl(completionProofUrl);
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);

        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(task.getPublisherId())
                .type(NotificationType.TASK_COMPLETED)
                .title("Task marked as complete")
                .message("The finisher has marked task '" + task.getTitle() + "' as completed. Please review and confirm.")
                .metadata(Map.of("taskId", taskId))
                .createdAt(Instant.now())
                .build());

        return toTaskResponse(task);
    }

    public TaskResponse confirmCompletion(String taskId, String publisherId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (!publisherId.equals(task.getPublisherId())) {
            throw new UnauthorizedException("Only the task publisher can confirm completion");
        }
        if (task.getStatus() != TaskStatus.PENDING_REVIEW) {
            throw new WorkPoolException("INVALID_STATUS", "Task is not pending review");
        }

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        TaskCompletedEvent event = new TaskCompletedEvent(
                task.getId(), task.getPublisherId(), task.getAssignedFinisherId(),
                task.getAcceptedBidId(), Instant.now());
        kafkaTemplate.send(KafkaTopics.TASK_COMPLETED, event);

        return toTaskResponse(task);
    }

    public List<BidResponse> getBidsForTask(String taskId, String requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (!task.getPublisherId().equals(requesterId)) {
            throw new UnauthorizedException("Only the publisher can view all bids");
        }
        return bidRepository.findByTaskId(taskId).stream()
                .map(this::toBidResponse)
                .collect(Collectors.toList());
    }

    public void sendTaskMessage(String taskId, String senderId, String senderName, SendTaskMessageRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (task.getAssignedFinisherId() == null) {
            throw new WorkPoolException("TASK_NOT_ASSIGNED", "Messaging is enabled only after a finisher is assigned");
        }
        boolean senderIsPublisher = senderId.equals(task.getPublisherId());
        boolean senderIsFinisher = senderId.equals(task.getAssignedFinisherId());
        if (!senderIsPublisher && !senderIsFinisher) {
            throw new UnauthorizedException("Only the task publisher or assigned finisher can send messages");
        }
        if (senderId.equals(request.recipientUserId())) {
            throw new WorkPoolException("INVALID_RECIPIENT", "Sender and recipient cannot be the same");
        }
        boolean recipientIsPublisher = request.recipientUserId().equals(task.getPublisherId());
        boolean recipientIsFinisher = request.recipientUserId().equals(task.getAssignedFinisherId());
        if (!recipientIsPublisher && !recipientIsFinisher) {
            throw new WorkPoolException("INVALID_RECIPIENT", "Recipient must be task publisher or assigned finisher");
        }
        String effectiveSenderName = senderName == null || senderName.isBlank() ? "Work Pool user" : senderName;

        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(request.recipientUserId())
                .type(NotificationType.DIRECT_MESSAGE)
                .title("New message on task: " + task.getTitle())
                .message(request.message())
                .metadata(Map.of(
                        "taskId", taskId,
                        "senderId", senderId,
                        "senderName", effectiveSenderName))
                .createdAt(Instant.now())
                .build());
    }

    private void publishTaskPostedEvent(Task task) {
        TaskPostedEvent event = new TaskPostedEvent(
                task.getId(), task.getPublisherId(), task.getTitle(), task.getCategory(),
                task.getRequiredSkills(),
                task.getLocation() != null ? task.getLocation().getCity() : null,
                task.getLocation() != null ? task.getLocation().getDistrict() : null,
                task.getLocation() != null ? task.getLocation().getState() : null,
                task.getLocation() != null ? task.getLocation().getLatitude() : 0,
                task.getLocation() != null ? task.getLocation().getLongitude() : 0,
                task.getBudgetMax(), Instant.now());
        kafkaTemplate.send(KafkaTopics.TASK_POSTED, event);
    }

    private TaskResponse toTaskResponse(Task task) {
        long bidCount = bidRepository.countByTaskIdAndStatus(task.getId(), BidStatus.PENDING);
        return TaskResponse.builder()
                .id(task.getId())
                .publisherId(task.getPublisherId())
                .title(task.getTitle())
                .description(task.getDescription())
                .category(task.getCategory())
                .requiredSkills(task.getRequiredSkills())
                .location(task.getLocation())
                .budgetMin(task.getBudgetMin())
                .budgetMax(task.getBudgetMax())
                .status(task.getStatus())
                .scheduledStart(task.getScheduledStart())
                .scheduledEnd(task.getScheduledEnd())
                .assignedFinisherId(task.getAssignedFinisherId())
                .agreedAmount(task.getAgreedAmount())
                .tags(task.getTags())
                .bidCount(bidCount)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private BidResponse toBidResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .taskId(bid.getTaskId())
                .finisherId(bid.getFinisherId())
                .finisherName(bid.getFinisherName())
                .proposedAmount(bid.getProposedAmount())
                .coverNote(bid.getCoverNote())
                .estimatedDurationHours(bid.getEstimatedDurationHours())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}

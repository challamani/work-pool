package com.workpool.task.service;

import com.workpool.common.enums.BidStatus;
import com.workpool.common.enums.TaskCategory;
import com.workpool.common.enums.TaskStatus;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.UnauthorizedException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.task.dto.BidResponse;
import com.workpool.task.dto.CreateTaskRequest;
import com.workpool.task.dto.PlaceBidRequest;
import com.workpool.task.dto.SendTaskMessageRequest;
import com.workpool.task.dto.TaskResponse;
import com.workpool.task.model.Bid;
import com.workpool.task.model.Task;
import com.workpool.task.repository.BidRepository;
import com.workpool.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TaskService taskService;

    private Task buildOpenTask(String id, String publisherId) {
        return Task.builder()
                .id(id)
                .publisherId(publisherId)
                .title("Fix my sink")
                .description("Fix sink in my bathroom please urgent")
                .category(TaskCategory.HOME_REPAIR)
                .status(TaskStatus.OPEN)
                .budgetMin(BigDecimal.valueOf(500))
                .budgetMax(BigDecimal.valueOf(1000))
                .build();
    }

    private CreateTaskRequest buildCreateRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Fix my sink now");
        request.setDescription("Fix sink in bathroom please urgently");
        request.setCategory(TaskCategory.HOME_REPAIR);
        request.setCity("Mumbai");
        request.setDistrict("Mumbai");
        request.setState("Maharashtra");
        request.setBudgetMin(BigDecimal.valueOf(500));
        request.setBudgetMax(BigDecimal.valueOf(1000));
        return request;
    }

    @Test
    void createTask_happyPath_returnsTaskResponse() {
        CreateTaskRequest request = buildCreateRequest();
        Task saved = buildOpenTask("task-1", "pub-1");
        when(taskRepository.save(any())).thenReturn(saved);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        TaskResponse response = taskService.createTask("pub-1", request);

        assertNotNull(response);
        assertEquals("task-1", response.getId());
    }

    @Test
    void getOpenTasks_withState_returnsPage() {
        Task task = buildOpenTask("task-1", "pub-1");
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findOpenTasksByState(anyString(), any())).thenReturn(page);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        Page<TaskResponse> result = taskService.getOpenTasks("Maharashtra", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOpenTasks_noState_returnsAll() {
        Task task = buildOpenTask("task-1", "pub-1");
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findByStatus(any(), any())).thenReturn(page);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        Page<TaskResponse> result = taskService.getOpenTasks(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMyPublishedTasks_returnsPage() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findByPublisherId(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(task)));
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        Page<TaskResponse> result = taskService.getMyPublishedTasks("pub-1", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMyAssignedTasks_returnsPage() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findByAssignedFinisherId(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(task)));
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        Page<TaskResponse> result = taskService.getMyAssignedTasks("fin-1", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTask_found_returnsResponse() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        TaskResponse response = taskService.getTask("task-1");

        assertEquals("task-1", response.getId());
    }

    @Test
    void getTask_notFound_throws() {
        when(taskRepository.findById("bad")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTask("bad"));
    }

    @Test
    void placeBid_happyPath_returnsBidResponse() {
        Task task = buildOpenTask("task-1", "pub-1");
        PlaceBidRequest request = new PlaceBidRequest();
        request.setProposedAmount(BigDecimal.valueOf(750));
        request.setCoverNote("I can fix it");
        request.setEstimatedDurationHours(2);

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.existsByTaskIdAndFinisherId("task-1", "fin-1")).thenReturn(false);
        Bid saved = Bid.builder().id("bid-1").taskId("task-1").finisherId("fin-1")
                .proposedAmount(BigDecimal.valueOf(750)).status(BidStatus.PENDING).build();
        when(bidRepository.save(any())).thenReturn(saved);
        when(taskRepository.save(any())).thenReturn(task);

        BidResponse response = taskService.placeBid("task-1", "fin-1", "Finisher Name", request);

        assertNotNull(response);
        assertEquals("bid-1", response.getId());
    }

    @Test
    void placeBid_taskNotOpen_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.ASSIGNED);
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        PlaceBidRequest request = new PlaceBidRequest();
        request.setProposedAmount(BigDecimal.valueOf(750));

        assertThrows(WorkPoolException.class,
                () -> taskService.placeBid("task-1", "fin-1", "Finisher", request));
    }

    @Test
    void placeBid_selfBid_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        PlaceBidRequest request = new PlaceBidRequest();
        request.setProposedAmount(BigDecimal.valueOf(750));

        assertThrows(WorkPoolException.class,
                () -> taskService.placeBid("task-1", "pub-1", "Publisher", request));
    }

    @Test
    void placeBid_duplicateBid_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.existsByTaskIdAndFinisherId("task-1", "fin-1")).thenReturn(true);
        PlaceBidRequest request = new PlaceBidRequest();
        request.setProposedAmount(BigDecimal.valueOf(750));

        assertThrows(WorkPoolException.class,
                () -> taskService.placeBid("task-1", "fin-1", "Finisher", request));
    }

    @Test
    void placeBid_alreadyBiddingStatus_doesNotSetBidding() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.BIDDING);
        PlaceBidRequest request = new PlaceBidRequest();
        request.setProposedAmount(BigDecimal.valueOf(750));

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.existsByTaskIdAndFinisherId("task-1", "fin-1")).thenReturn(false);
        Bid saved = Bid.builder().id("bid-1").taskId("task-1").finisherId("fin-1")
                .proposedAmount(BigDecimal.valueOf(750)).status(BidStatus.PENDING).build();
        when(bidRepository.save(any())).thenReturn(saved);

        BidResponse response = taskService.placeBid("task-1", "fin-1", "Finisher Name", request);

        assertNotNull(response);
    }

    @Test
    void acceptBid_happyPath_returnsBidResponse() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.BIDDING);
        Bid bid = Bid.builder().id("bid-1").taskId("task-1").finisherId("fin-1")
                .proposedAmount(BigDecimal.valueOf(750)).status(BidStatus.PENDING).build();
        Bid otherBid = Bid.builder().id("bid-2").taskId("task-1").finisherId("fin-2")
                .proposedAmount(BigDecimal.valueOf(800)).status(BidStatus.PENDING).build();

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.findById("bid-1")).thenReturn(Optional.of(bid));
        when(bidRepository.findByTaskId("task-1")).thenReturn(List.of(bid, otherBid));
        when(bidRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepository.save(any())).thenReturn(task);

        BidResponse response = taskService.acceptBid("task-1", "pub-1", "bid-1");

        assertNotNull(response);
    }

    @Test
    void acceptBid_notPublisher_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedException.class,
                () -> taskService.acceptBid("task-1", "other-user", "bid-1"));
    }

    @Test
    void acceptBid_taskNotOpen_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.ASSIGNED);
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(WorkPoolException.class,
                () -> taskService.acceptBid("task-1", "pub-1", "bid-1"));
    }

    @Test
    void acceptBid_bidNotFound_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.findById("bad-bid")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.acceptBid("task-1", "pub-1", "bad-bid"));
    }

    @Test
    void acceptBid_bidMismatch_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        Bid bid = Bid.builder().id("bid-1").taskId("other-task").finisherId("fin-1")
                .status(BidStatus.PENDING).build();
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.findById("bid-1")).thenReturn(Optional.of(bid));

        assertThrows(WorkPoolException.class,
                () -> taskService.acceptBid("task-1", "pub-1", "bid-1"));
    }

    @Test
    void markComplete_happyPath_returnsTaskResponse() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedFinisherId("fin-1");

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        TaskResponse response = taskService.markComplete("task-1", "fin-1", "http://proof.com");

        assertNotNull(response);
    }

    @Test
    void markComplete_notFinisher_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedException.class,
                () -> taskService.markComplete("task-1", "other-user", null));
    }

    @Test
    void markComplete_invalidStatus_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.OPEN);
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(WorkPoolException.class,
                () -> taskService.markComplete("task-1", "fin-1", null));
    }

    @Test
    void markComplete_inProgressStatus_succeeds() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        TaskResponse response = taskService.markComplete("task-1", "fin-1", null);
        assertNotNull(response);
    }

    @Test
    void confirmCompletion_happyPath_returnsTaskResponse() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.PENDING_REVIEW);
        task.setAssignedFinisherId("fin-1");
        task.setAcceptedBidId("bid-1");

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        TaskResponse response = taskService.confirmCompletion("task-1", "pub-1");

        assertNotNull(response);
    }

    @Test
    void confirmCompletion_notPublisher_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.PENDING_REVIEW);
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedException.class,
                () -> taskService.confirmCompletion("task-1", "other-user"));
    }

    @Test
    void confirmCompletion_notPendingReview_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setStatus(TaskStatus.ASSIGNED);
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(WorkPoolException.class,
                () -> taskService.confirmCompletion("task-1", "pub-1"));
    }

    @Test
    void getBidsForTask_happyPath_returnsList() {
        Task task = buildOpenTask("task-1", "pub-1");
        Bid bid = Bid.builder().id("bid-1").taskId("task-1").finisherId("fin-1")
                .proposedAmount(BigDecimal.valueOf(500)).status(BidStatus.PENDING).build();
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.findByTaskId("task-1")).thenReturn(List.of(bid));

        List<BidResponse> bids = taskService.getBidsForTask("task-1", "pub-1");

        assertEquals(1, bids.size());
    }

    @Test
    void getBidsForTask_notPublisher_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedException.class,
                () -> taskService.getBidsForTask("task-1", "other-user"));
    }

    @Test
    void sendTaskMessage_happyPath_sendsKafkaEvent() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("fin-1", "Hello finisher!");
        taskService.sendTaskMessage("task-1", "pub-1", "Publisher Name", request);

        verify(kafkaTemplate).send(anyString(), any());
    }

    @Test
    void sendTaskMessage_taskNotAssigned_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("fin-1", "Hello!");
        assertThrows(WorkPoolException.class,
                () -> taskService.sendTaskMessage("task-1", "pub-1", "Publisher", request));
    }

    @Test
    void sendTaskMessage_senderNotParticipant_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("fin-1", "Hello!");
        assertThrows(UnauthorizedException.class,
                () -> taskService.sendTaskMessage("task-1", "outsider", "Outsider", request));
    }

    @Test
    void sendTaskMessage_senderEqualsRecipient_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("pub-1", "Hello myself!");
        assertThrows(WorkPoolException.class,
                () -> taskService.sendTaskMessage("task-1", "pub-1", "Publisher", request));
    }

    @Test
    void sendTaskMessage_invalidRecipient_throws() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("outsider", "Hello!");
        assertThrows(WorkPoolException.class,
                () -> taskService.sendTaskMessage("task-1", "pub-1", "Publisher", request));
    }

    @Test
    void sendTaskMessage_nullSenderName_usesDefault() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("fin-1", "Hello!");
        taskService.sendTaskMessage("task-1", "pub-1", null, request);

        verify(kafkaTemplate).send(anyString(), any());
    }

    @Test
    void sendTaskMessage_blankSenderName_usesDefault() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("fin-1", "Hello!");
        taskService.sendTaskMessage("task-1", "pub-1", "  ", request);

        verify(kafkaTemplate).send(anyString(), any());
    }

    @Test
    void sendTaskMessage_finisherSendsToPublisher_succeeds() {
        Task task = buildOpenTask("task-1", "pub-1");
        task.setAssignedFinisherId("fin-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        SendTaskMessageRequest request = new SendTaskMessageRequest("pub-1", "Hello publisher!");
        taskService.sendTaskMessage("task-1", "fin-1", "Finisher", request);

        verify(kafkaTemplate).send(anyString(), any());
    }

    @Test
    void createTask_withNullLocation_publishesEvent() {
        CreateTaskRequest request = buildCreateRequest();
        Task saved = Task.builder()
                .id("task-1")
                .publisherId("pub-1")
                .title("Fix my sink now")
                .category(TaskCategory.HOME_REPAIR)
                .status(TaskStatus.OPEN)
                .location(null)
                .budgetMax(BigDecimal.valueOf(1000))
                .build();
        when(taskRepository.save(any())).thenReturn(saved);
        when(bidRepository.countByTaskIdAndStatus(anyString(), any())).thenReturn(0L);

        TaskResponse response = taskService.createTask("pub-1", request);

        assertNotNull(response);
    }
}

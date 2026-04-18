package com.workpool.task.controller;

import com.workpool.common.dto.ApiResponse;
import com.workpool.task.dto.*;
import com.workpool.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tasks", description = "Task marketplace APIs")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Post a new task", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(taskService.createTask(userId, request), "Task posted successfully"));
    }

    @Operation(summary = "Browse open tasks")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getOpenTasks(
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(taskService.getOpenTasks(state, pageable)));
    }

    @Operation(summary = "Get task details")
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTask(taskId)));
    }

    @Operation(summary = "My published tasks", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my/published")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyPublishedTasks(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                taskService.getMyPublishedTasks(userId, PageRequest.of(page, size))));
    }

    @Operation(summary = "My assigned tasks", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my/assigned")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyAssignedTasks(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                taskService.getMyAssignedTasks(userId, PageRequest.of(page, size))));
    }

    @Operation(summary = "Place a bid on a task", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{taskId}/bids")
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Anonymous") String userName,
            @Valid @RequestBody PlaceBidRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(taskService.placeBid(taskId, userId, userName, request)));
    }

    @Operation(summary = "Get all bids for a task (publisher only)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{taskId}/bids")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBids(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getBidsForTask(taskId, userId)));
    }

    @Operation(summary = "Accept a bid", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{taskId}/bids/{bidId}/accept")
    public ResponseEntity<ApiResponse<BidResponse>> acceptBid(
            @PathVariable String taskId,
            @PathVariable String bidId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.acceptBid(taskId, userId, bidId)));
    }

    @Operation(summary = "Mark task as complete (finisher)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> markComplete(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) String proofUrl) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.markComplete(taskId, userId, proofUrl)));
    }

    @Operation(summary = "Confirm task completion (publisher)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{taskId}/confirm")
    public ResponseEntity<ApiResponse<TaskResponse>> confirmCompletion(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.confirmCompletion(taskId, userId)));
    }
}

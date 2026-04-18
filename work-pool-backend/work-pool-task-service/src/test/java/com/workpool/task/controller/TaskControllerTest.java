package com.workpool.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpool.common.enums.TaskCategory;
import com.workpool.common.enums.TaskStatus;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.task.config.SecurityConfig;
import com.workpool.task.dto.BidResponse;
import com.workpool.task.dto.CreateTaskRequest;
import com.workpool.task.dto.PlaceBidRequest;
import com.workpool.task.dto.SendTaskMessageRequest;
import com.workpool.task.dto.TaskResponse;
import com.workpool.task.security.JwtTokenProvider;
import com.workpool.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = {TaskController.class, GlobalExceptionHandler.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class TaskControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MongoMappingContext mongoMappingContext() {
            return new MongoMappingContext();
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private TaskResponse buildTaskResponse() {
        return TaskResponse.builder()
                .id("task-1")
                .publisherId("pub-1")
                .title("Fix my sink")
                .status(TaskStatus.OPEN)
                .budgetMin(BigDecimal.valueOf(500))
                .budgetMax(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void createTask_authenticated_returns201() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Fix my sink now");
        request.setDescription("Fix sink in bathroom please urgently");
        request.setCategory(TaskCategory.HOME_REPAIR);
        request.setCity("Mumbai");
        request.setDistrict("Mumbai");
        request.setState("Maharashtra");
        request.setBudgetMin(BigDecimal.valueOf(500));
        request.setBudgetMax(BigDecimal.valueOf(1000));

        when(taskService.createTask(anyString(), any())).thenReturn(buildTaskResponse());

        mockMvc.perform(post("/api/v1/tasks")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                "pub-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getOpenTasks_noAuth_returns200() throws Exception {
        when(taskService.getOpenTasks(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/tasks")).andExpect(status().isOk());
    }

    @Test
    void getOpenTasks_withState_returns200() throws Exception {
        when(taskService.getOpenTasks(eq("Maharashtra"), any()))
                .thenReturn(new PageImpl<>(List.of(buildTaskResponse())));
        mockMvc.perform(get("/api/v1/tasks").param("state", "Maharashtra"))
                .andExpect(status().isOk());
    }

    @Test
    void getTask_byId_returns200() throws Exception {
        when(taskService.getTask("task-1")).thenReturn(buildTaskResponse());
        mockMvc.perform(get("/api/v1/tasks/task-1")).andExpect(status().isOk());
    }

    @Test
    void getMyPublishedTasks_authenticated_returns200() throws Exception {
        when(taskService.getMyPublishedTasks(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/tasks/my/published")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void getMyAssignedTasks_authenticated_returns200() throws Exception {
        when(taskService.getMyAssignedTasks(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/tasks/my/assigned")
                        .with(authentication(new UsernamePasswordAuthenticationToken("fin-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void placeBid_authenticated_returns201() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        request.setProposedAmount(BigDecimal.valueOf(750));
        request.setEstimatedDurationHours(2);

        BidResponse bidResponse = BidResponse.builder().id("bid-1").taskId("task-1").build();
        when(taskService.placeBid(anyString(), anyString(), anyString(), any())).thenReturn(bidResponse);

        mockMvc.perform(post("/api/v1/tasks/task-1/bids")
                        .with(authentication(new UsernamePasswordAuthenticationToken("fin-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getBids_authenticated_returns200() throws Exception {
        when(taskService.getBidsForTask(anyString(), anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/tasks/task-1/bids")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void acceptBid_authenticated_returns200() throws Exception {
        BidResponse bidResponse = BidResponse.builder().id("bid-1").build();
        when(taskService.acceptBid(anyString(), anyString(), anyString())).thenReturn(bidResponse);
        mockMvc.perform(post("/api/v1/tasks/task-1/bids/bid-1/accept")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void markComplete_authenticated_returns200() throws Exception {
        when(taskService.markComplete(anyString(), anyString(), any())).thenReturn(buildTaskResponse());
        mockMvc.perform(post("/api/v1/tasks/task-1/complete")
                        .with(authentication(new UsernamePasswordAuthenticationToken("fin-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void confirmCompletion_authenticated_returns200() throws Exception {
        when(taskService.confirmCompletion(anyString(), anyString())).thenReturn(buildTaskResponse());
        mockMvc.perform(post("/api/v1/tasks/task-1/confirm")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void sendTaskMessage_authenticated_returns200() throws Exception {
        SendTaskMessageRequest request = new SendTaskMessageRequest("fin-1", "Hello!");
        doNothing().when(taskService).sendTaskMessage(anyString(), anyString(), any(), any());
        mockMvc.perform(post("/api/v1/tasks/task-1/messages")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void handleWorkPoolException_notFoundCode_returns404() throws Exception {
        when(taskService.getTask("bad")).thenThrow(new WorkPoolException("NOT_FOUND", "Not found"));
        mockMvc.perform(get("/api/v1/tasks/bad")).andExpect(status().isNotFound());
    }

    @Test
    void handleWorkPoolException_unauthorizedCode_returns403() throws Exception {
        when(taskService.getTask("task-1")).thenThrow(new WorkPoolException("UNAUTHORIZED", "Forbidden"));
        mockMvc.perform(get("/api/v1/tasks/task-1")).andExpect(status().isForbidden());
    }

    @Test
    void handleWorkPoolException_defaultCode_returns400() throws Exception {
        when(taskService.getTask("task-1")).thenThrow(new WorkPoolException("TASK_NOT_OPEN", "Not open"));
        mockMvc.perform(get("/api/v1/tasks/task-1")).andExpect(status().isBadRequest());
    }

    @Test
    void handleGeneral_unexpectedError_returns500() throws Exception {
        when(taskService.getTask("task-1")).thenThrow(new RuntimeException("unexpected"));
        mockMvc.perform(get("/api/v1/tasks/task-1")).andExpect(status().isInternalServerError());
    }

    @Test
    void handleValidation_invalidRequest_returns400() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        mockMvc.perform(post("/api/v1/tasks/task-1/bids")
                        .with(authentication(new UsernamePasswordAuthenticationToken("fin-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

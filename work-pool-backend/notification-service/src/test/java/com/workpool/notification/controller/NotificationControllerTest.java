package com.workpool.notification.controller;

import com.workpool.notification.config.SecurityConfig;
import com.workpool.notification.security.JwtTokenProvider;
import com.workpool.notification.service.NotificationService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = NotificationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class NotificationControllerTest {

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

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getNotifications_returns200() throws Exception {
        when(notificationService.getNotifications(anyString(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/notifications")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void getUnreadCount_returns200() throws Exception {
        when(notificationService.getUnreadCount("user-1")).thenReturn(3L);
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(3));
    }

    @Test
    void markAsRead_returns200() throws Exception {
        doNothing().when(notificationService).markAsRead(anyString(), anyString());
        mockMvc.perform(patch("/api/v1/notifications/notif-1/read")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void markAllAsRead_returns200() throws Exception {
        doNothing().when(notificationService).markAllAsRead(anyString());
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of()))))
                .andExpect(status().isOk());
    }
}

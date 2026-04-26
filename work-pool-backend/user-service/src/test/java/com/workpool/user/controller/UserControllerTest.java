package com.workpool.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpool.user.config.SecurityConfig;
import com.workpool.user.dto.UpdateProfileRequest;
import com.workpool.user.dto.UserProfileResponse;
import com.workpool.user.security.JwtTokenProvider;
import com.workpool.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = {UserController.class, GlobalExceptionHandler.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class UserControllerTest {

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
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UserProfileResponse buildProfile(String id) {
        return UserProfileResponse.builder()
                .id(id)
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of())
                .build();
    }

    @Test
    void getMyProfile_authenticated_returns200() throws Exception {
        when(userService.getProfile("user-1")).thenReturn(buildProfile("user-1"));

        mockMvc.perform(get("/api/v1/users/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                "user-1", null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-1"));
    }

    @Test
    void updateProfile_authenticated_returns200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");

        when(userService.updateProfile(eq("user-1"), any())).thenReturn(buildProfile("user-1"));

        mockMvc.perform(put("/api/v1/users/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                "user-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getPublicProfile_returnsProfile() throws Exception {
        when(userService.getPublicProfile("user-2")).thenReturn(buildProfile("user-2"));

        mockMvc.perform(get("/api/v1/users/user-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user-2"));
    }
}

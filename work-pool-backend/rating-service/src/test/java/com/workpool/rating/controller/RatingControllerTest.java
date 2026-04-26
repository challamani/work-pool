package com.workpool.rating.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpool.rating.config.SecurityConfig;
import com.workpool.rating.dto.RatingResponse;
import com.workpool.rating.dto.SubmitRatingRequest;
import com.workpool.rating.dto.UserRatingSummary;
import com.workpool.rating.security.JwtTokenProvider;
import com.workpool.rating.service.RatingService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = RatingController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class RatingControllerTest {

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
    private RatingService ratingService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void submitRating_returns201() throws Exception {
        SubmitRatingRequest request = new SubmitRatingRequest();
        request.setTaskId("task-1");
        request.setRatedUserId("fin-1");
        request.setStars(5);
        RatingResponse response = RatingResponse.builder().id("r-1").taskId("task-1").build();
        when(ratingService.submitRating(anyString(), any())).thenReturn(response);
        mockMvc.perform(post("/api/v1/ratings")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submitRating_invalidRequest_returns400() throws Exception {
        SubmitRatingRequest request = new SubmitRatingRequest();
        request.setStars(10);
        mockMvc.perform(post("/api/v1/ratings")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRatings_returnsPage() throws Exception {
        when(ratingService.getRatingsForUser(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/ratings/users/user-1")).andExpect(status().isOk());
    }

    @Test
    void getSummary_returnsUserRatingSummary() throws Exception {
        UserRatingSummary summary = UserRatingSummary.builder()
                .userId("user-1").totalRatings(5).averageRating(4.2).build();
        when(ratingService.getSummaryForUser("user-1")).thenReturn(summary);
        mockMvc.perform(get("/api/v1/ratings/users/user-1/summary")).andExpect(status().isOk());
    }
}

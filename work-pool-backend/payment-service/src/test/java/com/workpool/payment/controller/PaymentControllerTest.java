package com.workpool.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpool.common.enums.PaymentStatus;
import com.workpool.payment.config.SecurityConfig;
import com.workpool.payment.dto.CreateOrderRequest;
import com.workpool.payment.dto.TransactionResponse;
import com.workpool.payment.model.Wallet;
import com.workpool.payment.security.JwtTokenProvider;
import com.workpool.payment.service.PaymentService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = PaymentController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class PaymentControllerTest {

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
    private PaymentService paymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private TransactionResponse buildTxn() {
        return TransactionResponse.builder()
                .id("txn-1").taskId("task-1").publisherId("pub-1").finisherId("fin-1")
                .agreedAmount(BigDecimal.valueOf(1000)).publisherCommission(BigDecimal.valueOf(10))
                .finisherCommission(BigDecimal.valueOf(10)).finisherNetPayout(BigDecimal.valueOf(990))
                .status(PaymentStatus.PENDING).build();
    }

    @Test
    void createOrder_returns200() throws Exception {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .taskId("task-1").finisherId("fin-1").bidId("bid-1")
                .agreedAmount(BigDecimal.valueOf(1000)).build();
        when(paymentService.createEscrowOrder(anyString(), any())).thenReturn(buildTxn());
        mockMvc.perform(post("/api/v1/payments/orders")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void handleWebhook_paymentCaptured_returns200() throws Exception {
        when(paymentService.capturePayment(anyString(), anyString())).thenReturn(buildTxn());
        Map<String, Object> entity = new HashMap<>();
        entity.put("order_id", "rzp_order_123");
        entity.put("id", "pay_123");
        Map<String, Object> paymentEntity = new HashMap<>();
        paymentEntity.put("entity", entity);
        Map<String, Object> payload = new HashMap<>();
        payload.put("payment", paymentEntity);
        Map<String, Object> body = new HashMap<>();
        body.put("event", "payment.captured");
        body.put("payload", payload);
        mockMvc.perform(post("/api/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void handleWebhook_otherEvent_returns200() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("event", "payment.failed");
        mockMvc.perform(post("/api/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void releasePayment_returns200() throws Exception {
        when(paymentService.releasePayment("task-1")).thenReturn(buildTxn());
        mockMvc.perform(post("/api/v1/payments/tasks/task-1/release")
                        .with(authentication(new UsernamePasswordAuthenticationToken("pub-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void getWallet_returns200() throws Exception {
        Wallet wallet = Wallet.builder().userId("user-1").balance(BigDecimal.ZERO).build();
        when(paymentService.getWallet("user-1")).thenReturn(wallet);
        mockMvc.perform(get("/api/v1/payments/wallet")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of()))))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_returns200() throws Exception {
        when(paymentService.getTransactionHistory(anyString(), anyBoolean())).thenReturn(List.of(buildTxn()));
        mockMvc.perform(get("/api/v1/payments/history")
                        .with(authentication(new UsernamePasswordAuthenticationToken("user-1", null, List.of()))))
                .andExpect(status().isOk());
    }
}

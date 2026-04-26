package com.workpool.payment.controller;

import com.workpool.common.dto.ApiResponse;
import com.workpool.payment.dto.CreateOrderRequest;
import com.workpool.payment.dto.TransactionResponse;
import com.workpool.payment.model.Wallet;
import com.workpool.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Payments & Wallet", description = "Escrow payment and wallet management")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create escrow payment order", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<TransactionResponse>> createOrder(
            @AuthenticationPrincipal String userId,
            @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.createEscrowOrder(userId, request)));
    }

    @Operation(summary = "Razorpay webhook – capture payment")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Razorpay webhook: {}", payload.get("event"));
        String event = (String) payload.get("event");
        if ("payment.captured".equals(event)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paymentEntity = (Map<String, Object>)
                    ((Map<String, Object>) payload.get("payload")).get("payment");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) paymentEntity.get("entity");
            String orderId = (String) entity.get("order_id");
            String paymentId = (String) entity.get("id");
            paymentService.capturePayment(orderId, paymentId);
        }
        return ResponseEntity.ok("OK");
    }

    @Operation(summary = "Release payment after task confirmation", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tasks/{taskId}/release")
    public ResponseEntity<ApiResponse<TransactionResponse>> releasePayment(@PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.releasePayment(taskId)));
    }

    @Operation(summary = "Get my wallet", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<Wallet>> getWallet(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getWallet(userId)));
    }

    @Operation(summary = "Get transaction history", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "true") boolean asPublisher) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getTransactionHistory(userId, asPublisher)));
    }
}

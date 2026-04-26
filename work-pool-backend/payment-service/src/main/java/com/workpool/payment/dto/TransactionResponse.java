package com.workpool.payment.dto;

import com.workpool.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id;
    private String taskId;
    private String publisherId;
    private String finisherId;
    private BigDecimal agreedAmount;
    private BigDecimal publisherCommission;
    private BigDecimal finisherCommission;
    private BigDecimal finisherNetPayout;
    private PaymentStatus status;
    private String gatewayOrderId;
    private Instant escrowHeldAt;
    private Instant releasedAt;
    private Instant createdAt;
}

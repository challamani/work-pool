package com.workpool.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEscrowEvent {
    private String paymentId;
    private String taskId;
    private String publisherId;
    private String finisherId;
    private BigDecimal totalAmount;
    private BigDecimal commissionAmount;
    private Instant createdAt;
}

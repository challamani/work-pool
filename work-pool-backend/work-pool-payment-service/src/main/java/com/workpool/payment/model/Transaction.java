package com.workpool.payment.model;

import com.workpool.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Indexed
    private String taskId;

    @Indexed
    private String publisherId;

    @Indexed
    private String finisherId;

    private String bidId;

    /** Total agreed amount (INR) */
    private BigDecimal agreedAmount;

    /** 1% commission from publisher */
    private BigDecimal publisherCommission;

    /** 1% commission from finisher */
    private BigDecimal finisherCommission;

    /** Amount finisher actually receives after commission */
    private BigDecimal finisherNetPayout;

    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Razorpay order ID */
    private String gatewayOrderId;

    /** Razorpay payment ID (once captured) */
    private String gatewayPaymentId;

    private Instant escrowHeldAt;
    private Instant releasedAt;
    private String failureReason;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

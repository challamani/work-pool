package com.workpool.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.workpool.common.enums.NotificationType;
import com.workpool.common.enums.PaymentStatus;
import com.workpool.common.event.NotificationEvent;
import com.workpool.common.event.PaymentEscrowEvent;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.common.util.CommissionConstants;
import com.workpool.common.util.KafkaTopics;
import com.workpool.payment.dto.CreateOrderRequest;
import com.workpool.payment.dto.TransactionResponse;
import com.workpool.payment.model.Transaction;
import com.workpool.payment.model.Wallet;
import com.workpool.payment.repository.TransactionRepository;
import com.workpool.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final RazorpayClient razorpayClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${platform.commission-rate:0.01}")
    private double commissionRate;

    public TransactionResponse createEscrowOrder(String publisherId, CreateOrderRequest request) {
        // Check no existing pending transaction for this task
        transactionRepository.findByTaskId(request.getTaskId()).ifPresent(t -> {
            if (t.getStatus() == PaymentStatus.ESCROW_HELD || t.getStatus() == PaymentStatus.PENDING) {
                throw new WorkPoolException("PAYMENT_EXISTS", "Payment already initiated for this task");
            }
        });

        BigDecimal agreed = request.getAgreedAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal publisherCommission = agreed.multiply(BigDecimal.valueOf(commissionRate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finisherCommission = agreed.multiply(BigDecimal.valueOf(commissionRate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finisherNetPayout = agreed.subtract(finisherCommission).setScale(2, RoundingMode.HALF_UP);

        // Total publisher pays = agreed + publisherCommission
        BigDecimal totalPublisherPays = agreed.add(publisherCommission).setScale(2, RoundingMode.HALF_UP);

        // Create Razorpay order (amount in paise)
        String gatewayOrderId = null;
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", totalPublisherPays.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "task_" + request.getTaskId());
            orderRequest.put("payment_capture", 1);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            gatewayOrderId = razorpayOrder.get("id");
        } catch (RazorpayException ex) {
            log.error("Razorpay order creation failed: {}", ex.getMessage());
            throw new WorkPoolException("PAYMENT_GATEWAY_ERROR", "Payment gateway error: " + ex.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .taskId(request.getTaskId())
                .publisherId(publisherId)
                .finisherId(request.getFinisherId())
                .bidId(request.getBidId())
                .agreedAmount(agreed)
                .publisherCommission(publisherCommission)
                .finisherCommission(finisherCommission)
                .finisherNetPayout(finisherNetPayout)
                .status(PaymentStatus.PENDING)
                .gatewayOrderId(gatewayOrderId)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Escrow order created for task {}: orderId={}", request.getTaskId(), gatewayOrderId);
        return toResponse(transaction);
    }

    public TransactionResponse capturePayment(String gatewayOrderId, String gatewayPaymentId) {
        Transaction transaction = transactionRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", gatewayOrderId));

        transaction.setStatus(PaymentStatus.ESCROW_HELD);
        transaction.setGatewayPaymentId(gatewayPaymentId);
        transaction.setEscrowHeldAt(Instant.now());
        transaction = transactionRepository.save(transaction);

        // Send escrow event
        PaymentEscrowEvent escrowEvent = new PaymentEscrowEvent(
                transaction.getId(), transaction.getTaskId(),
                transaction.getPublisherId(), transaction.getFinisherId(),
                transaction.getAgreedAmount(),
                transaction.getPublisherCommission().add(transaction.getFinisherCommission()),
                Instant.now());
        kafkaTemplate.send(KafkaTopics.PAYMENT_ESCROW, escrowEvent);

        // Notify both parties
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(transaction.getPublisherId())
                .type(NotificationType.PAYMENT_HELD)
                .title("Payment held in escrow")
                .message("₹" + transaction.getAgreedAmount() + " is securely held in escrow for task completion.")
                .metadata(Map.of("taskId", transaction.getTaskId()))
                .createdAt(Instant.now())
                .build());

        log.info("Payment captured and held in escrow for task {}", transaction.getTaskId());
        return toResponse(transaction);
    }

    public TransactionResponse releasePayment(String taskId) {
        Transaction transaction = transactionRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction for task", taskId));

        if (transaction.getStatus() != PaymentStatus.ESCROW_HELD) {
            throw new WorkPoolException("INVALID_PAYMENT_STATE",
                    "Payment is not held in escrow, cannot release");
        }

        transaction.setStatus(PaymentStatus.RELEASED);
        transaction.setReleasedAt(Instant.now());
        transaction = transactionRepository.save(transaction);

        // Credit finisher's wallet
        String finisherId = transaction.getFinisherId();
        Wallet finisherWallet = walletRepository.findByUserId(finisherId)
                .orElseGet(() -> Wallet.builder().userId(finisherId).build());
        finisherWallet.setBalance(finisherWallet.getBalance().add(transaction.getFinisherNetPayout()));
        walletRepository.save(finisherWallet);

        kafkaTemplate.send(KafkaTopics.PAYMENT_RELEASED, transaction);
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_SEND, NotificationEvent.builder()
                .recipientUserId(transaction.getFinisherId())
                .type(NotificationType.PAYMENT_RELEASED)
                .title("Payment released!")
                .message("₹" + transaction.getFinisherNetPayout() + " has been credited to your wallet.")
                .metadata(Map.of("taskId", taskId))
                .createdAt(Instant.now())
                .build());

        log.info("Payment released for task {}: ₹{} to finisher {}", taskId,
                transaction.getFinisherNetPayout(), transaction.getFinisherId());
        return toResponse(transaction);
    }

    public Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> Wallet.builder().userId(userId).build());
    }

    public List<TransactionResponse> getTransactionHistory(String userId, boolean asPublisher) {
        List<Transaction> txns = asPublisher
                ? transactionRepository.findByPublisherIdOrderByCreatedAtDesc(userId)
                : transactionRepository.findByFinisherIdOrderByCreatedAtDesc(userId);
        return txns.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .taskId(t.getTaskId())
                .publisherId(t.getPublisherId())
                .finisherId(t.getFinisherId())
                .agreedAmount(t.getAgreedAmount())
                .publisherCommission(t.getPublisherCommission())
                .finisherCommission(t.getFinisherCommission())
                .finisherNetPayout(t.getFinisherNetPayout())
                .status(t.getStatus())
                .gatewayOrderId(t.getGatewayOrderId())
                .escrowHeldAt(t.getEscrowHeldAt())
                .releasedAt(t.getReleasedAt())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

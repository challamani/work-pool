package com.workpool.payment.service;

import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.workpool.common.enums.PaymentStatus;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.payment.dto.CreateOrderRequest;
import com.workpool.payment.dto.TransactionResponse;
import com.workpool.payment.model.Transaction;
import com.workpool.payment.model.Wallet;
import com.workpool.payment.repository.TransactionRepository;
import com.workpool.payment.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private RazorpayClient razorpayClient;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private OrderClient mockOrderClient;

    @BeforeEach
    void setUp() throws Exception {
        mockOrderClient = mock(OrderClient.class);
        java.lang.reflect.Field ordersField = RazorpayClient.class.getField("orders");
        ordersField.setAccessible(true);
        ordersField.set(razorpayClient, mockOrderClient);
        ReflectionTestUtils.setField(paymentService, "commissionRate", 0.01);
    }

    private Transaction buildTransaction(String id, String taskId, PaymentStatus status) {
        return Transaction.builder()
                .id(id)
                .taskId(taskId)
                .publisherId("pub-1")
                .finisherId("fin-1")
                .bidId("bid-1")
                .agreedAmount(BigDecimal.valueOf(1000))
                .publisherCommission(BigDecimal.valueOf(10))
                .finisherCommission(BigDecimal.valueOf(10))
                .finisherNetPayout(BigDecimal.valueOf(990))
                .status(status)
                .gatewayOrderId("rzp_order_123")
                .build();
    }

    @Test
    void createEscrowOrder_happyPath_returnsTransactionResponse() throws RazorpayException {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .taskId("task-1")
                .finisherId("fin-1")
                .bidId("bid-1")
                .agreedAmount(BigDecimal.valueOf(1000))
                .build();

        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.empty());

        Order mockOrder = mock(Order.class);
        when(mockOrder.get("id")).thenReturn("rzp_order_123");
        when(mockOrderClient.create(any())).thenReturn(mockOrder);

        Transaction saved = buildTransaction("txn-1", "task-1", PaymentStatus.PENDING);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = paymentService.createEscrowOrder("pub-1", request);

        assertNotNull(response);
    }

    @Test
    void createEscrowOrder_paymentAlreadyExists_throws() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .taskId("task-1")
                .agreedAmount(BigDecimal.valueOf(1000))
                .build();

        Transaction existing = buildTransaction("txn-1", "task-1", PaymentStatus.ESCROW_HELD);
        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.of(existing));

        assertThrows(WorkPoolException.class,
                () -> paymentService.createEscrowOrder("pub-1", request));
    }

    @Test
    void createEscrowOrder_pendingPaymentAlreadyExists_throws() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .taskId("task-1")
                .agreedAmount(BigDecimal.valueOf(1000))
                .build();

        Transaction existing = buildTransaction("txn-1", "task-1", PaymentStatus.PENDING);
        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.of(existing));

        assertThrows(WorkPoolException.class,
                () -> paymentService.createEscrowOrder("pub-1", request));
    }

    @Test
    void createEscrowOrder_noExistingWithReleasedStatus_succeeds() throws RazorpayException {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .taskId("task-1")
                .finisherId("fin-1")
                .bidId("bid-1")
                .agreedAmount(BigDecimal.valueOf(1000))
                .build();

        Transaction existing = buildTransaction("txn-1", "task-1", PaymentStatus.RELEASED);
        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.of(existing));

        Order mockOrder = mock(Order.class);
        when(mockOrder.get("id")).thenReturn("rzp_order_456");
        when(mockOrderClient.create(any())).thenReturn(mockOrder);

        Transaction saved = buildTransaction("txn-2", "task-1", PaymentStatus.PENDING);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = paymentService.createEscrowOrder("pub-1", request);
        assertNotNull(response);
    }

    @Test
    void createEscrowOrder_razorpayFails_throwsWorkPoolException() throws RazorpayException {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .taskId("task-1")
                .finisherId("fin-1")
                .bidId("bid-1")
                .agreedAmount(BigDecimal.valueOf(1000))
                .build();

        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.empty());
        when(mockOrderClient.create(any())).thenThrow(new RazorpayException("Gateway error"));

        assertThrows(WorkPoolException.class,
                () -> paymentService.createEscrowOrder("pub-1", request));
    }

    @Test
    void capturePayment_happyPath_returnsTransactionResponse() {
        Transaction txn = buildTransaction("txn-1", "task-1", PaymentStatus.PENDING);
        when(transactionRepository.findByGatewayOrderId("rzp_order_123"))
                .thenReturn(Optional.of(txn));
        when(transactionRepository.save(any())).thenReturn(txn);

        TransactionResponse response = paymentService.capturePayment("rzp_order_123", "pay_123");

        assertNotNull(response);
        verify(kafkaTemplate, times(2)).send(anyString(), any());
    }

    @Test
    void capturePayment_notFound_throws() {
        when(transactionRepository.findByGatewayOrderId("bad-order"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.capturePayment("bad-order", "pay_123"));
    }

    @Test
    void releasePayment_happyPath_creditsWallet() {
        Transaction txn = buildTransaction("txn-1", "task-1", PaymentStatus.ESCROW_HELD);
        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.of(txn));
        when(transactionRepository.save(any())).thenReturn(txn);

        Wallet wallet = Wallet.builder().userId("fin-1").balance(BigDecimal.ZERO).build();
        when(walletRepository.findByUserId("fin-1")).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);

        TransactionResponse response = paymentService.releasePayment("task-1");

        assertNotNull(response);
    }

    @Test
    void releasePayment_noExistingWallet_createsWallet() {
        Transaction txn = buildTransaction("txn-1", "task-1", PaymentStatus.ESCROW_HELD);
        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.of(txn));
        when(transactionRepository.save(any())).thenReturn(txn);

        when(walletRepository.findByUserId("fin-1")).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenReturn(
                Wallet.builder().userId("fin-1").balance(BigDecimal.valueOf(990)).build());

        TransactionResponse response = paymentService.releasePayment("task-1");
        assertNotNull(response);
    }

    @Test
    void releasePayment_notFound_throws() {
        when(transactionRepository.findByTaskId("bad-task")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.releasePayment("bad-task"));
    }

    @Test
    void releasePayment_invalidState_throws() {
        Transaction txn = buildTransaction("txn-1", "task-1", PaymentStatus.PENDING);
        when(transactionRepository.findByTaskId("task-1")).thenReturn(Optional.of(txn));

        assertThrows(WorkPoolException.class, () -> paymentService.releasePayment("task-1"));
    }

    @Test
    void getWallet_existingWallet_returnsWallet() {
        Wallet wallet = Wallet.builder().userId("user-1").balance(BigDecimal.valueOf(500)).build();
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(wallet));

        Wallet result = paymentService.getWallet("user-1");

        assertNotNull(result);
    }

    @Test
    void getWallet_noWallet_returnsEmpty() {
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        Wallet result = paymentService.getWallet("user-1");

        assertNotNull(result);
    }

    @Test
    void getTransactionHistory_asPublisher_returnsTransactions() {
        Transaction txn = buildTransaction("txn-1", "task-1", PaymentStatus.RELEASED);
        when(transactionRepository.findByPublisherIdOrderByCreatedAtDesc("pub-1"))
                .thenReturn(List.of(txn));

        List<TransactionResponse> result = paymentService.getTransactionHistory("pub-1", true);

        assertNotNull(result);
    }

    @Test
    void getTransactionHistory_asFinisher_returnsTransactions() {
        Transaction txn = buildTransaction("txn-1", "task-1", PaymentStatus.RELEASED);
        when(transactionRepository.findByFinisherIdOrderByCreatedAtDesc("fin-1"))
                .thenReturn(List.of(txn));

        List<TransactionResponse> result = paymentService.getTransactionHistory("fin-1", false);

        assertNotNull(result);
    }
}

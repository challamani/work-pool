package com.workpool.payment.repository;

import com.workpool.common.enums.PaymentStatus;
import com.workpool.payment.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByTaskId(String taskId);

    List<Transaction> findByPublisherIdOrderByCreatedAtDesc(String publisherId);

    List<Transaction> findByFinisherIdOrderByCreatedAtDesc(String finisherId);

    Optional<Transaction> findByGatewayOrderId(String gatewayOrderId);

    List<Transaction> findByStatus(PaymentStatus status);
}

package com.workpool.notification.repository;

import com.workpool.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByRecipientUserIdAndReadFalse(String userId);
}

package com.workpool.common;

import com.workpool.common.dto.ApiResponse;
import com.workpool.common.enums.BidStatus;
import com.workpool.common.enums.NotificationType;
import com.workpool.common.enums.PaymentStatus;
import com.workpool.common.enums.TaskCategory;
import com.workpool.common.enums.TaskStatus;
import com.workpool.common.enums.UserRole;
import com.workpool.common.enums.VerificationStatus;
import com.workpool.common.event.NotificationEvent;
import com.workpool.common.event.PaymentEscrowEvent;
import com.workpool.common.event.TaskCompletedEvent;
import com.workpool.common.event.TaskPostedEvent;
import com.workpool.common.exception.ResourceNotFoundException;
import com.workpool.common.exception.UnauthorizedException;
import com.workpool.common.exception.WorkPoolException;
import com.workpool.common.model.GeoLocation;
import com.workpool.common.util.CommissionConstants;
import com.workpool.common.util.KafkaTopics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommonCoreCoverageTest {

    @Test
    void shouldCoverApiResponseFactories() {
        ApiResponse<String> ok = ApiResponse.ok("hello");
        assertTrue(ok.isSuccess());
        assertEquals("hello", ok.getData());
        assertNotNull(ok.getTimestamp());

        ApiResponse<String> okWithMessage = ApiResponse.ok("hello", "done");
        assertEquals("done", okWithMessage.getMessage());

        ApiResponse<String> error = ApiResponse.error("ERR", "failed");
        assertFalse(error.isSuccess());
        assertEquals("ERR", error.getErrorCode());
        assertEquals("failed", error.getMessage());
    }

    @Test
    void shouldCoverExceptionHierarchy() {
        WorkPoolException base = new WorkPoolException("CODE", "Message");
        assertEquals("CODE", base.getErrorCode());
        assertEquals("Message", base.getMessage());

        RuntimeException cause = new RuntimeException("boom");
        WorkPoolException withCause = new WorkPoolException("CODE2", "Message2", cause);
        assertEquals("CODE2", withCause.getErrorCode());
        assertSame(cause, withCause.getCause());

        ResourceNotFoundException notFound = new ResourceNotFoundException("User", "1");
        assertEquals("NOT_FOUND", notFound.getErrorCode());
        assertTrue(notFound.getMessage().contains("User"));

        UnauthorizedException unauthorized = new UnauthorizedException("No access");
        assertEquals("UNAUTHORIZED", unauthorized.getErrorCode());
        assertEquals("No access", unauthorized.getMessage());
    }

    @Test
    void shouldCoverCommonModelsAndEvents() {
        GeoLocation location = new GeoLocation(17.1, 78.2, "Hyderabad", "Hyderabad", "Telangana", "500001");
        assertEquals("Hyderabad", location.getCity());
        location.setLatitude(18.0);
        assertEquals(18.0, location.getLatitude());

        Instant now = Instant.now();
        PaymentEscrowEvent payment = new PaymentEscrowEvent(
                "p1", "t1", "u1", "u2", BigDecimal.TEN, BigDecimal.ONE, now);
        assertEquals(BigDecimal.TEN, payment.getTotalAmount());

        TaskCompletedEvent completed = new TaskCompletedEvent("t1", "u1", "u2", "b1", now);
        assertEquals("u2", completed.getFinisherId());

        TaskPostedEvent posted = new TaskPostedEvent(
                "t1", "u1", "Task", TaskCategory.COOKING, List.of("Skill"),
                "City", "District", "State", 10.2, 20.3, BigDecimal.ONE, now);
        assertEquals(TaskCategory.COOKING, posted.getCategory());

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId("u1")
                .type(NotificationType.DIRECT_MESSAGE)
                .title("Title")
                .message("Message")
                .metadata(Map.of("k", "v"))
                .createdAt(now)
                .build();
        assertEquals(NotificationType.DIRECT_MESSAGE, event.getType());
    }

    @Test
    void shouldCoverEnumsAndUtilityConstants() throws Exception {
        assertNotNull(BidStatus.valueOf("PENDING"));
        assertNotNull(TaskStatus.valueOf("OPEN"));
        assertNotNull(TaskCategory.valueOf("OTHER"));
        assertNotNull(NotificationType.valueOf("SYSTEM_ALERT"));
        assertNotNull(PaymentStatus.valueOf("PENDING"));
        assertNotNull(UserRole.valueOf("PUBLISHER"));
        assertNotNull(VerificationStatus.valueOf("UNVERIFIED"));

        assertEquals(0.01, CommissionConstants.PUBLISHER_COMMISSION_RATE);
        assertEquals(0.01, CommissionConstants.FINISHER_COMMISSION_RATE);
        assertEquals(100, CommissionConstants.DEFAULT_MAX_RADIUS_KM);
        assertEquals(20, CommissionConstants.DEFAULT_RADIUS_KM);
        assertEquals("workpool.task.posted", KafkaTopics.TASK_POSTED);
        assertEquals("workpool.notification.send", KafkaTopics.NOTIFICATION_SEND);

        Constructor<CommissionConstants> commissionCtor = CommissionConstants.class.getDeclaredConstructor();
        commissionCtor.setAccessible(true);
        commissionCtor.newInstance();

        Constructor<KafkaTopics> kafkaCtor = KafkaTopics.class.getDeclaredConstructor();
        kafkaCtor.setAccessible(true);
        kafkaCtor.newInstance();
    }
}

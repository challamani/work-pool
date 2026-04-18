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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonCoreCoverageTest {

    @Test
    void shouldCoverApiResponseFactories() {
        ApiResponse<String> ok = ApiResponse.ok("hello");
        assertTrue(ok.isSuccess());
        assertEquals("hello", ok.getData());
        assertNotNull(ok.getTimestamp());
        ok.toString();
        ok.hashCode();
        assertTrue(ok.equals(ok));

        ApiResponse<String> okWithMessage = ApiResponse.ok("hello", "done");
        assertEquals("done", okWithMessage.getMessage());
        assertNotNull(okWithMessage.toString());

        ApiResponse<String> error = ApiResponse.error("ERR", "failed");
        assertFalse(error.isSuccess());
        assertEquals("ERR", error.getErrorCode());
        assertEquals("failed", error.getMessage());
        assertFalse(error.equals(ok));
        assertNotEquals(error.hashCode(), ok.hashCode());

        ApiResponse<String> empty = new ApiResponse<>();
        assertNotNull(empty.getTimestamp());
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
        location.toString();
        location.hashCode();
        GeoLocation locationFromSetters = new GeoLocation();
        locationFromSetters.setLatitude(10.1);
        locationFromSetters.setLongitude(20.2);
        locationFromSetters.setCity("City");
        locationFromSetters.setDistrict("District");
        locationFromSetters.setState("State");
        locationFromSetters.setPincode("600001");
        assertEquals("City", locationFromSetters.getCity());
        assertFalse(location.equals(locationFromSetters));

        Instant now = Instant.now();
        PaymentEscrowEvent payment = new PaymentEscrowEvent(
                "p1", "t1", "u1", "u2", BigDecimal.TEN, BigDecimal.ONE, now);
        assertEquals(BigDecimal.TEN, payment.getTotalAmount());
        payment.toString();
        payment.hashCode();
        PaymentEscrowEvent paymentFromSetters = new PaymentEscrowEvent();
        paymentFromSetters.setPaymentId("p2");
        paymentFromSetters.setTaskId("t2");
        paymentFromSetters.setPublisherId("u1");
        paymentFromSetters.setFinisherId("u2");
        paymentFromSetters.setTotalAmount(BigDecimal.valueOf(100));
        paymentFromSetters.setCommissionAmount(BigDecimal.ONE);
        paymentFromSetters.setCreatedAt(now);
        assertEquals("p2", paymentFromSetters.getPaymentId());
        assertFalse(payment.equals(paymentFromSetters));

        TaskCompletedEvent completed = new TaskCompletedEvent("t1", "u1", "u2", "b1", now);
        assertEquals("u2", completed.getFinisherId());
        completed.toString();
        completed.hashCode();
        TaskCompletedEvent completedFromSetters = new TaskCompletedEvent();
        completedFromSetters.setTaskId("t2");
        completedFromSetters.setPublisherId("u1");
        completedFromSetters.setFinisherId("u2");
        completedFromSetters.setBidId("b2");
        completedFromSetters.setCompletedAt(now);
        assertEquals("b2", completedFromSetters.getBidId());
        assertFalse(completed.equals(completedFromSetters));

        TaskPostedEvent posted = new TaskPostedEvent(
                "t1", "u1", "Task", TaskCategory.COOKING, List.of("Skill"),
                "City", "District", "State", 10.2, 20.3, BigDecimal.ONE, now);
        assertEquals(TaskCategory.COOKING, posted.getCategory());
        posted.toString();
        posted.hashCode();
        TaskPostedEvent postedFromSetters = new TaskPostedEvent();
        postedFromSetters.setTaskId("t2");
        postedFromSetters.setPublisherId("u1");
        postedFromSetters.setTitle("Task");
        postedFromSetters.setCategory(TaskCategory.CLEANING);
        postedFromSetters.setRequiredSkills(List.of("Cleaning"));
        postedFromSetters.setCity("City");
        postedFromSetters.setDistrict("District");
        postedFromSetters.setState("State");
        postedFromSetters.setLatitude(11.1);
        postedFromSetters.setLongitude(22.2);
        postedFromSetters.setBudget(BigDecimal.valueOf(500));
        postedFromSetters.setPostedAt(now);
        assertEquals("Task", postedFromSetters.getTitle());
        assertFalse(posted.equals(postedFromSetters));

        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId("u1")
                .type(NotificationType.DIRECT_MESSAGE)
                .title("Title")
                .message("Message")
                .metadata(Map.of("k", "v"))
                .createdAt(now)
                .build();
        assertEquals(NotificationType.DIRECT_MESSAGE, event.getType());
        event.toString();
        event.hashCode();
        NotificationEvent notificationFromSetters = new NotificationEvent();
        notificationFromSetters.setRecipientUserId("u2");
        notificationFromSetters.setType(NotificationType.SYSTEM_ALERT);
        notificationFromSetters.setTitle("Alert");
        notificationFromSetters.setMessage("Message");
        notificationFromSetters.setMetadata(Map.of("a", "b"));
        notificationFromSetters.setCreatedAt(now);
        assertEquals("u2", notificationFromSetters.getRecipientUserId());
        assertFalse(event.equals(notificationFromSetters));
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

package com.workpool.common.util;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String TASK_POSTED = "workpool.task.posted";
    public static final String TASK_COMPLETED = "workpool.task.completed";
    public static final String TASK_CANCELLED = "workpool.task.cancelled";
    public static final String BID_PLACED = "workpool.bid.placed";
    public static final String BID_ACCEPTED = "workpool.bid.accepted";
    public static final String PAYMENT_ESCROW = "workpool.payment.escrow";
    public static final String PAYMENT_RELEASED = "workpool.payment.released";
    public static final String NOTIFICATION_SEND = "workpool.notification.send";
    public static final String RATING_SUBMITTED = "workpool.rating.submitted";
}

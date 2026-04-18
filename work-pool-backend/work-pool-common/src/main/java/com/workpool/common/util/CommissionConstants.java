package com.workpool.common.util;

/**
 * Work Pool platform commission constants.
 * Commission is 1% from each side (publisher and finisher).
 */
public final class CommissionConstants {
    private CommissionConstants() {}

    /** 1% commission charged from the task publisher */
    public static final double PUBLISHER_COMMISSION_RATE = 0.01;

    /** 1% commission charged from the task finisher */
    public static final double FINISHER_COMMISSION_RATE = 0.01;

    /** Maximum service radius in kilometres for task matching */
    public static final int DEFAULT_MAX_RADIUS_KM = 100;

    /** Default radius used if finisher has not set their preference */
    public static final int DEFAULT_RADIUS_KM = 20;
}

package com.billing.util;

public final class AmountUtil {

    private AmountUtil() {
        // Utility class – prevent instantiation
    }

    /**
     * Rounds the given value to the nearest whole number.
     * Null-safe.
     */
    public static double round(Double value) {
        return value == null ? 0.0 : Math.round(value);
    }

    /**
     * Rounds the result of an arithmetic operation safely.
     */
    public static double round(double value) {
        return Math.round(value);
    }
}

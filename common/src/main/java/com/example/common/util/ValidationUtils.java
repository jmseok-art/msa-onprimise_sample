package com.example.common.util;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    public static void requirePositive(Number n, String message) {
        if (n == null || n.doubleValue() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}

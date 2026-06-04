package com.staytrack.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidationUtil() {
    }

    public static boolean validPhone(String value) {
        return value != null && value.matches("\\d{7,15}");
    }

    public static boolean validEmail(String value) {
        return value == null || value.isBlank() || EMAIL.matcher(value).matches();
    }

    public static boolean positive(int value) {
        return value > 0;
    }

    public static boolean nonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }
}

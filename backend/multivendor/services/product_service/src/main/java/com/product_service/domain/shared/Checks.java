package com.product_service.domain.shared;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;

import java.util.Objects;

public final class Checks {
    private Checks() {
    }

    public static void rule(boolean valid, String message) {
        if (!valid) throw new DomainException(ErrorCode.INVALID_DOMAIN_RULE);
    }

    public static <T> T required(T value, String name) {

        return Objects.requireNonNull(value, name);
    }

    public static String text(String value, String name, int max) {
        rule(value != null && !value.isBlank(), name + " must not be blank");
        String result = value.strip();
        rule(result.length() <= max, name + " is too long");
        return result;
    }
}

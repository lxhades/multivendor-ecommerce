package com.catalog_service.domain.model.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount) {
    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
}

package com.product_service.domain.model.vo;

import java.math.BigDecimal;

import static com.product_service.domain.shared.Checks.required;
import static com.product_service.domain.shared.Checks.rule;

/**
 * Single currency catalog; currency is configured outside the domain.
 */
public record Money(BigDecimal amount) implements Comparable<Money> {
    public Money {
        required(amount, "amount");
        rule(amount.signum() >= 0, "Negative price");
        amount = amount.stripTrailingZeros();
    }

    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }
}

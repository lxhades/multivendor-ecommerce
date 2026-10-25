package com.product_service.application.product.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface SalesPort {

    record Summary(
            long sold,
            long refunded,
            BigDecimal gross,
            BigDecimal refunds,
            BigDecimal net
    ) {
    }

    Summary stats(
            UUID shopId,
            UUID productId,
            LocalDate from,
            LocalDate to
    );

    boolean apply(
            UUID eventId,
            String type,
            UUID orderItemId,
            UUID productId,
            UUID variantId,
            long quantity,
            BigDecimal amount,
            Instant occurredAt
    );
}
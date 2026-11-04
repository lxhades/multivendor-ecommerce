package com.order_service.application.checkout.dto;

import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.enums.CheckoutStatus;
import com.order_service.domain.model.enums.PaymentMethod;

import java.time.Instant;
import java.util.UUID;

public record CheckoutResult(
        UUID id,
        UUID buyerId,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        CheckoutStatus status,
        Instant expiresAt
) {

    public static CheckoutResult from(Checkout c) {
        return new CheckoutResult(
                c.getId(),
                c.getBuyerId(),
                c.getIdempotencyKey(),
                c.getPaymentMethod(),
                c.getStatus(),
                c.getExpiresAt()
        );
    }
}
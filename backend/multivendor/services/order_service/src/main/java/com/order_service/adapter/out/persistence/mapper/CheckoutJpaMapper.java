package com.order_service.adapter.out.persistence.mapper;

import com.order_service.adapter.out.persistence.entity.CheckoutJpaEntity;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.enums.CheckoutStatus;
import com.order_service.domain.model.enums.PaymentMethod;

public final class CheckoutJpaMapper {

    private CheckoutJpaMapper() {
    }

    public static CheckoutJpaEntity toJpa(Checkout c) {
        var e = new CheckoutJpaEntity();

        e.id = c.getId();
        e.buyerId = c.getBuyerId();
        e.idempotencyKey = c.getIdempotencyKey();
        e.paymentMethod = c.getPaymentMethod().name();
        e.status = c.getStatus().name();
        e.expiresAt = c.getExpiresAt();
        e.createdAt = c.getCreatedAt();
        e.updatedAt = c.getUpdatedAt();
        e.version = c.getVersion();

        return e;
    }

    public static Checkout toDomain(CheckoutJpaEntity e) {
        return Checkout.restore(
                e.id,
                e.buyerId,
                e.idempotencyKey,
                PaymentMethod.valueOf(e.paymentMethod),
                CheckoutStatus.valueOf(e.status),
                e.expiresAt,
                e.createdAt,
                e.updatedAt,
                e.version
        );
    }
}
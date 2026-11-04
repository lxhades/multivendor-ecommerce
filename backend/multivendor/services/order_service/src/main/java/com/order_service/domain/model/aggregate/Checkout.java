package com.order_service.domain.model.aggregate;

import com.order_service.domain.model.enums.CheckoutStatus;
import com.order_service.domain.model.enums.PaymentMethod;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Checkout {

    private final UUID id;
    private final UUID buyerId;
    private final String idempotencyKey;
    private final PaymentMethod paymentMethod;
    private CheckoutStatus status;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Long version;

    private Checkout(
            UUID id,
            UUID buyerId,
            String idempotencyKey,
            PaymentMethod paymentMethod,
            CheckoutStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.buyerId = Objects.requireNonNull(buyerId);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key required");
        }
        this.idempotencyKey = idempotencyKey;

        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.status = Objects.requireNonNull(status);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Invalid timestamps");
        }

        this.version = version;
    }

    public static Checkout create(
            UUID id,
            UUID buyerId,
            String idempotencyKey,
            PaymentMethod paymentMethod,
            Instant now
    ) {
        return new Checkout(
                id,
                buyerId,
                idempotencyKey,
                paymentMethod,
                CheckoutStatus.PENDING,
                now.plus(Duration.ofMinutes(15)),
                now,
                now,
                null
        );
    }

    public static Checkout restore(
            UUID id,
            UUID buyerId,
            String idempotencyKey,
            PaymentMethod paymentMethod,
            CheckoutStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        return new Checkout(
                id,
                buyerId,
                idempotencyKey,
                paymentMethod,
                status,
                expiresAt,
                createdAt,
                updatedAt,
                version
        );
    }

    private void move(
            CheckoutStatus expectedStatus,
            CheckoutStatus targetStatus,
            Instant now
    ) {
        Objects.requireNonNull(now);

        if (status == targetStatus) {
            return;
        }

        if (status != expectedStatus) {
            throw new IllegalStateException("Invalid checkout transition");
        }

        if (now.isBefore(updatedAt)) {
            throw new IllegalArgumentException("Time went backwards");
        }

        if (now.isAfter(expiresAt) && targetStatus != CheckoutStatus.EXPIRED) {
            throw new IllegalStateException("Checkout expired");
        }

        status = targetStatus;
        updatedAt = now;
    }

    public void startProcessing(Instant now) {
        move(CheckoutStatus.PENDING, CheckoutStatus.PROCESSING, now);
    }

    public void complete(Instant now) {
        move(CheckoutStatus.PROCESSING, CheckoutStatus.COMPLETED, now);
    }

    public void fail(Instant now) {
        move(CheckoutStatus.PROCESSING, CheckoutStatus.FAILED, now);
    }

    public void expire(Instant now) {
        Objects.requireNonNull(now);

        if (now.isBefore(expiresAt)) {
            throw new IllegalStateException("Not expired yet");
        }

        if (status == CheckoutStatus.EXPIRED) {
            return;
        }

        if (status != CheckoutStatus.PENDING && status != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException("Cannot expire");
        }

        status = CheckoutStatus.EXPIRED;
        updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public CheckoutStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVe rsion() {
        return version;
    }
}
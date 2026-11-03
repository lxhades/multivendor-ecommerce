
package com.order_service.domain.model.aggregate;

import com.order_service.domain.model.enums.CheckoutStatus;
import com.order_service.domain.model.enums.PaymentMethod;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Checkout {

    private static final Duration CHECKOUT_TTL =
            Duration.ofMinutes(15);

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
        this.id = Objects.requireNonNull(
                id, "id is required"
        );
        this.buyerId = Objects.requireNonNull(
                buyerId, "buyerId is required"
        );
        this.idempotencyKey = Objects.requireNonNull(
                idempotencyKey, "idempotencyKey is required"
        );

        if (idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key cannot be blank"
            );
        }

        this.paymentMethod = Objects.requireNonNull(
                paymentMethod, "paymentMethod is required"
        );
        this.status = Objects.requireNonNull(
                status, "status is required"
        );

        this.expiresAt = Objects.requireNonNull(
                expiresAt, "expiresAt is required"
        );
        this.createdAt = Objects.requireNonNull(
                createdAt, "createdAt is required"
        );
        this.updatedAt = Objects.requireNonNull(
                updatedAt, "updatedAt is required"
        );

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after createdAt"
            );
        }

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "updatedAt cannot be before createdAt"
            );
        }

        if (version != null && version < 0) {
            throw new IllegalArgumentException(
                    "version cannot be negative"
            );
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
        Objects.requireNonNull(now, "now is required");

        return new Checkout(
                id,
                buyerId,
                idempotencyKey,
                paymentMethod,
                CheckoutStatus.PENDING,
                now.plus(CHECKOUT_TTL),
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



    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now is required");
        return !now.isBefore(expiresAt);
    }

    public void startProcessing(Instant now) {
        Objects.requireNonNull(now, "now is required");

        if (status == CheckoutStatus.PROCESSING) {
            return;
        }

        ensurePendingAndNotExpired(now);

        this.status = CheckoutStatus.PROCESSING;
        this.updatedAt = now;
    }

    public void complete(Instant now) {
        Objects.requireNonNull(now, "now is required");

        if (status == CheckoutStatus.COMPLETED) {
            return;
        }

        if (status != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Only PROCESSING checkout can be completed"
            );
        }

        if (isExpired(now)) {
            throw new IllegalStateException(
                    "Checkout has expired"
            );
        }

        this.status = CheckoutStatus.COMPLETED;
        this.updatedAt = now;
    }

    public void fail(Instant now) {
        Objects.requireNonNull(now, "now is required");

        if (status == CheckoutStatus.FAILED) {
            return;
        }

        if (status != CheckoutStatus.PENDING
                && status != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot fail checkout in status: " + status
            );
        }

        this.status = CheckoutStatus.FAILED;
        this.updatedAt = now;
    }

    public void expire(Instant now) {
        Objects.requireNonNull(now, "now is required");

        if (status == CheckoutStatus.EXPIRED) {
            return;
        }

        if (status != CheckoutStatus.PENDING
                && status != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot expire checkout in status: " + status
            );
        }

        if (!isExpired(now)) {
            throw new IllegalStateException(
                    "Checkout has not expired yet"
            );
        }

        this.status = CheckoutStatus.EXPIRED;
        this.updatedAt = now;
    }

    private void ensurePendingAndNotExpired(Instant now) {
        if (status != CheckoutStatus.PENDING) {
            throw new IllegalStateException(
                    "Checkout must be PENDING"
            );
        }

        if (isExpired(now)) {
            throw new IllegalStateException(
                    "Checkout has expired"
            );
        }
    }

    // ===== GETTERS =====

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

    public Long getVersion() {
        return version;
    }
}

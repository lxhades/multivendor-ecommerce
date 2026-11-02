package com.inventory_service.domain.model.entity;

import com.inventory_service.domain.model.enums.ReservationStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StockReservation {

    private final UUID id;
    private final UUID orderId;
    private final UUID inventoryId;
    private final long quantity;

    private ReservationStatus status;

    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private StockReservation(
            UUID id,
            UUID orderId,
            UUID inventoryId,
            long quantity,
            ReservationStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.orderId = Objects.requireNonNull(orderId);
        this.inventoryId = Objects.requireNonNull(inventoryId);

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be positive"
            );
        }

        this.quantity = quantity;
        this.status = Objects.requireNonNull(status);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static StockReservation create(
            UUID id,
            UUID orderId,
            UUID inventoryId,
            long quantity,
            Instant expiresAt,
            Instant now
    ) {
        Objects.requireNonNull(expiresAt);
        Objects.requireNonNull(now);

        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException(
                    "Expiration must be in the future"
            );
        }

        return new StockReservation(
                id,
                orderId,
                inventoryId,
                quantity,
                ReservationStatus.RESERVED,
                expiresAt,
                now,
                now,
                null
        );
    }

    public static StockReservation restore(
            UUID id,
            UUID orderId,
            UUID inventoryId,
            long quantity,
            ReservationStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        return new StockReservation(
                id,
                orderId,
                inventoryId,
                quantity,
                status,
                expiresAt,
                createdAt,
                updatedAt,
                version
        );
    }

    public void confirm(Instant now) {
        ensureReserved(now);
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = now;
    }

    public void release(Instant now) {
        ensureReserved(now);
        this.status = ReservationStatus.RELEASED;
        this.updatedAt = now;
    }

    public void expire(Instant now) {
        Objects.requireNonNull(now);

        if (status == ReservationStatus.EXPIRED) {
            return;
        }

        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Only RESERVED reservation can expire"
            );
        }

        if (now.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "Reservation has not expired"
            );
        }

        this.status = ReservationStatus.EXPIRED;
        this.updatedAt = now;
    }

    private void ensureReserved(Instant now) {
        Objects.requireNonNull(now);

        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Reservation is no longer active"
            );
        }

        if (!now.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "Reservation has expired"
            );
        }
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getInventoryId() { return inventoryId; }
    public long getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }


}
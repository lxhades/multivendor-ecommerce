package com.inventory_service.domain.model.aggregate;

import com.inventory_service.domain.exception.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Inventory {

    private final UUID id;
    private final UUID productId;
    private final UUID variantId;
    private final UUID shopId;

    private long onHandQuantity;
    private long reservedQuantity;

    private final Instant createdAt;
    private Instant updatedAt;

    private Inventory(
            UUID id,
            UUID productId,
            UUID variantId,
            UUID shopId,
            long onHandQuantity,
            long reservedQuantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.variantId = Objects.requireNonNull(variantId);
        this.shopId = Objects.requireNonNull(shopId);

        if (onHandQuantity < 0
                || reservedQuantity < 0
                || reservedQuantity > onHandQuantity) {
            throw new IllegalArgumentException(
                    "Invalid inventory quantities"
            );
        }

        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Inventory initialize(
            UUID id,
            UUID productId,
            UUID variantId,
            UUID shopId,
            Instant now
    ) {
        return new Inventory(
                id,
                productId,
                variantId,
                shopId,
                0,
                0,
                now,
                now
        );
    }

    public static Inventory restore(
            UUID id,
            UUID productId,
            UUID variantId,
            UUID shopId,
            long onHandQuantity,
            long reservedQuantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Inventory(
                id,
                productId,
                variantId,
                shopId,
                onHandQuantity,
                reservedQuantity,
                createdAt,
                updatedAt
        );
    }

    public void increaseStock(long quantity, Instant now) {
        if (quantity <= 0) {
            throw new DomainException(
                    "Quantity must be positive"
            );
        }

        this.onHandQuantity = Math.addExact(
                this.onHandQuantity, quantity
        );
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void adjustStock(long newQuantity, Instant now) {
        if (newQuantity < reservedQuantity) {
            throw new DomainException(
                    "Stock cannot be less than reserved quantity"
            );
        }

        this.onHandQuantity = newQuantity;
        this.updatedAt = Objects.requireNonNull(now);
    }

    public long availableQuantity() {
        return onHandQuantity - reservedQuantity;
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getVariantId() { return variantId; }
    public UUID getShopId() { return shopId; }

    public long getOnHandQuantity() { return onHandQuantity; }
    public long getReservedQuantity() { return reservedQuantity; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
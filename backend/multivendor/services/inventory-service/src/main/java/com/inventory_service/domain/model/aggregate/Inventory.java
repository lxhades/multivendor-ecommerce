package com.inventory_service.domain.model.aggregate;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;

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
    private final Long version;

    private Inventory(
            UUID id,
            UUID productId,
            UUID variantId,
            UUID shopId,
            long onHandQuantity,
            long reservedQuantity,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.variantId = Objects.requireNonNull(variantId);
        this.shopId = Objects.requireNonNull(shopId);

        if (onHandQuantity < 0 || reservedQuantity < 0
                || reservedQuantity > onHandQuantity) {
            throw new IllegalArgumentException("Invalid inventory quantities");
        }
        if (version != null && version < 0) {
            throw new IllegalArgumentException("Invalid version");
        }

        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static Inventory initialize(
            UUID id,
            UUID productId,
            UUID variantId,
            UUID shopId,
            Instant now
    ) {
        return new Inventory(
                id, productId, variantId, shopId,
                0, 0, now, now, null
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
            Instant updatedAt,
            Long version
    ) {
        return new Inventory(
                id, productId, variantId, shopId,
                onHandQuantity, reservedQuantity,
                createdAt, updatedAt, Objects.requireNonNull(version)
        );
    }

    public Long getVersion() {
        return version;
    }

    public void increaseStock(long quantity, Instant now) {
        if (quantity <= 0) {
            throw new DomainException(ErrorCode.INVALID_INVENTORY_QUANTITY);
        }

        this.onHandQuantity = Math.addExact(
                this.onHandQuantity, quantity
        );
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void adjustStock(long newQuantity, Instant now) {
        if (newQuantity < reservedQuantity) {
            throw new DomainException(ErrorCode.STOCK_BELOW_RESERVED_QUANTITY);
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
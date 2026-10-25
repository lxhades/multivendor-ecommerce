package com.product_service.domain.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ProductVariant {
    private final UUID id;
    private String sku;
    private BigDecimal price;
    private String attributesJson;
    private boolean active;
    private long soldCount;
    private Instant deletedAt;

    public ProductVariant(UUID id, String sku, BigDecimal price, String attributesJson, boolean active, long soldCount, Instant deletedAt) {
        this.id = Objects.requireNonNull(id);
        edit(sku, price, attributesJson, active);
        if (soldCount < 0) throw new IllegalArgumentException("soldCount");
        this.soldCount = soldCount;
        this.deletedAt = deletedAt;
    }

    public void edit(String sku, BigDecimal price, String attributesJson, boolean active) {
        if (sku == null || sku.isBlank() || price == null || price.signum() < 0)
            throw new IllegalArgumentException("Invalid variant");
        this.sku = sku;
        this.price = price;
        this.attributesJson = attributesJson;
        this.active = active;
    }

    public void softDelete(Instant at) {
        this.deletedAt = Objects.requireNonNull(at);
        this.active = false;
    }

    public void recordSale(long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity");
        soldCount = Math.addExact(soldCount, quantity);
    }

    public UUID id() {
        return id;
    }

    public String sku() {
        return sku;
    }

    public BigDecimal price() {
        return price;
    }

    public String attributesJson() {
        return attributesJson;
    }

    public boolean active() {
        return active;
    }

    public long soldCount() {
        return soldCount;
    }

    public Instant deletedAt() {
        return deletedAt;
    }
}

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
    private String imageUrl;

    public String imageUrl() { return imageUrl; }
    public void changeImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public ProductVariant(UUID id, String sku, BigDecimal price, String attributesJson, boolean active, long soldCount, Instant deletedAt, String imageUrl) {
        this(id, sku, price, attributesJson, active, soldCount, deletedAt);
        this.imageUrl = imageUrl;
    }
    private boolean active;
    private long soldCount;
    private Instant deletedAt;

    public ProductVariant(UUID id, String sku, BigDecimal price, String attributesJson, boolean active, long soldCount, Instant deletedAt) {
        this.id = Objects.requireNonNull(id);
        edit(sku, price, attributesJson, active);
        if (soldCount < 0) throw new IllegalArgumentException("Số lượng đã bán không được là số âm");
        this.soldCount = soldCount;
        this.deletedAt = deletedAt;
    }

    public void edit(String sku, BigDecimal price, String attributesJson, boolean active) {
        if (sku == null || sku.isBlank() || price == null || price.signum() < 0)
            throw new IllegalArgumentException("Thông tin biến thể không hợp lệ");
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
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
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

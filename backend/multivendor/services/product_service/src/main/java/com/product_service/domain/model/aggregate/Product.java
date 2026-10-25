package com.product_service.domain.model.aggregate;

import com.product_service.domain.model.entity.ProductApprovalHistory;
import com.product_service.domain.model.entity.ProductVariant;
import com.product_service.domain.model.enumtype.ApprovalStatus;
import com.product_service.domain.model.enumtype.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class Product {
    private final UUID id, shopId;
    private UUID categoryId;
    private String name, slug, description, mediaJson;
    private ProductStatus status;
    private ApprovalStatus approvalStatus;
    private final List<ProductVariant> variants = new ArrayList<>();
    private final List<ProductApprovalHistory> histories = new ArrayList<>();
    private BigDecimal minPrice;
    private long soldCount;
    private Instant createdAt, updatedAt, deletedAt;

    public Product(
            UUID id, UUID shopId, UUID categoryId,
            String name, String slug, String description, String mediaJson, Instant now) {
        this.id = Objects.requireNonNull(id);
        this.shopId = Objects.requireNonNull(shopId);
        this.createdAt = Objects.requireNonNull(now);
        this.updatedAt = now;
        this.status = ProductStatus.INACTIVE;
        this.approvalStatus = ApprovalStatus.PENDING;
        editFields(categoryId, name, slug, description, mediaJson);
    }

    public static Product rehydrate(
            UUID id, UUID shopId, UUID categoryId,
            String name, String slug, String description,
            String mediaJson, ProductStatus status, ApprovalStatus approval,
            long soldCount,
            Instant createdAt, Instant updatedAt, Instant deletedAt,
            List<ProductVariant> variants,
            List<ProductApprovalHistory> histories) {
        Product p = new Product(id, shopId, categoryId, name, slug, description, mediaJson, createdAt);
        p.status = status;
        p.approvalStatus = approval;
        p.soldCount = soldCount;
        p.updatedAt = updatedAt;
        p.deletedAt = deletedAt;
        p.variants.addAll(variants);
        p.histories.addAll(histories);
        p.recalculateMinPrice();
        return p;
    }

    private void editFields(
            UUID categoryId, String name, String slug, String description, String mediaJson) {
        if (categoryId == null || name == null || name.isBlank() || slug == null || slug.isBlank())
            throw new IllegalArgumentException("Invalid product");
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.mediaJson = mediaJson;
    }

    private void checkLive() {
        if (deletedAt != null) throw new IllegalStateException("Product deleted");
    }

    public void edit(
            UUID categoryId, String name, String slug, String description, String mediaJson, Instant at) {
        checkLive();
        editFields(categoryId, name, slug, description, mediaJson);
        approvalStatus = ApprovalStatus.PENDING;
        updatedAt = at;
    }

    public void addVariant(ProductVariant v, Instant at) {
        checkLive();
        if (variants.stream()
                .anyMatch(x -> x.id().equals(v.id()) || x.deletedAt() == null && x.sku().equals(v.sku())))
            throw new IllegalArgumentException("Duplicate variant");
        variants.add(v);
        approvalStatus = ApprovalStatus.PENDING;
        updatedAt = at;
        recalculateMinPrice();
    }

    public void editVariant(UUID id, String sku, BigDecimal price, String attrs, boolean active, Instant at) {
        checkLive();
        ProductVariant v = variant(id);
        if (variants.stream().anyMatch(x -> !x.id().equals(id) && x.deletedAt() == null && x.sku().equals(sku)))
            throw new IllegalArgumentException("Duplicate SKU");
        v.edit(sku, price, attrs, active);
        approvalStatus = ApprovalStatus.PENDING;
        updatedAt = at;
        recalculateMinPrice();
    }

    public void deleteVariant(UUID id, Instant at) {
        checkLive();
        variant(id).softDelete(at);
        approvalStatus = ApprovalStatus.PENDING;
        updatedAt = at;
        recalculateMinPrice();
    }

    public ProductVariant variant(UUID id) {
        return variants.stream().filter(v -> v.id().equals(id) && v.deletedAt() == null).findFirst().orElseThrow(() -> new NoSuchElementException("Variant not found"));
    }

    public void changeStatus(ProductStatus s, Instant at) {
        checkLive();
        status = Objects.requireNonNull(s);
        updatedAt = at;
    }

    public void decide(ApprovalStatus decision, UUID adminId, String reason, Instant at) {
        checkLive();
        if (decision == ApprovalStatus.PENDING) throw new IllegalArgumentException("Invalid decision");
        if (approvalStatus != ApprovalStatus.PENDING) throw new IllegalStateException("Not pending");
        approvalStatus = decision;
        histories.add(new ProductApprovalHistory(UUID.randomUUID(), adminId, decision, reason, at));
        updatedAt = at;
    }

    public void softDelete(Instant at) {
        checkLive();
        deletedAt = at;
        status = ProductStatus.INACTIVE;
        updatedAt = at;
        for (ProductVariant v : variants) if (v.deletedAt() == null) v.softDelete(at);
        recalculateMinPrice();
    }

    public void recordSale(UUID variantId, long qty) {
        variant(variantId).recordSale(qty);
        soldCount = Math.addExact(soldCount, qty);
    }

    public void recalculateMinPrice() {
        minPrice = variants.stream()
                .filter(v -> v.deletedAt() == null && v.active())
                .map(ProductVariant::price)
                .min(BigDecimal::compareTo).orElse(null);
    }

    public boolean canDisplay() {
        return deletedAt == null && status == ProductStatus.ACTIVE && approvalStatus == ApprovalStatus.APPROVED;
    }

    public UUID id() {
        return id;
    }

    public UUID shopId() {
        return shopId;
    }

    public UUID categoryId() {
        return categoryId;
    }

    public String name() {
        return name;
    }

    public String slug() {
        return slug;
    }

    public String description() {
        return description;
    }

    public String mediaJson() {
        return mediaJson;
    }

    public ProductStatus status() {
        return status;
    }

    public ApprovalStatus approvalStatus() {
        return approvalStatus;
    }

    public BigDecimal minPrice() {
        return minPrice;
    }

    public long soldCount() {
        return soldCount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    public List<ProductVariant> variants() {
        return List.copyOf(variants);
    }

    public List<ProductApprovalHistory> histories() {
        return List.copyOf(histories);
    }
}

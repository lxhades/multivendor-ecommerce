package com.product_service.domain.model.aggregate;

import java.time.Instant;
import java.util.*;
import com.product_service.domain.model.vo.*;
import com.product_service.domain.model.entity.*;
import com.product_service.domain.model.enumtype.*;
import static com.product_service.domain.shared.Checks.*;

public final class Product {

    private final ProductId id;
    private final ShopId shopId;
    private CategoryId categoryId;
    private String name, description;
    private Slug slug;
    private ProductStatus status = ProductStatus.DRAFT;
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_SUBMITTED;
    private final Map<VariantId, ProductVariant> variants = new LinkedHashMap<>();
    private List<ProductMedia> media = List.of();
    private final List<ProductApprovalHistory> approvalHistories = new ArrayList<>();
    private RatingSummary rating = RatingSummary.empty();
    private final Instant createdAt;
    private Instant updatedAt;


    public Product(ProductId id, ShopId shopId, CategoryId categoryId, String name, Slug slug, String description, Instant now) {
        this.id = required(id, "id");
        this.shopId = required(shopId, "shopId");
        this.categoryId = required(categoryId, "categoryId");
        this.name = text(name, "product name", 200);
        this.slug = required(slug, "slug");
        this.description = description;
        createdAt = updatedAt = required(now, "now");
    }


    private void assertEditable() {
        rule(status != ProductStatus.ARCHIVED, "Archived product cannot be edited");
        rule(approvalStatus != ApprovalStatus.PENDING, "Pending product cannot be edited");
    }


    private void edited(Instant now) {
        approvalStatus = ApprovalStatus.NOT_SUBMITTED;
        if (status == ProductStatus.PUBLISHED) {
            status = ProductStatus.HIDDEN;
        }
        updatedAt = now;
    }


    public void updateDetails(CategoryId categoryId, String name, Slug slug, String description, Instant now) {
        assertEditable();
        required(categoryId, "categoryId");
        required(slug, "slug");
        required(now, "now");
        String checked = text(name, "product name", 200);
        this.categoryId = categoryId;
        this.name = checked;
        this.slug = slug;
        this.description = description;
        edited(now);
    }


    public void addVariant(ProductVariant variant, Instant now) {
        assertEditable();
        required(variant, "variant");
        required(now, "now");
        rule(!variants.containsKey(variant.id()), "Duplicate variant id");
        assertUniqueSku(variant);
        variants.put(variant.id(), variant);
        edited(now);
    }


    public void updateVariant(ProductVariant variant, Instant now) {
        assertEditable();
        required(variant, "variant");
        required(now, "now");
        rule(variants.containsKey(variant.id()), "Variant not found");
        assertUniqueSku(variant);
        variants.put(variant.id(), variant);
        edited(now);
    }


    private void assertUniqueSku(ProductVariant candidate) {
        rule(
                variants.values().stream()
                        .noneMatch(v -> !v.id().equals(candidate.id()) && v.sku().equals(candidate.sku())),
                "SKU already exists in product"
        );
    }


    public void removeVariant(VariantId variantId, Instant now) {
        assertEditable();
        required(now, "now");
        rule(variants.containsKey(variantId), "Variant not found");
        variants.remove(variantId);
        edited(now);
    }

    public void replaceMedia(List<ProductMedia> media, Instant now) {
        assertEditable();
        List<ProductMedia> copy = List.copyOf(media);
        required(now, "now");
        this.media = copy;
        edited(now);
    }

    public void submitForApproval(Instant now) {
        assertEditable();
        required(now, "now");
        rule(
                approvalStatus == ApprovalStatus.NOT_SUBMITTED || approvalStatus == ApprovalStatus.REJECTED,
                "Cannot submit in current approval state"
        );
        rule(
                variants.values().stream().anyMatch(ProductVariant::active),
                "At least one active variant required"
        );
        approvalStatus = ApprovalStatus.PENDING;
        updatedAt = now;
    }


    public void decide(ApprovalHistoryId historyId, UUID adminId, boolean approved, String reason, Instant now) {
        rule(
                status != ProductStatus.ARCHIVED && approvalStatus == ApprovalStatus.PENDING,
                "Product is not pending approval"
        );
        ApprovalStatus decision = approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
        rule(
                approvalHistories.stream().noneMatch(h -> h.id().equals(historyId)),
                "Duplicate history id"
        );
        ProductApprovalHistory history = new ProductApprovalHistory(historyId, id, adminId, decision, reason, now);
        approvalHistories.add(history);
        approvalStatus = decision;
        updatedAt = now;
    }


    public void publish(Shop shop, Category category, Instant now) {
        required(now, "now");
        required(shop, "shop");
        required(category, "category");
        rule(status != ProductStatus.ARCHIVED, "Archived product");
        rule(
                shopId.equals(shop.getId()) && categoryId.equals(category.getId()),
                "Wrong shop or category"
        );
        shop.assertCanSell();
        rule(category.getStatus() == CategoryStatus.ACTIVE, "Inactive category");
        rule(approvalStatus == ApprovalStatus.APPROVED, "Product not approved");
        rule(
                variants.values().stream().anyMatch(ProductVariant::active),
                "No active variant"
        );
        status = ProductStatus.PUBLISHED;
        updatedAt = now;
    }


    public boolean canDisplay(Shop shop, Category category) {
        return shop != null
                && category != null
                && shopId.equals(shop.getId())
                && categoryId.equals(category.getId())
                && shop.getStatus() == ShopStatus.ACTIVE
                && category.getStatus() == CategoryStatus.ACTIVE
                && status == ProductStatus.PUBLISHED
                && approvalStatus == ApprovalStatus.APPROVED
                && variants.values().stream().anyMatch(ProductVariant::active);
    }


    public void hide(Instant now) {
        required(now, "now");
        rule(status == ProductStatus.PUBLISHED, "Product not published");
        status = ProductStatus.HIDDEN;
        updatedAt = now;
    }


    public void archive(Instant now) {
        required(now, "now");
        status = ProductStatus.ARCHIVED;
        updatedAt = now;
    }


    public void replaceRating(RatingSummary rating, Instant now) {
        required(rating, "rating");
        required(now, "now");
        this.rating = rating;
        updatedAt = now;
    }


    public Optional<Money> getMinPrice() {
        return variants.values().stream()
                .filter(ProductVariant::active)
                .map(ProductVariant::price)
                .min(Money::compareTo);
    }


    public Optional<Money> getMaxPrice() {
        return variants.values().stream()
                .filter(ProductVariant::active)
                .map(ProductVariant::price)
                .max(Money::compareTo);
    }

    public ProductId getId() {
        return id;
    }

    public ShopId getShopId() {
        return shopId;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Slug getSlug() {
        return slug;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public List<ProductVariant> getVariants() {
        return List.copyOf(variants.values());
    }

    public List<ProductMedia> getMedia() {
        return media;
    }

    public List<ProductApprovalHistory> getApprovalHistories() {
        return List.copyOf(approvalHistories);
    }

    public RatingSummary getRating() {
        return rating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
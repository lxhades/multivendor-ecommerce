package com.product_service.domain.model.aggregate;

import com.product_service.domain.exception.DomainException;
import com.product_service.domain.model.enumtype.CategoryStatus;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.model.vo.Slug;

import java.time.Instant;

import static com.product_service.domain.shared.Checks.*;

public final class Category {

    private final CategoryId id;
    private CategoryId parentId;
    private String name, description, imageUrl;
    private Slug slug;
    private CategoryStatus status = CategoryStatus.ACTIVE;
    private int displayOrder;
    private final Instant createdAt;
    private Instant updatedAt;

    public Category(CategoryId id, CategoryId parentId, String name, Slug slug, Instant now) {
        this.id = required(id, "id");
        validateParent(parentId);
        this.parentId = parentId;
        this.name = text(name, "category name", 150);
        validateSlug(slug);
        this.slug = slug;
        createdAt = updatedAt = required(now, "now");
    }

    public static Category restore(
            CategoryId id,
            CategoryId parentId,
            String name,
            Slug slug,
            String description,
            String imageUrl,
            CategoryStatus status,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        Category category = new Category(
                id,
                parentId,
                name,
                slug,
                createdAt
        );

        category.description = description;
        category.imageUrl = imageUrl;
        category.status = required(status, "status");
        category.displayOrder = displayOrder;
        category.updatedAt = required(updatedAt, "updatedAt");

        return category;
    }

    private void validateParent(CategoryId parent) {
        rule(!id.equals(parent), "Category cannot be its own parent");
    }

    private void validateSlug(Slug slug) {
        required(slug, "slug");
        rule(slug.value().length() <= 180, "Category slug too long");
    }

    public void moveTo(CategoryId parent, Instant now) {
        validateParent(parent);
        required(now, "now");
        parentId = parent;
        updatedAt = now;
    }

    public void update(String name, Slug slug, String description, String imageUrl, Integer displayOrder, Instant now) {
        String checked = text(name, "category name", 150);
        validateSlug(slug);
        required(now, "now");
        this.name = checked;
        this.slug = slug;
        this.description = description;
        this.imageUrl = imageUrl;
        int finalOrder = displayOrder == null ? 0 : displayOrder;


        if (finalOrder < 0) {
            throw new DomainException("Thứ tự hiển thị không được là số âm");
        }
        this.displayOrder = finalOrder;
        updatedAt = now;
    }


    public void changeStatus(CategoryStatus status, Instant now) {
        required(status, "status");
        required(now, "now");
        this.status = status;
        updatedAt = now;
    }

    public CategoryId getId() {
        return id;
    }

    public CategoryId getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public Slug getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

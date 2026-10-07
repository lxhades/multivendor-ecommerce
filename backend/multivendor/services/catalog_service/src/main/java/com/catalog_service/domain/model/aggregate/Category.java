package com.catalog_service.domain.model.aggregate;

import com.catalog_service.domain.model.vo.CategoryId;
import com.catalog_service.domain.model.vo.CategoryName;
import com.catalog_service.domain.model.enumtype.CategoryStatus;

import java.time.Instant;
import java.util.Objects;

public class Category {
    private final CategoryId categoryId;
    private CategoryId parentId;
    private CategoryName name;
    private String slug;
    private CategoryStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Category(CategoryId categoryId, CategoryId parentId, CategoryName name, String slug,
                     CategoryStatus status, Instant createdAt, Instant updatedAt) {
        this.categoryId = Objects.requireNonNull(categoryId);
        this.parentId = parentId;
        this.name = Objects.requireNonNull(name);
        this.slug = normalizeSlug(slug);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Category create(CategoryId parentId, CategoryName name, String slug) {
        Instant now = Instant.now();
        return new Category(CategoryId.generate(), parentId, name, slug, CategoryStatus.ACTIVE, now, now);
    }

    public static Category reconstitute(CategoryId id, CategoryId parentId, CategoryName name, String slug,
                                         CategoryStatus status, Instant createdAt, Instant updatedAt) {
        return new Category(id, parentId, name, slug, status, createdAt, updatedAt);
    }

    public void update(CategoryName name, String slug) {
        this.name = Objects.requireNonNull(name);
        this.slug = normalizeSlug(slug);
        this.updatedAt = Instant.now();
    }

    public void changeParent(CategoryId parentId) {
        this.parentId = parentId;
        this.updatedAt = Instant.now();
    }

    public void activate() { this.status = CategoryStatus.ACTIVE; this.updatedAt = Instant.now(); }
    public void deactivate() { this.status = CategoryStatus.INACTIVE; this.updatedAt = Instant.now(); }

    private static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) throw new IllegalArgumentException("Category slug must not be blank");
        return slug.trim().toLowerCase();
    }

    public CategoryId getCategoryId() { return categoryId; }
    public CategoryId getParentId() { return parentId; }
    public CategoryName getName() { return name; }
    public String getSlug() { return slug; }
    public CategoryStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

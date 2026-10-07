package com.catalog_service.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="categories", uniqueConstraints=@UniqueConstraint(name="uk_categories_slug", columnNames="slug"))
public class CategoryJpaEntity {
    @Id private UUID id;
    @Column(name="parent_id") private UUID parentId;
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String slug;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private CategoryStatusJpa status;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    protected CategoryJpaEntity() {}
    public CategoryJpaEntity(UUID id, UUID parentId, String name, String slug, CategoryStatusJpa status, Instant createdAt, Instant updatedAt) {
        this.id=id; this.parentId=parentId; this.name=name; this.slug=slug; this.status=status; this.createdAt=createdAt; this.updatedAt=updatedAt;
    }
    public UUID getId(){return id;} public UUID getParentId(){return parentId;} public String getName(){return name;} public String getSlug(){return slug;} public CategoryStatusJpa getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}

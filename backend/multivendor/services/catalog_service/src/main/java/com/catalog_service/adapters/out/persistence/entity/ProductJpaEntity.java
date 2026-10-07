package com.catalog_service.adapters.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity {
    @Id
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ProductJpaEntity() {}

    public ProductJpaEntity(UUID id, UUID sellerId, String name, String description,
                            BigDecimal price, Status status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public enum Status { DRAFT, ACTIVE, INACTIVE }

    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

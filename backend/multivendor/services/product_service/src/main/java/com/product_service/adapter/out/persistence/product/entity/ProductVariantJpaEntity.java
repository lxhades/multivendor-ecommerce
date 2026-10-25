package com.product_service.adapter.out.persistence.product.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariantJpaEntity {
    @Id
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public ProductJpaEntity product;
    public String sku;
    public BigDecimal price;
    @Column(name = "attributes_json")
    public String attributesJson;
    public boolean active;
    @Column(name = "sold_count")
    public long soldCount;
    @Column(name = "created_at")
    public Instant createdAt;
    @Column(name = "updated_at")
    public Instant updatedAt;
    @Column(name = "deleted_at")
    public Instant deletedAt;
}

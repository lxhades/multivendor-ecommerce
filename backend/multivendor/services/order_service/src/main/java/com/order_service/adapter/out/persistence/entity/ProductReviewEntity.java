package com.order_service.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_reviews_order_product", columnNames = {"order_id", "product_id"}),
        indexes = @Index(name = "idx_product_reviews_product_created", columnList = "product_id, created_at, id"))
@Check(constraints = "rating between 1 and 5 and char_length(trim(comment)) between 1 and 2000")
public class ProductReviewEntity {
    @Id
    public UUID id;
    @Column(name = "product_id", nullable = false, updatable = false)
    public UUID productId;
    @Column(name = "buyer_id", nullable = false, updatable = false)
    public UUID buyerId;
    @Column(name = "buyer_name", nullable = false, length = 255)
    public String buyerName;
    @Column(name = "order_id", nullable = false, updatable = false)
    public UUID orderId;
    @Column(nullable = false)
    public Integer rating;
    @Column(nullable = false, columnDefinition = "text")
    public String comment;
    @Convert(converter = ReviewMediaUrlsConverter.class)
    @Column(name = "media_urls", nullable = false, columnDefinition = "text")
    public List<String> mediaUrls = List.of();
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

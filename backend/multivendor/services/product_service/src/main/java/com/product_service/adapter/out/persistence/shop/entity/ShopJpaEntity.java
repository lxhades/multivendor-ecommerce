package com.product_service.adapter.out.persistence.shop.entity;

import com.product_service.domain.model.enumtype.ShopStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shops", uniqueConstraints = @UniqueConstraint(name = "uk_shops_slug", columnNames = "slug"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    @Column(name = "slug", nullable = false, length = 220)
    private String slug;
    @Column(name = "logo_url", columnDefinition = "text")
    private String logoUrl;
    @Column(name = "banner_url", columnDefinition = "text")
    private String bannerUrl;
    @Column(name = "description", columnDefinition = "text")
    private String description;
    @Column(name = "address", columnDefinition = "text")
    private String address;
    @Column(name = "bank_details", length = 200)
    private String bankDetails;
    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg;
    @Column(name = "rating_count", nullable = false)
    private long ratingCount;
    @Column(name = "product_count", nullable = false)
    private long productCount;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShopStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

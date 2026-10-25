package com.product_service.adapter.out.persistence.product.entity;

import com.product_service.domain.model.enumtype.ApprovalStatus;
import com.product_service.domain.model.enumtype.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity {
    @Id
    public UUID id;
    @Column(name = "shop_id")
    public UUID shopId;
    @Column(name = "category_id")
    public UUID categoryId;
    public String name, slug;
    public String description;
    @Column(name = "media_json")
    public String mediaJson;
    @Enumerated(EnumType.STRING)
    public ProductStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    public ApprovalStatus approvalStatus;
    @Column(name = "min_price")
    public BigDecimal minPrice;
    @Column(name = "sold_count")
    public long soldCount;
    @Column(name = "rating_avg")
    public BigDecimal ratingAvg = BigDecimal.ZERO;
    @Column(name = "rating_count")
    public long ratingCount;
    @Column(name = "created_at")
    public Instant createdAt;
    @Column(name = "updated_at")
    public Instant updatedAt;
    @Column(name = "deleted_at")
    public Instant deletedAt;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<ProductVariantJpaEntity> variants = new ArrayList<>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<ProductApprovalHistoryJpaEntity> histories = new ArrayList<>();
}

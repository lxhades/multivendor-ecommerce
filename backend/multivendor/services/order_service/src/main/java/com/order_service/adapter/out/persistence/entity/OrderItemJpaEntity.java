package com.order_service.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {
    @Id
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    public UUID productId;

    @Column(name = "variant_id", nullable = false)
    public UUID variantId;

    @Column(name = "product_name", nullable = false)
    public String productName;

    @Column(name = "variant_name", columnDefinition = "text")
    public String variantName;

    @Column(name = "variant_image_url", columnDefinition = "text")
    public String variantImageUrl;

    @Column(name = "unit_price", nullable = false)
    public long unitPrice;

    @Column(nullable = false)
    public int quantity;

    @Column(nullable = false)
    public long subtotal;
}

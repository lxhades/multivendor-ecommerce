package com.order_service.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    public UUID id;

    @Column(name = "checkout_id", nullable = false)
    public UUID checkoutId;

    @Column(name = "buyer_id", nullable = false)
    public UUID buyerId;

    @Column(name = "shop_id", nullable = false)
    public UUID shopId;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "text")
    public String shippingAddress;

    @Column(nullable = false)
    public long subtotal;

    @Column(name = "shipping_fee", nullable = false)
    public long shippingFee;

    @Column(name = "total_amount", nullable = false)
    public long totalAmount;

    @Column(name = "payment_method", nullable = false)
    public String paymentMethod;

    @Column(name = "payment_status", nullable = false)
    public String paymentStatus;

    @Column(name = "order_status", nullable = false)
    public String orderStatus;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Version
    public Long version;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    public List<OrderItemJpaEntity> items = new ArrayList<>();
}
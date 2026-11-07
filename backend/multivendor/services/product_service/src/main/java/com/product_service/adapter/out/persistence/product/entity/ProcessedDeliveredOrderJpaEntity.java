package com.product_service.adapter.out.persistence.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "processed_delivered_orders")
public class ProcessedDeliveredOrderJpaEntity {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    protected ProcessedDeliveredOrderJpaEntity() {}
}

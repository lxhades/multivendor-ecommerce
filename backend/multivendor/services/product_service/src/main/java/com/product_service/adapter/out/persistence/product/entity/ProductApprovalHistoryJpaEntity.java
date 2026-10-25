package com.product_service.adapter.out.persistence.product.entity;

import com.product_service.domain.model.enumtype.ApprovalStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_approval_histories")
public class ProductApprovalHistoryJpaEntity {
    @Id
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public ProductJpaEntity product;
    @Column(name = "actor_id")
    public UUID actorId;
    @Enumerated(EnumType.STRING)
    public ApprovalStatus decision;
    public String reason;
    @Column(name = "created_at")
    public Instant createdAt;
}

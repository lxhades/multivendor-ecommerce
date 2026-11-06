package com.order_service.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "checkouts", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_checkout_buyer_idempotency",
                columnNames = {"buyer_id", "idempotency_key"}
        )
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutJpaEntity {
    @Id
    public UUID id;
    @Column(name = "buyer_id", nullable = false)
    public UUID buyerId;
    @Column(name = "idempotency_key", nullable = false)
    public String idempotencyKey;
    @Column(name = "payment_method", nullable = false)
    public String paymentMethod;
    @Column(nullable = false)
    public String status;
    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
    @Version
    public Long version;
}

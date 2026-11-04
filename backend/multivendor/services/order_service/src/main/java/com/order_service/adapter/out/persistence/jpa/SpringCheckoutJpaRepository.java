package com.order_service.adapter.out.persistence.jpa;

import com.order_service.adapter.out.persistence.entity.CheckoutJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringCheckoutJpaRepository extends JpaRepository<CheckoutJpaEntity, UUID> {
    Optional<CheckoutJpaEntity> findByBuyerIdAndIdempotencyKey(UUID buyerId, String idempotencyKey);
}

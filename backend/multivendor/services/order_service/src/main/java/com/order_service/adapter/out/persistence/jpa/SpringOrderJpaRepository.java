package com.order_service.adapter.out.persistence.jpa;

import com.order_service.adapter.out.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringOrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID>,
        JpaSpecificationExecutor<OrderJpaEntity> {
    Optional<OrderJpaEntity> findByCheckoutIdAndShopId(UUID checkoutId, UUID shopId);

    List<OrderJpaEntity> findByCheckoutId(UUID checkoutId);

    List<OrderJpaEntity> findByBuyerId(UUID buyerId);

    List<OrderJpaEntity> findByShopId(UUID shopId);
}

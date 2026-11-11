package com.order_service.adapter.out.persistence.jpa;

import com.order_service.adapter.out.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringOrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID>,
        JpaSpecificationExecutor<OrderJpaEntity> {
    @org.springframework.data.jpa.repository.Query("""
            select (count(i) > 0) from OrderItemJpaEntity i join i.order o
            where o.id = :orderId and o.buyerId = :buyerId and o.orderStatus = 'DELIVERED'
              and i.productId = :productId
            """)
    boolean hasDeliveredProduct(@org.springframework.data.repository.query.Param("orderId") UUID orderId,
                                @org.springframework.data.repository.query.Param("buyerId") UUID buyerId,
                                @org.springframework.data.repository.query.Param("productId") UUID productId);

    Optional<OrderJpaEntity> findByCheckoutIdAndShopId(UUID checkoutId, UUID shopId);

    List<OrderJpaEntity> findByCheckoutId(UUID checkoutId);

    List<OrderJpaEntity> findByBuyerId(UUID buyerId);

    List<OrderJpaEntity> findByShopId(UUID shopId);
}

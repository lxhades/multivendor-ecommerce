
package com.order_service.domain.repository;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    List<Order> saveAll(List<Order> orders);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByCheckoutIdAndShopId(
            UUID checkoutId,
            UUID shopId
    );

    List<Order> findByCheckoutId(UUID checkoutId);

    List<Order> findByBuyerId(UUID buyerId);

    List<Order> findByShopId(UUID shopId);

    Page<Order> findShopOrders(UUID shopId, OrderStatus status, Instant from, Instant until, Pageable page);
}

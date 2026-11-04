package com.order_service.application.order.usecase.flow;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ShipOrderUseCase {
    private final OrderRepository orders;
    private final Clock clock;

    public ShipOrderUseCase(OrderRepository orders, Clock clock) {
        this.orders = orders;
        this.clock = clock;
    }

    @Transactional
    public OrderResult execute(UUID orderId) {
        java.util.Objects.requireNonNull(orderId, "orderId is required");

        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.markShipped(clock.instant());

        return OrderResult.from(orders.save(order));
    }
}

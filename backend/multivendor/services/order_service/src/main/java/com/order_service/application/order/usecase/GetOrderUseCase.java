package com.order_service.application.order.usecase;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetOrderUseCase {
    private final OrderRepository orders;

    public GetOrderUseCase(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public OrderResult execute(UUID orderId) {
        java.util.Objects.requireNonNull(orderId, "orderId is required");

        return OrderResult.from(
                orders.findById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found")));
    }
}

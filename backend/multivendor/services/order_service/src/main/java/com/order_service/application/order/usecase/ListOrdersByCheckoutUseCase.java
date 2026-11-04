package com.order_service.application.order.usecase;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListOrdersByCheckoutUseCase {
    private final OrderRepository orders;

    public ListOrdersByCheckoutUseCase(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<OrderResult> execute(UUID checkoutId) {
        java.util.Objects.requireNonNull(checkoutId, "checkoutId is required");

        return orders.findByCheckoutId(checkoutId)
                .stream()
                .map(OrderResult::from)
                .toList();
    }

}

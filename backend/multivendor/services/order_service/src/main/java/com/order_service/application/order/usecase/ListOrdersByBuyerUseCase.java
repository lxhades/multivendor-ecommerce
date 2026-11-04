package com.order_service.application.order.usecase;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListOrdersByBuyerUseCase {
    private final OrderRepository orders;

    public ListOrdersByBuyerUseCase(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<OrderResult> execute(UUID buyerId) {
        return orders.findByBuyerId(buyerId)
                .stream()
                .map(OrderResult::from)
                .toList();
    }
}

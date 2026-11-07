package com.order_service.application.order.usecase.flow;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class MarkOrderPaidUseCase {
    private final OrderRepository orders;
    private final Clock clock;

    public MarkOrderPaidUseCase(OrderRepository orders, Clock clock) {
        this.orders = orders;
        this.clock = clock;
    }

    @Transactional
    public OrderResult execute(UUID orderId) {
        java.util.Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");

        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        order.markPaid(clock.instant());

        return OrderResult.from(orders.save(order));
    }
}

package com.order_service.application.order.usecase.flow;

import com.order_service.application.order.dto.OrderResult;
import com.order_service.application.checkout.port.OutboxPort;
import com.order_service.application.event.OrderDelivered;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class DeliverOrderUseCase {
    private final OrderRepository orders;
    private final OutboxPort outbox;
    private final Clock clock;

    public DeliverOrderUseCase(OrderRepository orders, OutboxPort outbox, Clock clock) {
        this.orders = orders;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Transactional
    public OrderResult execute(UUID orderId) {
        return execute(orderId, clock.instant());
    }

    @Transactional
    public OrderResult execute(UUID orderId, Instant deliveredAt) {
        Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");
        Objects.requireNonNull(deliveredAt, "Thời điểm giao hàng không được để trống");

        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return OrderResult.from(order);
        }

        order.markDelivered(clock.instant());
        Order saved = orders.save(order);

        var items = saved.getItems().stream()
                .map(item -> new OrderDelivered.Item(
                        item.getProductId(), item.getVariantId(), item.getQuantity()))
                .toList();

        outbox.saveOrderDelivered(new OrderDelivered(
                UUID.randomUUID(),
                OrderDelivered.EVENT_TYPE,
                saved.getId(),
                saved.getShopId(),
                deliveredAt,
                items
        ));

        return OrderResult.from(saved);
    }
}

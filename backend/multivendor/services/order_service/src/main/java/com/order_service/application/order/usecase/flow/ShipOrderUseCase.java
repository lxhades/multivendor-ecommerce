package com.order_service.application.order.usecase.flow;


import com.order_service.application.checkout.port.OutboxPort;
import com.order_service.application.checkout.port.ShopOwnershipPort;
import com.order_service.application.event.OrderShipped;
import com.order_service.application.order.dto.OrderResult;


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
public class ShipOrderUseCase {

    private final OrderRepository orders;
    private final ShopOwnershipPort shopOwnershipPort;
    private final OutboxPort outboxPort;
    private final Clock clock;

    public ShipOrderUseCase(
            OrderRepository orders,
            ShopOwnershipPort shopOwnershipPort,
            OutboxPort outboxPort,
            Clock clock
    ) {
        this.orders = orders;
        this.shopOwnershipPort = shopOwnershipPort;
        this.outboxPort = outboxPort;
        this.clock = clock;
    }

    @Transactional
    public OrderResult execute(UUID orderId, UUID sellerId) {

        Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");
        Objects.requireNonNull(sellerId, "Mã người bán không được để trống");

        Order order = orders.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy đơn hàng")
                );

        if (!shopOwnershipPort.isOwner(
                sellerId,
                order.getShopId()
        )) {
            throw new org.springframework.security.access
                    .AccessDeniedException(
                    "Seller does not own this shop"
            );
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED) {
            return OrderResult.from(order);
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Không thể chuyển đơn hàng sang trạng thái đang giao từ trạng thái: "
                            + order.getOrderStatus()
            );
        }

        Instant now = clock.instant();

        order.startProcessing(now);
        order.markShipped(now);

        Order savedOrder = orders.save(order);

        OrderShipped event = OrderShipped.create(
                savedOrder.getId(),
                savedOrder.getShopId(),
                savedOrder.getShippingAddress(),
                now
        );

        outboxPort.saveOrderShipped(event);

        return OrderResult.from(savedOrder);
    }
}
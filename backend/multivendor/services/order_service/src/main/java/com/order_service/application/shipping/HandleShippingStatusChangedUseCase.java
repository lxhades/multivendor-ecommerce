package com.order_service.application.shipping;


import com.order_service.application.saga.port.InboxPort;
import com.order_service.application.shipping.command.HandleShippingStatusChangedCommand;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class HandleShippingStatusChangedUseCase {

    private static final String CONSUMER_NAME =
            "order-shipping-status-consumer";

    private static final String EVENT_TYPE =
            "ShippingStatusChanged";

    private final OrderRepository orderRepository;
    private final InboxPort inboxPort;
    private final Clock clock;

    public HandleShippingStatusChangedUseCase(
            OrderRepository orderRepository,
            InboxPort inboxPort,
            Clock clock
    ) {
        this.orderRepository = orderRepository;
        this.inboxPort = inboxPort;
        this.clock = clock;
    }

    @Transactional
    public void execute(HandleShippingStatusChangedCommand command) {

        Objects.requireNonNull(command, "Yêu cầu xử lý không được để trống");
        Objects.requireNonNull(command.eventId(), "Mã sự kiện không được để trống");
        Objects.requireNonNull(command.orderId(), "Mã đơn hàng không được để trống");

        if (!"DELIVERED".equals(command.status())) {
            throw new IllegalArgumentException(
                    "Trạng thái vận chuyển không được hỗ trợ: " + command.status()
            );
        }

        boolean claimed = inboxPort.tryClaim(
                CONSUMER_NAME,
                command.eventId(),
                EVENT_TYPE
        );

        if (!claimed) {
            return;
        }

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy đơn hàng: " + command.orderId()
                        )
                );

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return;
        }

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException(
                    "Không thể xác nhận giao hàng từ trạng thái: "
                            + order.getOrderStatus()
            );
        }

        Instant now = clock.instant();

        order.markDelivered(now);
        orderRepository.save(order);
    }
}
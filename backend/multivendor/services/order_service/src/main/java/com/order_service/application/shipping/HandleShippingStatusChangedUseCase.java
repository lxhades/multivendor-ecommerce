package com.order_service.application.shipping;


import com.order_service.application.saga.port.InboxPort;
import com.order_service.application.shipping.command.HandleShippingStatusChangedCommand;

import com.order_service.application.order.usecase.flow.DeliverOrderUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class HandleShippingStatusChangedUseCase {

    private static final String CONSUMER_NAME =
            "order-shipping-status-consumer";

    private static final String EVENT_TYPE =
            "ShippingStatusChanged";

    private final DeliverOrderUseCase deliverOrder;
    private final InboxPort inboxPort;

    public HandleShippingStatusChangedUseCase(
            DeliverOrderUseCase deliverOrder,
            InboxPort inboxPort
    ) {
        this.deliverOrder = deliverOrder;
        this.inboxPort = inboxPort;
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

        deliverOrder.execute(command.orderId(), command.occurredAt());
    }
}

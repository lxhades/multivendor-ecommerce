package com.order_service.application.order.usecase.flow;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.checkout.port.OutboxPort;
import com.order_service.application.event.InventoryReleaseRequested;
import com.order_service.application.order.dto.OrderResult;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class CancelOrderUseCase {

    private final OrderRepository orders;
    private final OutboxPort outbox;
    private final Clock clock;

    public CancelOrderUseCase(
            OrderRepository orders,
            OutboxPort outbox,
            Clock clock
    ) {
        this.orders = orders;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Transactional
    public OrderResult execute(UUID orderId) {

        Order order = orders.findById(orderId)
                .orElseThrow(() ->
                        new DomainException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getPaymentMethod() != PaymentMethod.COD) {
            throw new IllegalStateException(
                    "Chưa hỗ trợ hủy đơn thanh toán qua ATM"
            );
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return OrderResult.from(order);
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED &&
                order.getOrderStatus() != OrderStatus.PENDING
        ) {
            throw new IllegalStateException(
                    "Chỉ có thể hủy đơn thanh toán khi nhận hàng ở trạng thái đã xác nhận hoặc chờ xác nhận"
            );
        }

        order.cancel(clock.instant());

        Order saved = orders.save(order);

        outbox.saveInventoryReleaseRequested(
                new InventoryReleaseRequested(
                        UUID.randomUUID(),
                        InventoryReleaseRequested.EVENT_TYPE,
                        saved.getId()
                )
        );

        return OrderResult.from(saved);
    }
}
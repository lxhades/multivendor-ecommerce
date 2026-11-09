package com.order_service.application.order.usecase.flow;

import com.order_service.application.order.OrderRole;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.checkout.port.OutboxPort;
import com.order_service.application.event.InventoryReleaseRequested;
import com.order_service.application.checkout.port.ShopOwnershipPort;
import com.order_service.application.order.dto.OrderResult;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.repository.OrderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.UUID;

@Service
public class CancelOrderUseCase {

    private final OrderRepository orders;
    private final ShopOwnershipPort ownership;
    private final OutboxPort outbox;
    private final Clock clock;

    public CancelOrderUseCase(
            OrderRepository orders,
            ShopOwnershipPort ownership,
            OutboxPort outbox,
            Clock clock
    ) {
        this.orders = orders;
        this.ownership = ownership;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Transactional
    public OrderResult execute(UUID actorId, UUID orderId, OrderRole role) {

        Order order = orders.findById(orderId)
                .orElseThrow(() ->
                        new DomainException(ErrorCode.ORDER_NOT_FOUND));

        if (role != OrderRole.ADMIN
                && !(role == OrderRole.BUYER && order.getBuyerId().equals(actorId))
                && !(role == OrderRole.SELLER && ownership.isOwner(actorId, order.getShopId()))) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return OrderResult.from(order);
        }

        if (order.getOrderStatus() == OrderStatus.PENDING) {
            throw new DomainException(ErrorCode.INVALID_CANCEL_PENDING_ORDER);
        }

        if (order.getPaymentMethod() != PaymentMethod.COD) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Chưa hỗ trợ hủy đơn thanh toán qua ATM"
            );
        }

        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Chỉ có thể hủy đơn hàng ở trạng thái đã xác nhận"
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

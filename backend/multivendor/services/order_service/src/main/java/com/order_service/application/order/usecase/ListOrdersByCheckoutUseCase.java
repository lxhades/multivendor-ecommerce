package com.order_service.application.order.usecase;

import com.order_service.application.order.OrderRole;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.checkout.port.ShopOwnershipPort;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListOrdersByCheckoutUseCase {
    private final OrderRepository orders;
    private final ShopOwnershipPort ownership;

    public ListOrdersByCheckoutUseCase(OrderRepository orders, ShopOwnershipPort ownership) {
        this.orders = orders;
        this.ownership = ownership;
    }

    @Transactional(readOnly = true)
    public List<OrderResult> execute(UUID checkoutId, UUID actorId, OrderRole role) {
        java.util.Objects.requireNonNull(checkoutId, "Mã phiên thanh toán không được để trống");

        var checkoutOrders = orders.findByCheckoutId(checkoutId);
        var allowedOrders = checkoutOrders.stream()
                .filter(order -> role == OrderRole.ADMIN
                        || (role == OrderRole.BUYER && order.getBuyerId().equals(actorId))
                        || (role == OrderRole.SELLER && ownership.isOwner(actorId, order.getShopId())))
                .map(OrderResult::from)
                .toList();
        if (!checkoutOrders.isEmpty() && allowedOrders.isEmpty()) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }
        return allowedOrders;
    }

}

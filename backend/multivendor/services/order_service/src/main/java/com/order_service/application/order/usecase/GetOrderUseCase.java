package com.order_service.application.order.usecase;

import com.order_service.application.order.OrderRole;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.checkout.port.ShopOwnershipPort;
import com.order_service.application.order.dto.OrderResult;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class GetOrderUseCase {
    private final OrderRepository orderRepository;
    private final ShopOwnershipPort ownership;

    public GetOrderUseCase(OrderRepository orderRepository, ShopOwnershipPort ownership) {
        this.orderRepository = orderRepository;
        this.ownership = ownership;
    }

    @Transactional(readOnly = true)
    public OrderResult execute(UUID orderId, UUID currentUserId, OrderRole role) {
        Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException(ErrorCode.ORDER_NOT_FOUND));

        if (role != OrderRole.ADMIN
                && !(role == OrderRole.BUYER && order.getBuyerId().equals(currentUserId))
                && !(role == OrderRole.SELLER && ownership.isOwner(currentUserId, order.getShopId()))) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }

        return OrderResult.from(order);
    }

}
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
import java.util.Objects;
import java.util.UUID;

@Service
public class ListOrdersByShopUseCase {
    private final OrderRepository orders;
    private final ShopOwnershipPort ownership;

    public ListOrdersByShopUseCase(OrderRepository orders, ShopOwnershipPort ownership) {
        this.orders = orders;
        this.ownership = ownership;
    }

    @Transactional(readOnly = true)
    public List<OrderResult> execute(UUID shopId, UUID sellerId, OrderRole role) {
        Objects.requireNonNull(shopId, "Mã cửa hàng không được để trống");

        if (role != OrderRole.ADMIN && (role != OrderRole.SELLER || !ownership.isOwner(sellerId, shopId))) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }

        return orders
                .findByShopId(shopId)
                .stream()
                .map(OrderResult::from)
                .toList();
    }
}

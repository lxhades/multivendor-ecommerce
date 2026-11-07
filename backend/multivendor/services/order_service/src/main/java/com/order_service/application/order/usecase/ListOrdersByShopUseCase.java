package com.order_service.application.order.usecase;

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

    public ListOrdersByShopUseCase(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<OrderResult> execute(UUID shopId) {
        Objects.requireNonNull(shopId, "Mã cửa hàng không được để trống");

        return orders
                .findByShopId(shopId)
                .stream()
                .map(OrderResult::from)
                .toList();
    }
}

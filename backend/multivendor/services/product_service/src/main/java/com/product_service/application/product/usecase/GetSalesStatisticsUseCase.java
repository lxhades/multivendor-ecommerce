package com.product_service.application.product.usecase;

import com.product_service.application.product.port.SalesPort;
import com.product_service.application.product.port.ShopOwnershipPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class GetSalesStatisticsUseCase {
    private final ShopOwnershipPort owner;
    private final SalesPort sales;

    public GetSalesStatisticsUseCase(ShopOwnershipPort owner, SalesPort sales) {
        this.owner = owner;
        this.sales = sales;
    }

    @Transactional(readOnly = true)
    public SalesPort.Summary execute(UUID sellerId, UUID shopId, UUID productId, LocalDate from, LocalDate to) {
        if (!owner.ownedBy(shopId, sellerId)) throw new RuntimeException("Bạn không có shop này");

        if (from != null && to != null && !from.isBefore(to))
            throw new IllegalArgumentException("Thời gian không hợp lệ");

        return sales.stats(shopId, productId, from, to);
    }
}

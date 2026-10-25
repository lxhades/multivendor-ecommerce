package com.product_service.application.product.usecase;

import com.product_service.application.product.port.SalesPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class RecordSaleEventUseCase {

    private static final Set<String> VALID_EVENT_TYPES = Set.of("SALE_RECORDED", "SALE_REFUNDED");

    private final SalesPort sales;

    public RecordSaleEventUseCase(SalesPort sales) {
        this.sales = sales;
    }

    @Transactional
    public boolean execute(
            UUID eventId,
            String type,
            UUID orderItemId,
            UUID productId,
            UUID variantId,
            long quantity,
            BigDecimal amount,
            Instant occurredAt
    ) {
        if (!VALID_EVENT_TYPES.contains(type)) {
            throw new IllegalArgumentException("Loại sự kiện không hợp lệ. Chỉ chấp nhận SALE_RECORDED hoặc SALE_REFUNDED");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng bán/hoàn phải lớn hơn 0");
        }

        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Số tiền không được để trống và không được nhỏ hơn 0");
        }

        return sales.apply(
                eventId,
                type,
                orderItemId,
                productId,
                variantId,
                quantity,
                amount,
                occurredAt
        );
    }
}

package com.order_service.application.checkout.command;

import com.order_service.domain.model.enums.PaymentMethod;
import java.util.*;

public record CreateCheckoutCommand(
        UUID buyerId,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        String shippingAddress,
        List<CheckoutItemCommand> items
) {
    public CreateCheckoutCommand {
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(paymentMethod);

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Thông tin phiên thanh toán không hợp lệ");
        }

        items = List.copyOf(Objects.requireNonNull(items));

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Phiên thanh toán phải có ít nhất một sản phẩm");
        }

        Set<UUID> variants = new HashSet<>();
        for (CheckoutItemCommand item : items) {
            if (!variants.add(item.variantId())) {
                throw new IllegalArgumentException("Mã biến thể bị trùng");
            }
        }
    }
}

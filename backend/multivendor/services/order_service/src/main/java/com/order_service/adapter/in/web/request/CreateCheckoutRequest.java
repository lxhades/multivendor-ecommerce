package com.order_service.adapter.in.web.request;

import com.order_service.application.checkout.command.CheckoutItemCommand;
import com.order_service.application.checkout.command.CreateCheckoutCommand;
import com.order_service.domain.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record CreateCheckoutRequest(
        @NotNull(message = "Phương thức thanh toán không được để trống") PaymentMethod paymentMethod,
        @NotBlank(message = "Địa chỉ giao hàng không được để trống") String shippingAddress,
        @NotEmpty(message = "Danh sách sản phẩm không được để trống") List<@Valid ItemRequest> items
) {

    public record ItemRequest(
            @NotNull(message = "Mã biến thể không được để trống") UUID variantId,
            @Positive(message = "Số lượng phải lớn hơn 0") int quantity
    ) {}

    public CreateCheckoutCommand toCommand(
            UUID buyerId,
            String idempotencyKey
    ) {
        return new CreateCheckoutCommand(
                buyerId,
                idempotencyKey,
                paymentMethod,
                shippingAddress,
                items.stream()
                        .map(item -> new CheckoutItemCommand(
                                item.variantId(),
                                item.quantity()
                        ))
                        .toList()
        );
    }
}
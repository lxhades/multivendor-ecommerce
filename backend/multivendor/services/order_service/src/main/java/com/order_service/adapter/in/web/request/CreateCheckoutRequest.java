package com.order_service.adapter.in.web.request;

import com.order_service.application.checkout.command.CreateCheckoutCommand;
import com.order_service.application.checkout.command.OrderItemCommand;
import com.order_service.application.checkout.command.ShopOrderCommand;
import com.order_service.domain.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record CreateCheckoutRequest(
        @NotNull UUID buyerId,
        @NotBlank String idempotencyKey,
        @NotNull PaymentMethod paymentMethod,
        @NotBlank String shippingAddress,
        @NotEmpty List<@Valid ShopRequest> shops
) {

    public record ShopRequest(
            @NotNull UUID shopId,
            @NotEmpty List<@Valid ItemRequest> items,
            @PositiveOrZero long shippingFee
    ) {
    }

    public record ItemRequest(
            @NotNull UUID productId,
            @NotNull UUID variantId,
            @NotBlank String productName,
            @PositiveOrZero long unitPrice,
            @Positive int quantity
    ) {
    }

    public CreateCheckoutCommand toCommand() {
        return new CreateCheckoutCommand(
                buyerId,
                idempotencyKey,
                paymentMethod,
                shippingAddress,
                shops.stream()
                        .map(s -> new ShopOrderCommand(
                                s.shopId(),
                                s.items().stream()
                                        .map(i -> new OrderItemCommand(
                                                i.productId(),
                                                i.variantId(),
                                                i.productName(),
                                                i.unitPrice(),
                                                i.quantity()
                                        ))
                                        .toList(),
                                s.shippingFee()
                        ))
                        .toList()
        );
    }
}
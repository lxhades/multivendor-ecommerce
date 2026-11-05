package com.order_service.adapter.in.web.request;

import com.order_service.application.checkout.command.CheckoutItemCommand;
import com.order_service.application.checkout.command.CreateCheckoutCommand;
import com.order_service.domain.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record CreateCheckoutRequest(
        @NotNull PaymentMethod paymentMethod,
        @NotBlank String shippingAddress,
        @NotEmpty List<@Valid ItemRequest> items
) {

    public record ItemRequest(
            @NotNull UUID variantId,
            @Positive int quantity
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
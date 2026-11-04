package com.order_service.application.checkout.dto;

import com.order_service.application.order.dto.OrderResult;

import java.util.List;

public record CreateCheckoutResult(CheckoutResult checkout, List<OrderResult> orders) {
    public CreateCheckoutResult {
        orders = List.copyOf(orders);
    }
}

package com.multivendor.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateShipmentRequest(
        @NotNull(message = "Mã đơn hàng không được để trống")
        UUID orderId,
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String shippingAddress) {
}

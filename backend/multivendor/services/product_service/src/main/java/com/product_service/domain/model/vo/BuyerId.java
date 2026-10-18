package com.product_service.domain.model.vo;

import java.util.UUID;
import static com.product_service.domain.shared.Checks.required;
public record BuyerId(UUID value) {
    public BuyerId { required(value, "BuyerId"); }
    public static BuyerId random() { return new BuyerId(UUID.randomUUID()); }
}

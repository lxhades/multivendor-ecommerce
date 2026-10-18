package com.product_service.domain.model.vo;

import static com.product_service.domain.shared.Checks.*;
public record Sku(String value) {
    public Sku { value = text(value, "Sku", 100); rule(value.matches(".+"), "Invalid Sku"); }
}

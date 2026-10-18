package com.product_service.domain.model.entity;

import java.util.Map;
import com.product_service.domain.model.vo.*;
import static com.product_service.domain.shared.Checks.*;


public record ProductVariant(
        VariantId id,
        Sku sku,
        Money price,
        Map<String, String> attributes,
        boolean active) {


    public ProductVariant {
        required(id, "variant id");
        required(sku, "sku");
        required(price, "price");
        attributes = Map.copyOf(required(attributes, "attributes"));
        attributes.forEach((key, value) -> {
            text(key, "attribute key", 100);
            text(value, "attribute value", 500);
        });
    }
}
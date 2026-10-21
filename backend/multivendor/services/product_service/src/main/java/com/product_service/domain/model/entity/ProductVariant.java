package com.product_service.domain.model.entity;

import com.product_service.domain.model.vo.Money;
import com.product_service.domain.model.vo.Sku;
import com.product_service.domain.model.vo.VariantId;

import java.util.Map;

import static com.product_service.domain.shared.Checks.required;
import static com.product_service.domain.shared.Checks.text;


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
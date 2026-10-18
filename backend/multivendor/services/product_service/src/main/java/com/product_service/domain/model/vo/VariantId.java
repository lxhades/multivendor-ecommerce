package com.product_service.domain.model.vo;

import java.util.UUID;
import static com.product_service.domain.shared.Checks.required;
public record VariantId(UUID value) {
    public VariantId { required(value, "VariantId"); }
    public static VariantId random() { return new VariantId(UUID.randomUUID()); }
}

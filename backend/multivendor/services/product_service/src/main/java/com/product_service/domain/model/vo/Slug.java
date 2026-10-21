package com.product_service.domain.model.vo;

import static com.product_service.domain.shared.Checks.rule;
import static com.product_service.domain.shared.Checks.text;

public record Slug(String value) {
    public Slug {
        value = text(value, "Slug", 220);
        rule(value.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "Invalid Slug");
    }
}

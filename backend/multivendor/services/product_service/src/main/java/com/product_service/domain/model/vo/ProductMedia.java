package com.product_service.domain.model.vo;

import static com.product_service.domain.shared.Checks.*;
public record ProductMedia(String location, String altText, int displayOrder) {
    public ProductMedia { location = text(location, "media location", 2048); rule(displayOrder >= 0, "Negative media order"); }
}

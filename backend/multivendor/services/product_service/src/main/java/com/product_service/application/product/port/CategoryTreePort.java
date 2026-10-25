package com.product_service.application.product.port;

import java.util.Set;
import java.util.UUID;

public interface CategoryTreePort {
    Set<UUID> descendantsIncluding(UUID categoryId);
}

package com.product_service.application.product.port;

import java.util.UUID;

public interface ShopOwnershipPort {
    boolean ownedBy(UUID shopId, UUID sellerId);
}

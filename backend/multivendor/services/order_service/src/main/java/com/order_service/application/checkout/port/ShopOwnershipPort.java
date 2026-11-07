package com.order_service.application.checkout.port;

import java.util.UUID;

public interface ShopOwnershipPort {

    boolean isOwner(UUID sellerId, UUID shopId);
}
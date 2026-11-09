package com.product_service.application.product.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface SalesPort {

    boolean claimDeliveredOrder(UUID orderId);

    void incrementSoldCount(UUID shopId, UUID productId, UUID variantId, long quantity);


}

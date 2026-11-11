
package com.order_service.application.checkout.port;

import java.util.List;
import java.util.UUID;

public interface ProductPricingPort {

    List<VerifiedVariant> verifyVariants(List<UUID> variantIds);

    record VerifiedVariant(
            UUID productId,
            UUID variantId,
            UUID shopId,
            String productName,
            String variantName,
            String variantImageUrl,
            long unitPrice,
            boolean available
    ) {}
}

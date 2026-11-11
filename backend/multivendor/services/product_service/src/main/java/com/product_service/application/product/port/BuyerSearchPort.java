package com.product_service.application.product.port;

import com.product_service.application.product.dto.BuyerSearch;
import java.util.Collection;
import java.util.UUID;

public interface BuyerSearchPort {
    BuyerSearch.Page search(BuyerSearch.Criteria criteria, Collection<UUID> categoryIds);
}

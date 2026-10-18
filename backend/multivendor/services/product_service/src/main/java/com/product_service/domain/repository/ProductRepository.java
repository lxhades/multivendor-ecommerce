package com.product_service.domain.repository;

import java.util.Optional;
import com.product_service.domain.model.aggregate.Product;
import com.product_service.domain.model.vo.*;
public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    Product save(Product aggregate);
    boolean existsByShopIdAndSlug(ShopId shopId, Slug slug, ProductId excludingId);
}

package com.product_service.domain.repository;

import com.product_service.domain.model.aggregate.Product;
import com.product_service.domain.model.vo.ProductId;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.model.vo.Slug;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);

    Product save(Product aggregate);

    void deleteById(ProductId id);

    boolean existsByShopIdAndSlug(ShopId shopId, Slug slug, ProductId excludingId);

}

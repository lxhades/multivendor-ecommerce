package com.product_service.domain.repository;

import com.product_service.domain.model.aggregate.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findByIdAndShopId(UUID id, UUID shopId);

    Optional<Product> findVisibleById(UUID id);

    List<Product> findVisible(UUID shopId, UUID categoryId, Collection<UUID> categoryIds);

    List<Product> findByShop(UUID shopId);

    List<Product> findPending();

    boolean existsSlug(UUID shopId, String slug, UUID exceptId);
}

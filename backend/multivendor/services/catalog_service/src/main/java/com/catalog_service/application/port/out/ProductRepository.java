package com.catalog_service.application.port.out;

import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId productId);
    List<Product> findAll();
    void deleteById(ProductId productId);
}

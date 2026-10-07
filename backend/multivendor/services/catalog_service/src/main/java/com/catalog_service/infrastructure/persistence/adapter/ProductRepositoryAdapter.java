package com.catalog_service.infrastructure.persistence.adapter;

import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.ProductId;
import com.catalog_service.infrastructure.persistence.mapper.ProductPersistenceMapper;
import com.catalog_service.infrastructure.persistence.repository.SpringDataProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository repository;
    private final ProductPersistenceMapper mapper = new ProductPersistenceMapper();

    public ProductRepositoryAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        return mapper.toDomain(repository.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return repository.findById(productId.value()).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(ProductId productId) {
        repository.deleteById(productId.value());
    }
}

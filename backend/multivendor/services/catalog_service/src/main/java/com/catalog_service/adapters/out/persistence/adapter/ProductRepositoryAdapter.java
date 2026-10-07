package com.catalog_service.adapters.out.persistence.adapter;

import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.domain.model.aggregate.Product;
import com.catalog_service.domain.model.vo.ProductId;
import com.catalog_service.adapters.out.persistence.mapper.ProductPersistenceMapper;
import com.catalog_service.adapters.out.persistence.repository.SpringDataProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository repository;
    private final ProductPersistenceMapper mapper;

    public ProductRepositoryAdapter(SpringDataProductRepository repository, ProductPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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

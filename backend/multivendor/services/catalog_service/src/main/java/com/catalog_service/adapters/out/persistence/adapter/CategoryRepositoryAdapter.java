package com.catalog_service.adapters.out.persistence.adapter;

import com.catalog_service.adapters.out.persistence.mapper.CategoryMapper;
import com.catalog_service.adapters.out.persistence.repository.CategoryJpaRepository;
import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.domain.model.aggregate.Category;
import com.catalog_service.domain.model.vo.CategoryId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository repository;
    private final CategoryMapper mapper;
    public CategoryRepositoryAdapter(CategoryJpaRepository repository, CategoryMapper mapper){this.repository=repository;this.mapper=mapper;}
    public Category save(Category c){return mapper.toDomain(repository.save(mapper.toEntity(c)));}
    public Optional<Category> findById(CategoryId id){return repository.findById(id.value()).map(mapper::toDomain);}
    public List<Category> findAll(){return repository.findAll().stream().map(mapper::toDomain).toList();}
    public List<Category> findByParentId(CategoryId id){return repository.findByParentId(id.value()).stream().map(mapper::toDomain).toList();}
    public boolean existsById(CategoryId id){return repository.existsById(id.value());}
    public boolean existsBySlug(String slug){return repository.existsBySlug(slug);}
    public void deleteById(CategoryId id){repository.deleteById(id.value());}
}

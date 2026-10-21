package com.product_service.adapter.out.persistence.category;

import com.product_service.adapter.out.persistence.category.mapper.CategoryPersistenceMapper;
import com.product_service.adapter.out.persistence.category.repository.CategoryJpaRepository;
import com.product_service.domain.model.aggregate.Category;
import com.product_service.domain.model.vo.CategoryId;
import com.product_service.domain.model.vo.Slug;
import com.product_service.domain.repository.CategoryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.value())
                .map(CategoryPersistenceMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAllByOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(CategoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Category save(Category category) {
        return CategoryPersistenceMapper.toDomain(
                jpaRepository.save(CategoryPersistenceMapper.toEntity(category))
        );
    }

    @Override
    public boolean existsBySlug(Slug slug, CategoryId excludingId) {
        return excludingId == null
                ? jpaRepository.existsBySlug(slug.value())
                : jpaRepository.existsBySlugAndIdNot(slug.value(), excludingId.value());
    }

    @Override
    public boolean isDescendant(CategoryId candidate, CategoryId ancestor) {
        return !candidate.equals(ancestor)
                && jpaRepository.countAncestorMatches(candidate.value(), ancestor.value()) > 0;
    }
}
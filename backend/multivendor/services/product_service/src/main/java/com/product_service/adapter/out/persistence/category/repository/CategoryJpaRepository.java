package com.product_service.adapter.out.persistence.category.repository;

import com.product_service.adapter.out.persistence.category.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository
        extends JpaRepository<CategoryJpaEntity, UUID> {

    List<CategoryJpaEntity> findAllByOrderByDisplayOrderAscNameAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<CategoryJpaEntity> findByParentId(UUID id);
    @Query(
            value = """
                    WITH RECURSIVE ancestors AS (
                        SELECT parent_id
                        FROM categories
                        WHERE id = :candidateId
                    
                        UNION
                    
                        SELECT c.parent_id
                        FROM categories c
                        JOIN ancestors a ON c.id = a.parent_id
                    )
                    SELECT COUNT(*)
                    FROM ancestors
                    WHERE parent_id = :ancestorId
                    """,
            nativeQuery = true
    )
    long countAncestorMatches(
            @Param("candidateId") UUID candidateId,
            @Param("ancestorId") UUID ancestorId
    );
}
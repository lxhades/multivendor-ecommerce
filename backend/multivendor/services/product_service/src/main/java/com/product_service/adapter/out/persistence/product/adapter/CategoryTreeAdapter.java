package com.product_service.adapter.out.persistence.product.adapter;

import com.product_service.application.product.port.CategoryTreePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class CategoryTreeAdapter implements CategoryTreePort {

    private final JdbcTemplate jdbc;

    public CategoryTreeAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Lấy danh sách ID của danh mục hiện tại và TẤT CẢ các danh mục con
    @Override
    public Set<UUID> descendantsIncluding(UUID id) {
        // Sử dụng Recursive CTE (Common Table Expression) của PostgreSQL để duyệt cây
        String sql = """
                WITH RECURSIVE tree AS (
                    SELECT id FROM categories WHERE id = ?
                
                    UNION ALL
                
                    SELECT c.id 
                    FROM categories c 
                    JOIN tree t ON c.parent_id = t.id
                )
                SELECT DISTINCT id FROM tree
                """;

        List<UUID> ids = jdbc.queryForList(sql, UUID.class, id);
        return new LinkedHashSet<>(ids);
    }
}
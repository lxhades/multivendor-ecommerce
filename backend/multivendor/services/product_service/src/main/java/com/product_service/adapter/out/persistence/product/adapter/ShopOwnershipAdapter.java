package com.product_service.adapter.out.persistence.product.adapter;

import com.product_service.application.product.port.ShopOwnershipPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ShopOwnershipAdapter implements ShopOwnershipPort {

    private final JdbcTemplate jdbc;

    public ShopOwnershipAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean ownedBy(UUID shopId, UUID sellerId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 
                    FROM shops 
                    WHERE id = ? 
                      AND seller_id = ? 
                      AND status <> 'DELETED'
                )
                """;

        Boolean exists = jdbc.queryForObject(
                sql,
                Boolean.class,
                shopId,
                sellerId
        );

        return Boolean.TRUE.equals(exists);
    }
}
package com.product_service.adapter.out.persistence.product.adapter;

import com.product_service.application.product.port.SalesPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Repository
public class SalesRepositoryAdapter implements SalesPort {

    private final JdbcTemplate jdbc;

    public SalesRepositoryAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean claimDeliveredOrder(UUID orderId) {
        return jdbc.update("""
                INSERT INTO processed_delivered_orders (order_id)
                VALUES (?) ON CONFLICT (order_id) DO NOTHING
                """, orderId) == 1;
    }

    @Override
    public void incrementSoldCount(UUID shopId, UUID productId, UUID variantId, long quantity) {
        int products = jdbc.update("""
                UPDATE products SET sold_count = sold_count + ?
                WHERE id = ? AND shop_id = ?
                """, quantity, productId, shopId);
        if (products != 1) {
            throw new IllegalStateException("Không tìm thấy sản phẩm thuộc cửa hàng: " + productId);
        }

        int variants = jdbc.update("""
                UPDATE product_variants SET sold_count = sold_count + ?
                WHERE id = ? AND product_id = ?
                """, quantity, variantId, productId);
        if (variants != 1) {
            throw new IllegalStateException("Không tìm thấy biến thể thuộc sản phẩm: " + variantId);
        }
    }

}

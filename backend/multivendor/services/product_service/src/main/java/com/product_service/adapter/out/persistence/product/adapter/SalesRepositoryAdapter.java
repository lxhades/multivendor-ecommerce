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
    public Summary stats(UUID shop, UUID product, LocalDate from, LocalDate to) {
        String sql = """
                SELECT 
                    coalesce(sum(s.sold_quantity), 0), 
                    coalesce(sum(s.refunded_quantity), 0), 
                    coalesce(sum(s.gross_amount), 0), 
                    coalesce(sum(s.refunded_amount), 0) 
                FROM product_sales_daily s 
                JOIN products p ON p.id = s.product_id 
                WHERE p.shop_id = ? 
                  AND (?::uuid IS NULL OR s.product_id = ?) 
                  AND (?::date IS NULL OR s.sale_date >= ?) 
                  AND (?::date IS NULL OR s.sale_date < ?)
                """;

        return jdbc.query(sql, rs -> {
            rs.next();
            BigDecimal gross = rs.getBigDecimal(3);
            BigDecimal refund = rs.getBigDecimal(4);

            return new Summary(
                    rs.getLong(1),
                    rs.getLong(2),
                    gross,
                    refund,
                    gross.subtract(refund)
            );
        }, shop, product, product, from, from, to, to);
    }

    @Override
    @Transactional
    public boolean apply(
            UUID eventId,
            String type,
            UUID orderItemId,
            UUID productId,
            UUID variantId,
            long quantity,
            BigDecimal amount,
            Instant at) {

        if (at == null) {
            throw new IllegalArgumentException("occurredAt không được để trống");
        }

        String insertEventSql = """
                INSERT INTO processed_product_sales_events (event_id, event_type, order_item_id, product_id, variant_id, occurred_at) 
                VALUES (?, ?, ?, ?, ?, ?) 
                ON CONFLICT (event_id) DO NOTHING
                """;

        int inserted = jdbc.update(
                insertEventSql,
                eventId, type, orderItemId, productId, variantId, java.sql.Timestamp.from(at)
        );

        if (inserted == 0) {
            return false;
        }

        String checkVariantSql = "SELECT count(*) FROM product_variants WHERE id = ? AND product_id = ?";
        Integer matches = jdbc.queryForObject(checkVariantSql, Integer.class, variantId, productId);

        if (matches == null || matches != 1) {
            throw new IllegalArgumentException("Variant không thuộc về Product này");
        }

        if (type.equals("SALE_REFUNDED")) {
            String sumSoldSql = """
                    SELECT coalesce(sum(CASE WHEN event_type = 'SALE_RECORDED' THEN quantity ELSE -quantity END), 0) 
                    FROM product_sale_event_quantities 
                    WHERE order_item_id = ? AND variant_id = ?
                    """;
            Long sold = jdbc.queryForObject(sumSoldSql, Long.class, orderItemId, variantId);

            if (sold == null || sold < quantity) {
                throw new IllegalArgumentException("Số lượng hoàn trả vượt quá số lượng đã bán");
            }

            String sumAmountSql = """
                    SELECT coalesce(sum(CASE WHEN event_type = 'SALE_RECORDED' THEN amount ELSE -amount END), 0) 
                    FROM product_sale_event_quantities 
                    WHERE order_item_id = ? AND variant_id = ?
                    """;
            BigDecimal available = jdbc.queryForObject(sumAmountSql, BigDecimal.class, orderItemId, variantId);

            if (available == null || available.compareTo(amount) < 0) {
                throw new IllegalArgumentException("Số tiền hoàn trả vượt quá số tiền đã thanh toán");
            }
        }

        String insertQuantitySql = """
                INSERT INTO product_sale_event_quantities (event_id, order_item_id, variant_id, event_type, quantity, amount) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(insertQuantitySql, eventId, orderItemId, variantId, type, quantity, amount);

        LocalDate day = at.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();
        boolean sale = type.equals("SALE_RECORDED");

        String upsertDailySql = """
                INSERT INTO product_sales_daily (product_id, variant_id, sale_date, sold_quantity, refunded_quantity, gross_amount, refunded_amount) 
                VALUES (?, ?, ?, ?, ?, ?, ?) 
                ON CONFLICT (product_id, variant_id, sale_date) DO UPDATE SET 
                    sold_quantity = product_sales_daily.sold_quantity + excluded.sold_quantity,
                    refunded_quantity = product_sales_daily.refunded_quantity + excluded.refunded_quantity,
                    gross_amount = product_sales_daily.gross_amount + excluded.gross_amount,
                    refunded_amount = product_sales_daily.refunded_amount + excluded.refunded_amount
                """;

        jdbc.update(
                upsertDailySql,
                productId,
                variantId,
                day,
                sale ? quantity : 0,
                sale ? 0 : quantity,
                sale ? amount : BigDecimal.ZERO,
                sale ? BigDecimal.ZERO : amount
        );

        if (sale) {
            String updateVariantSql = "UPDATE product_variants SET sold_count = sold_count + ? WHERE id = ?";
            jdbc.update(updateVariantSql, quantity, variantId);

            String updateProductSql = "UPDATE products SET sold_count = sold_count + ? WHERE id = ?";
            jdbc.update(updateProductSql, quantity, productId);
        }

        return true;
    }
}
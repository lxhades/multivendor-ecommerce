package com.order_service.adapter.out.persistence.report;

import com.order_service.application.report.dto.SellerSalesReport.SoldVariant;
import com.order_service.application.report.model.ReportGroupBy;
import com.order_service.application.report.model.ReportRange;
import com.order_service.application.report.port.SellerReportQueryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/** PostgreSQL aggregation. Does not load order aggregates or call other services. */
@Repository
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class SellerReportJdbcAdapter implements SellerReportQueryPort {
    private final NamedParameterJdbcTemplate jdbc;
    public SellerReportJdbcAdapter(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<DemandBucket> demand(UUID shopId, ReportRange range, ReportGroupBy groupBy) {
        String sql = """
                WITH selected_orders AS (
                    SELECT o.id, o.order_status,
                           CAST(date_trunc(:bucket, o.created_at AT TIME ZONE :timezone) AS date) AS period_start
                    FROM orders o
                    WHERE o.shop_id = :shopId AND o.created_at >= :start AND o.created_at < :until
                ), quantities AS (
                    SELECT i.order_id, SUM(i.quantity::bigint) AS quantity
                    FROM order_items i JOIN selected_orders o ON o.id = i.order_id
                    GROUP BY i.order_id
                )
                SELECT o.period_start, COUNT(*) AS total_orders,
                       COUNT(*) FILTER (WHERE o.order_status = 'CANCELLED') AS cancelled_orders,
                       COALESCE(SUM(q.quantity), 0) AS total_quantity,
                       COALESCE(SUM(q.quantity) FILTER (WHERE o.order_status = 'CANCELLED'), 0) AS cancelled_quantity
                FROM selected_orders o LEFT JOIN quantities q ON q.order_id = o.id
                GROUP BY o.period_start ORDER BY o.period_start
                """;
        return jdbc.query(sql, parameters(shopId, range, groupBy), (rs, row) -> new DemandBucket(
                rs.getDate("period_start").toLocalDate(), rs.getLong("total_orders"), rs.getLong("cancelled_orders"),
                rs.getLong("total_quantity"), rs.getLong("cancelled_quantity")));
    }

    @Override
    public SalesData sales(UUID shopId, ReportRange range, ReportGroupBy groupBy, int variantPage, int variantSize) {
        var params = parameters(shopId, range, groupBy)
                .addValue("limit", variantSize).addValue("offset", (long) variantPage * variantSize);
        // Aggregate item quantities per order before joining: never multiply order revenue by line count.
        String periodsSql = """
                WITH selected_orders AS (
                    SELECT o.id, o.subtotal, o.shipping_fee, o.total_amount,
                           CAST(date_trunc(:bucket, o.updated_at AT TIME ZONE :timezone) AS date) AS period_start
                    FROM orders o
                    WHERE o.shop_id = :shopId AND o.order_status = 'DELIVERED'
                      AND o.updated_at >= :start AND o.updated_at < :until
                ), quantities AS (
                    SELECT i.order_id, SUM(i.quantity::bigint) AS quantity
                    FROM order_items i JOIN selected_orders o ON o.id = i.order_id
                    GROUP BY i.order_id
                )
                SELECT o.period_start, COUNT(*) AS delivered_orders,
                       COALESCE(SUM(q.quantity), 0) AS sold_quantity,
                       COALESCE(SUM(o.subtotal::numeric), 0) AS revenue,
                       COALESCE(SUM(o.shipping_fee::numeric), 0) AS shipping_fee,
                       COALESCE(SUM(o.total_amount::numeric), 0) AS total_order_amount
                FROM selected_orders o LEFT JOIN quantities q ON q.order_id = o.id
                GROUP BY o.period_start ORDER BY o.period_start
                """;
        List<SalesBucket> buckets = jdbc.query(periodsSql, params, (rs, row) -> new SalesBucket(
                rs.getDate("period_start").toLocalDate(), rs.getLong("delivered_orders"), rs.getLong("sold_quantity"),
                rs.getBigDecimal("revenue"), rs.getBigDecimal("shipping_fee"), rs.getBigDecimal("total_order_amount")));

        String filteredItems = """
                FROM order_items i JOIN orders o ON o.id = i.order_id
                WHERE o.shop_id = :shopId AND o.order_status = 'DELIVERED'
                  AND o.updated_at >= :start AND o.updated_at < :until
                """;
        // Names come from the latest matching purchase snapshot, not the current catalog.
        String variantsSql = """
                SELECT i.product_id, i.variant_id,
                       (array_agg(i.product_name ORDER BY o.updated_at DESC, o.id DESC, i.id DESC))[1] AS product_name,
                       (array_agg(i.variant_name ORDER BY o.updated_at DESC, o.id DESC, i.id DESC))[1] AS variant_name,
                       SUM(i.quantity::bigint) AS sold_quantity,
                       SUM(i.unit_price::numeric * i.quantity::numeric) AS revenue
                """ + filteredItems + """
                GROUP BY i.product_id, i.variant_id
                ORDER BY sold_quantity DESC, revenue DESC, i.product_id, i.variant_id
                LIMIT :limit OFFSET :offset
                """;
        List<SoldVariant> variants = jdbc.query(variantsSql, params, (rs, row) -> new SoldVariant(
                rs.getObject("product_id", UUID.class), rs.getString("product_name"),
                rs.getObject("variant_id", UUID.class), rs.getString("variant_name"),
                rs.getLong("sold_quantity"), rs.getBigDecimal("revenue")));
        Long totalVariants = jdbc.queryForObject("SELECT COUNT(*) FROM (SELECT i.product_id, i.variant_id "
                + filteredItems + " GROUP BY i.product_id, i.variant_id) variant_groups", params, Long.class);
        return new SalesData(buckets, variants, totalVariants == null ? 0 : totalVariants);
    }

    private MapSqlParameterSource parameters(UUID shopId, ReportRange range, ReportGroupBy groupBy) {
        return new MapSqlParameterSource().addValue("shopId", shopId).addValue("bucket", groupBy.sqlUnit())
                .addValue("timezone", ReportRange.ZONE.getId())
                .addValue("start", Timestamp.from(range.startInclusive()))
                .addValue("until", Timestamp.from(range.endExclusive()));
    }
}

package com.order_service.adapter.out.persistence.report;

import com.order_service.application.admin.AdminOrderQueryPort;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.application.report.model.ReportRange;
import com.order_service.domain.model.enums.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;
import java.sql.*;
import java.util.*;

@Repository
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class AdminOrderJdbcAdapter implements AdminOrderQueryPort {
    private final NamedParameterJdbcTemplate jdbc;
    public AdminOrderJdbcAdapter(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public OrderPage orders(Filter f) {
        var params = new MapSqlParameterSource().addValue("limit", f.size()).addValue("offset", (long) f.page() * f.size());
        var where = new StringBuilder(" WHERE 1=1");
        add(where, params, "o.id", "id", f.orderId());
        add(where, params, "o.checkout_id", "checkout", f.checkoutId());
        add(where, params, "o.shop_id", "shop", f.shopId());
        add(where, params, "o.buyer_id", "buyer", f.buyerId());
        add(where, params, "o.order_status", "status", f.status() == null ? null : f.status().name());
        add(where, params, "o.payment_status", "payment", f.paymentStatus() == null ? null : f.paymentStatus().name());
        add(where, params, "o.payment_method", "method", f.paymentMethod() == null ? null : f.paymentMethod().name());
        String date = f.dateBasis() == DateBasis.UPDATED_AT ? "o.updated_at" : "o.created_at";
        if (f.from() != null) { where.append(" AND ").append(date).append(" >= :start"); params.addValue("start", Timestamp.from(f.from().atStartOfDay(ReportRange.ZONE).toInstant())); }
        if (f.to() != null) { where.append(" AND ").append(date).append(" < :until"); params.addValue("until", Timestamp.from(f.to().plusDays(1).atStartOfDay(ReportRange.ZONE).toInstant())); }
        if (!f.q().isEmpty()) {
            // Escape LIKE metacharacters with !; every user value stays a bound parameter.
            params.addValue("q", "%" + f.q().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
            where.append("""
                     AND (CAST(o.id AS text) ILIKE :q ESCAPE '!'
                       OR CAST(o.checkout_id AS text) ILIKE :q ESCAPE '!'
                       OR CAST(o.shop_id AS text) ILIKE :q ESCAPE '!'
                       OR CAST(o.buyer_id AS text) ILIKE :q ESCAPE '!'
                       OR o.shipping_address ILIKE :q ESCAPE '!'
                       OR EXISTS (SELECT 1 FROM order_items qi WHERE qi.order_id=o.id
                         AND (qi.product_name ILIKE :q ESCAPE '!' OR qi.variant_name ILIKE :q ESCAPE '!')))
                    """);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM orders o" + where, params, Long.class);
        String direction = f.direction() == Direction.ASC ? "ASC" : "DESC";
        // Stable sort and page orders first. Never paginate a join of order lines.
        var headers = jdbc.query("SELECT o.* FROM orders o" + where + " ORDER BY " + date + " " + direction + ", o.id " + direction + " LIMIT :limit OFFSET :offset", params,
                (rs, n) -> mapOrder(rs, List.of()));
        Map<UUID, List<OrderResult.Item>> items = new HashMap<>();
        if (!headers.isEmpty()) {
            var lineParams = new MapSqlParameterSource("ids", headers.stream().map(OrderResult::id).toList());
            jdbc.query("SELECT * FROM order_items WHERE order_id IN (:ids) ORDER BY order_id, id", lineParams, (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
                UUID id = rs.getObject("order_id", UUID.class);
                items.computeIfAbsent(id, key -> new ArrayList<>()).add(new OrderResult.Item(
                    rs.getObject("id", UUID.class), rs.getObject("product_id", UUID.class), rs.getObject("variant_id", UUID.class),
                    rs.getString("product_name"), rs.getString("variant_name"), rs.getString("variant_image_url"), rs.getLong("unit_price"), rs.getInt("quantity")));
            });
        }
        var results = headers.stream().map(o -> new OrderResult(o.id(), o.checkoutId(), o.buyerId(), o.shopId(), o.shippingAddress(),
                items.getOrDefault(o.id(), List.of()), o.subtotal(), o.shippingFee(), o.totalAmount(), o.orderStatus(), o.paymentStatus(), o.paymentMethod(), o.createdAt(), o.updatedAt())).toList();
        long count = total == null ? 0 : total;
        return new OrderPage(results, f.page(), f.size(), count, pages(count, f.size()));
    }

    private static final String AMOUNTS = """
            COUNT(*) AS delivered_orders,
            COUNT(*) FILTER (WHERE payment_status='PAID') AS paid_orders,
            COALESCE(SUM(subtotal::numeric),0) AS revenue,
            COALESCE(SUM(shipping_fee::numeric),0) AS shipping_fee,
            COALESCE(SUM(total_amount::numeric),0) AS total_amount,
            COALESCE(SUM(total_amount::numeric) FILTER (WHERE payment_status='PAID'),0) AS paid_amount,
            COALESCE(SUM(total_amount::numeric) FILTER (WHERE payment_status<>'PAID'),0) AS unpaid_amount
            """;
    @Override
    public RevenueReport revenue(ReportRange range, UUID shopId, int page, int size) {
        var params = new MapSqlParameterSource().addValue("start", Timestamp.from(range.startInclusive()))
                .addValue("until", Timestamp.from(range.endExclusive())).addValue("limit", size).addValue("offset", (long) page * size);
        String where = " FROM orders WHERE order_status='DELIVERED' AND updated_at >= :start AND updated_at < :until";
        if (shopId != null) { where += " AND shop_id=:shopId"; params.addValue("shopId", shopId); }
        Amounts summary = jdbc.queryForObject("SELECT " + AMOUNTS + where, params, (rs, n) -> amounts(rs));
        Long total = jdbc.queryForObject("SELECT COUNT(DISTINCT shop_id)" + where, params, Long.class);
        var shops = jdbc.query("SELECT shop_id, " + AMOUNTS + where + " GROUP BY shop_id ORDER BY revenue DESC, shop_id LIMIT :limit OFFSET :offset",
                params, (rs, n) -> new ShopRevenue(rs.getObject("shop_id", UUID.class), amounts(rs)));
        long count = total == null ? 0 : total;
        return new RevenueReport(range.from(), range.to(), ReportRange.ZONE.getId(), "updatedAt", summary, shops, page, size, count, pages(count, size));
    }
    private static long pages(long count, int size) { return count / size + (count % size == 0 ? 0 : 1); }
    private static void add(StringBuilder where, MapSqlParameterSource params, String column, String key, Object value) {
        if (value != null) { where.append(" AND ").append(column).append(" = :").append(key); params.addValue(key, value); }
    }
    private static Amounts amounts(ResultSet rs) throws SQLException {
        return new Amounts(rs.getLong("delivered_orders"), rs.getLong("paid_orders"), rs.getBigDecimal("revenue"), rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("total_amount"), rs.getBigDecimal("paid_amount"), rs.getBigDecimal("unpaid_amount"));
    }
    private static OrderResult mapOrder(ResultSet rs, List<OrderResult.Item> items) throws SQLException {
        return new OrderResult(rs.getObject("id", UUID.class), rs.getObject("checkout_id", UUID.class), rs.getObject("buyer_id", UUID.class), rs.getObject("shop_id", UUID.class),
                rs.getString("shipping_address"), items, rs.getLong("subtotal"), rs.getLong("shipping_fee"), rs.getLong("total_amount"), OrderStatus.valueOf(rs.getString("order_status")),
                PaymentStatus.valueOf(rs.getString("payment_status")), PaymentMethod.valueOf(rs.getString("payment_method")), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant());
    }
}

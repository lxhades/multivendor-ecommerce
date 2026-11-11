package com.product_service.adapter.out.persistence.product.adapter;

import com.product_service.application.product.dto.BuyerSearch;
import com.product_service.application.product.port.BuyerSearchPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;


@Repository
public class BuyerSearchAdapter implements BuyerSearchPort {

    // 1. ĐIỀU KIỆN CƠ BẢN: Chỉ lấy sản phẩm đang hoạt động, đã duyệt, chưa xóa
    // và thuộc về cửa hàng đang hoạt động.
    private static final String BASE_QUERY = """
            FROM public.products p
            JOIN public.shops s ON s.id = p.shop_id AND s.status = 'ACTIVE'
            WHERE p.deleted_at IS NULL 
              AND p.status = 'ACTIVE' 
              AND p.approval_status = 'APPROVED'
            """;

    // 2. ĐIỀU KIỆN TÌM KIẾM TỪ KHÓA (sử dụng ilike)
    // Logic: Tìm theo Tên sản phẩm (chứa từ khóa) HOẶC dùng Full-text search HOẶC khớp SKU.
    private static final String KEYWORD_CONDITION = """
            AND p.id IN (
                SELECT indexed.id FROM public.products indexed
                WHERE indexed.deleted_at IS NULL 
                  AND indexed.status = 'ACTIVE' 
                  AND indexed.approval_status = 'APPROVED'
                  AND (
                 
                      indexed.name ILIKE '%' || :keyword || '%' 
                     
                      OR indexed.search_document @@ plainto_tsquery('public.catalog_search', :keyword)
                  )
                UNION
               
                SELECT v.product_id FROM public.product_variants v
                WHERE v.active AND v.deleted_at IS NULL 
                  AND v.sku ILIKE '%' || :keyword || '%'
            )
            """;

    // 3. ĐIỀU KIỆN PHỤ TRỢ: Kiểm tra xem sản phẩm có biến thể khớp CHÍNH XÁC mã SKU không.
    // Thường dùng để ưu tiên sắp xếp (Sort Relevance).
    private static final String EXACT_SKU_CONDITION = """
            EXISTS (SELECT 1 FROM public.product_variants v
                    WHERE v.product_id = p.id AND v.active AND v.deleted_at IS NULL
                      AND lower(v.sku) = lower(:keyword))
            """;

    // 4. HÀM TÌM KIẾM FULL-TEXT CƠ BẢN (Dùng cho việc đánh giá độ liên quan)
    private static final String FTS_TERMS = "plainto_tsquery('public.catalog_search', :keyword)";

    private final NamedParameterJdbcTemplate jdbc;

    public BuyerSearchAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public BuyerSearch.Page search(BuyerSearch.Criteria criteria, Collection<UUID> categoryIds) {
        // Bước 1: Khởi tạo các tham số phân trang (LIMIT và OFFSET)
        var parameters = new MapSqlParameterSource("size", criteria.size())
                .addValue("offset", (criteria.page() - 1) * criteria.size());

        // Bước 2: Xây dựng câu lệnh WHERE động (Dynamic WHERE clause)
        var sqlBuilder = new StringBuilder(BASE_QUERY);

        boolean hasKeyword = criteria.keyword() != null && !criteria.keyword().isBlank();

        // 2.1. Thêm điều kiện tìm kiếm từ khóa (nếu có)
        if (hasKeyword) {
            sqlBuilder.append(KEYWORD_CONDITION);
            parameters.addValue("keyword", criteria.keyword());
        }

        // 2.2. Thêm bộ lọc theo Cửa hàng (Shop)
        if (criteria.shopId() != null) {
            sqlBuilder.append(" AND p.shop_id = :shop");
            parameters.addValue("shop", criteria.shopId());
        }

        // 2.3. Thêm bộ lọc theo Danh mục (Category)
        if (categoryIds != null && !categoryIds.isEmpty()) {
            sqlBuilder.append(" AND p.category_id IN (:categories)");
            parameters.addValue("categories", categoryIds);
        }

        // 2.4. Thêm bộ lọc theo Khoảng giá (Min/Max)
        if (criteria.minPrice() != null) {
            sqlBuilder.append(" AND p.min_price >= :minPrice");
            parameters.addValue("minPrice", criteria.minPrice());
        }
        if (criteria.maxPrice() != null) {
            sqlBuilder.append(" AND p.min_price <= :maxPrice");
            parameters.addValue("maxPrice", criteria.maxPrice());
        }

        // Bước 3: Đếm tổng số lượng kết quả (để tính số trang)
        String countSql = "SELECT count(*) " + sqlBuilder;
        Long total = jdbc.queryForObject(countSql, parameters, Long.class);
        long safeTotal = (total != null) ? total : 0L;

        // Bước 4: Xác định câu lệnh sắp xếp (Sort)
        String sortSql = buildSortExpression(criteria.sort(), hasKeyword);

        // Bước 5: Truy vấn lấy dữ liệu của trang hiện tại
        String dataSql = """
                SELECT p.id, p.shop_id, p.category_id, p.name, p.media_json, p.min_price,
                       s.name AS shop_name,
                       (SELECT c.name FROM public.categories c WHERE c.id = p.category_id) AS category_name
                """ + sqlBuilder + " ORDER BY " + sortSql + " LIMIT :size OFFSET :offset";

        var items = jdbc.query(dataSql, parameters,
                (rs, rowNum) -> new BuyerSearch.Item(
                        rs.getObject("id", UUID.class),
                        rs.getObject("shop_id", UUID.class),
                        rs.getObject("category_id", UUID.class),
                        rs.getString("name"),
                        rs.getString("media_json"),
                        rs.getBigDecimal("min_price"),
                        rs.getString("shop_name"),
                        rs.getString("category_name")
                ));

        // Bước 6: Tính toán tổng số trang và trả về kết quả
        long pages = safeTotal / criteria.size() + (safeTotal % criteria.size() == 0 ? 0 : 1);
        return new BuyerSearch.Page(items, criteria.page(), criteria.size(), safeTotal, pages);
    }


    private static String buildSortExpression(BuyerSearch.Sort sort, boolean hasKeyword) {
        // Điều kiện phụ (Tie-breaker): Nếu các tiêu chí chính bằng nhau, sắp xếp theo ngày tạo mới nhất
        String tieBreak = "p.created_at DESC NULLS LAST, p.id";

        return switch (sort) {
            case NEWEST -> tieBreak;

            case PRICE_ASC -> "p.min_price ASC NULLS LAST, " + tieBreak;

            case PRICE_DESC -> "p.min_price DESC NULLS LAST, " + tieBreak;

            case RELEVANCE -> {
                if (!hasKeyword) {
                    // Nếu không có từ khóa, không thể sắp xếp theo độ liên quan -> Dùng mặc định
                    yield tieBreak;
                }
                // Nếu có từ khóa, sắp xếp theo 3 cấp độ ưu tiên:
                // Cấp 1: Khớp chính xác mã SKU (Ưu tiên cao nhất - Giá trị 0)
                // Cấp 2: Tên sản phẩm khớp chính xác cụm từ (Giá trị 1)
                // Cấp 3: Sắp xếp theo điểm số Full-Text Search giảm dần (Giá trị 2)
                yield "CASE " +
                        "WHEN " + EXACT_SKU_CONDITION + " THEN 0 " +
                        "WHEN to_tsvector('public.catalog_search', p.name) @@ phraseto_tsquery('public.catalog_search', :keyword) THEN 1 " +
                        "ELSE 2 END, " +
                        "ts_rank_cd(p.search_document, " + FTS_TERMS + ") DESC, " +
                        tieBreak;
            }
        };
    }
}
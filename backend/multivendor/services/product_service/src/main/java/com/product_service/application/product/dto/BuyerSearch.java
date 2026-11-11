package com.product_service.application.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class BuyerSearch {
    private BuyerSearch() {}

    public enum Sort { RELEVANCE, NEWEST, PRICE_ASC, PRICE_DESC }

    public record Criteria(String keyword, UUID shopId, UUID categoryId, boolean includeChildren,
                           Long minPrice, Long maxPrice, Sort sort, int page, int size) {
        public Criteria {
            keyword = keyword == null ? "" : keyword.strip();
            if (keyword.length() > 120 || page < 1 || page > 200 || size < 1 || size > 48) {
                throw new IllegalArgumentException("Invalid keyword length, page or page size");
            }
            if ((minPrice != null && minPrice < 0) || (maxPrice != null && maxPrice < 0)
                    || (minPrice != null && maxPrice != null && minPrice > maxPrice)) {
                throw new IllegalArgumentException("Invalid price range");
            }
            if (includeChildren && categoryId == null) {
                throw new IllegalArgumentException("includeChildren requires categoryId");
            }
            if (sort == null) sort = keyword.isBlank() ? Sort.NEWEST : Sort.RELEVANCE;
        }
    }

    public record Item(UUID id, UUID shopId, UUID categoryId, String name, String mediaJson, BigDecimal minPrice,
                       String shopName, String categoryName) {}
    public record Page(List<Item> items, int page, int size, long totalElements, long totalPages) {}
}

package com.product_service.application.product.usecase;

import com.product_service.application.product.dto.BuyerSearch;
import com.product_service.application.product.port.BuyerSearchPort;
import com.product_service.application.product.port.CategoryTreePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class BuyerSearchUseCase {
    private final BuyerSearchPort search;
    private final CategoryTreePort categories;

    public BuyerSearchUseCase(BuyerSearchPort search, CategoryTreePort categories) {
        this.search = search;
        this.categories = categories;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public BuyerSearch.Page execute(BuyerSearch.Criteria criteria) {
        Collection<UUID> categoryIds = List.of();
        if (criteria.categoryId() != null) {
            categoryIds = criteria.includeChildren()
                    ? categories.descendantsIncluding(criteria.categoryId())
                    : List.of(criteria.categoryId());
            if (categoryIds.isEmpty()) {
                return new BuyerSearch.Page(List.of(), criteria.page(), criteria.size(), 0, 0);
            }
        }
        return search.search(criteria, categoryIds);
    }
}

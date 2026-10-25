package com.product_service.application.product.usecase;

import com.product_service.application.product.dto.ProductDtos.Buyer;
import com.product_service.application.product.port.CategoryTreePort;
import com.product_service.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BuyerProductUseCase {
    private final ProductRepository repo;
    private final CategoryTreePort categories;

    public BuyerProductUseCase(ProductRepository repo, CategoryTreePort categories) {
        this.repo = repo;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public Buyer details(UUID id) {
        return Buyer.of(
                repo.findVisibleById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")),
                true);
    }

    @Transactional(readOnly = true)
    public List<Buyer> list(UUID shopId, UUID categoryId, boolean includeChildren) {
        Collection<UUID> categoryIds;

        if (categoryId == null) {
            categoryIds = null;
        } else if (includeChildren) {
            categoryIds = categories.descendantsIncluding(categoryId);
        } else {
            categoryIds = Set.of(categoryId);
        }
        return repo.findVisible(shopId, null, categoryIds)
                .stream()
                .map(p -> Buyer.of(p, false))
                .toList();
    }
}

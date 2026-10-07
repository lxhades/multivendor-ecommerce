package com.catalog_service.domain.model.aggregate;

import com.catalog_service.domain.model.vo.Money;
import com.catalog_service.domain.model.vo.ProductName;
import com.catalog_service.domain.model.vo.SellerId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    @Test
    void createProductCreatesDraftProduct() {
        Product product = Product.create(
                SellerId.of(UUID.randomUUID()),
                new ProductName("Laptop"),
                "Test product",
                Money.of(new BigDecimal("100.00"))
        );

        assertNotNull(product.getProductId());
        assertEquals("Laptop", product.getName().value());
        assertEquals("DRAFT", product.getStatus().name());
    }
}

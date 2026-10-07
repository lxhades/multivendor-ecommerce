package com.catalog_service.domain.model.aggregate;
import com.catalog_service.domain.model.enumtype.CategoryStatus;
import com.catalog_service.domain.model.vo.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class CategoryTest {
 @Test void createRootCategory_shouldCreateActiveCategory(){var c=Category.create(null,new CategoryName("Điện thoại"),"dien-thoai");assertNotNull(c.getCategoryId());assertNull(c.getParentId());assertEquals("Điện thoại",c.getName().value());assertEquals(CategoryStatus.ACTIVE,c.getStatus());}
 @Test void createChildCategory_shouldHaveParent(){var parent=CategoryId.of(UUID.randomUUID());var c=Category.create(parent,new CategoryName("iPhone"),"iphone");assertEquals(parent,c.getParentId());}
}

package com.catalog_service.application.usecase;

import com.catalog_service.application.command.CreateProductCommand;
import com.catalog_service.application.port.out.ProductRepository;
import com.catalog_service.domain.model.aggregate.Product;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateProductUseCaseTest {
    @Test
    void createProductSavesProduct() {
        ProductRepository repository = mock(ProductRepository.class);
        when(repository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        CreateProductUseCase useCase = new CreateProductUseCase(repository);

        var result = useCase.execute(new CreateProductCommand(
                UUID.randomUUID().toString(),
                "Phone",
                "Phone description",
                "999.99"
        ));

        assertNotNull(result.productId());
        assertEquals("Phone", result.name());
        verify(repository).save(any(Product.class));
    }
}


package com.product_service.adapter.in.web.product;

import com.product_service.application.product.usecase.VerifyVariantsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/products/variants")
public class InternalVariantController {

    private final VerifyVariantsUseCase useCase;

    public InternalVariantController(VerifyVariantsUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/verify")
    public List<VerifyVariantsUseCase.VerifiedVariant> verify(
            @Valid @RequestBody VerifyRequest request
    ) {
        return useCase.execute(request.variantIds());
    }

    public record VerifyRequest(
            @NotEmpty
            @Size(max = 100)
            List<@NotNull UUID> variantIds
    ) {}
}

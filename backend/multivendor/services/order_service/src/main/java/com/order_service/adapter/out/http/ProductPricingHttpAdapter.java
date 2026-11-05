
package com.order_service.adapter.out.http;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.application.checkout.port.ProductPricingPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Component
public class ProductPricingHttpAdapter implements ProductPricingPort {

    private final RestClient client;

    public ProductPricingHttpAdapter(
            RestClient.Builder builder,
            @Value("${services.product.base-url}") String baseUrl,
            @Value("${services.product.internal-token}") String token
    ) {
        this.client = builder
                .baseUrl(baseUrl)
//                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Override
    public List<VerifiedVariant> verifyVariants(List<UUID> variantIds) {

        if (variantIds == null || variantIds.isEmpty()) {
            throw new IllegalArgumentException("Empty variants");
        }

        try {
            VerifiedVariant[] response = client.post()
                    .uri("/internal/products/variants/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new VerifyRequest(variantIds))
                    .retrieve()
                    .body(VerifiedVariant[].class);

            if (response == null) {
                throw new IllegalStateException(
                        "Empty response from Product Service"
                );
            }

            return List.of(response);

        } catch (RestClientException ex) {
            throw new IllegalArgumentException(
                    " ProductServiceUnavailableException"
            );
        }
    }

    private record VerifyRequest(List<UUID> variantIds) {}

}

package com.order_service.adapter.out.http;

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
            throw new IllegalArgumentException("Danh sách biến thể không được để trống");
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
                        "Phản hồi từ dịch vụ sản phẩm rỗng"
                );
            }

            return List.of(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Không thể xác minh biến thể từ dịch vụ sản phẩm",
                    ex
            );
        }
    }

    private record VerifyRequest(List<UUID> variantIds) {
    }

}

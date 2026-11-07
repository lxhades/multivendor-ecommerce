package com.order_service.adapter.out.http;



import com.order_service.application.checkout.port.ShopOwnershipPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class ShopOwnershipHttpAdapter implements ShopOwnershipPort {

    private final RestClient client;

    public ShopOwnershipHttpAdapter(
            RestClient.Builder builder,
            @Value("${services.product.base-url}") String baseUrl
    ) {
        this.client = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public boolean isOwner(UUID sellerId, UUID shopId) {

        if (sellerId == null || shopId == null) {
            throw new IllegalArgumentException("Thiếu mã người bán hoặc mã cửa hàng");
        }

        ShopApiResponse response = client.get()
                .uri("/shops/{shopId}", shopId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, result) -> {
                            throw new IllegalStateException(
                                    "Không tìm thấy cửa hàng: " + shopId
                            );
                        }
                )
                .body(ShopApiResponse.class);

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Phản hồi thông tin cửa hàng rỗng");
        }

        return sellerId.equals(response.data().sellerId());
    }

    private record ShopApiResponse(ShopData data) {}

    private record ShopData(
            UUID id,
            UUID sellerId
    ) {}
}
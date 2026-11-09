package com.inventory_service.adapter.out.inventory.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class ShopOwnershipClient {
    private final RestClient client;

    public ShopOwnershipClient(@Value("${inventory.product-base-url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public boolean isOwner(UUID sellerId, UUID shopId) {
        try {
            var response = client.get().uri("/shops/{shopId}", shopId)
                    .retrieve().body(ShopResponse.class);
            if (response == null || response.data() == null || response.data().sellerId() == null) {
                throw unavailable(null);
            }
            return sellerId.equals(response.data().sellerId());
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (RestClientException ex) {
            throw unavailable(ex);
        }
    }

    private ResponseStatusException unavailable(Throwable cause) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Không thể xác minh quyền sở hữu cửa hàng", cause);
    }

    private record ShopResponse(ShopData data) {}

    private record ShopData(UUID sellerId) {}
}

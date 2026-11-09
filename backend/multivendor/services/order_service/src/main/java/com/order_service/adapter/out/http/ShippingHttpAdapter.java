package com.order_service.adapter.out.http;

import com.order_service.application.shipping.dto.ShipmentResult;
import com.order_service.application.shipping.port.ShippingPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.UUID;

@Component
public class ShippingHttpAdapter implements ShippingPort {

    private final RestClient client;

    public ShippingHttpAdapter(
            RestClient.Builder builder,
            @Value("${services.shipping.base-url}") String baseUrl
    ) {
        this.client = builder
                .clone()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt) {
                        request.getHeaders().setBearerAuth(jwt.getToken().getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    @Override
    public ShipmentResult createShipment(
            UUID orderId,
            String shippingAddress
    ) {
        Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");

        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException(
                    "Địa chỉ giao hàng không được để trống"
            );
        }

        try {
            ShipmentResult response = client.post()
                    .uri("/api/shipments/admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateShipmentRequest(
                            orderId,
                            shippingAddress
                    ))
                    .retrieve()
                    .body(ShipmentResult.class);

            return requireResponse(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Không thể tạo vận đơn cho đơn hàng: " + orderId,
                    ex
            );
        }
    }

    @Override
    public ShipmentResult getShipment(String trackingCode) {

        if (trackingCode == null || trackingCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Mã vận đơn không được để trống"
            );
        }

        try {
            ShipmentResult response = client.get()
                    .uri("/api/shipments/admin/{trackingCode}", trackingCode)
                    .retrieve()
                    .body(ShipmentResult.class);

            return requireResponse(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Không thể lấy thông tin vận đơn: " + trackingCode,
                    ex
            );
        }
    }

    @Override
    public ShipmentResult getShipmentByOrderId(UUID orderId) {

        Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");

        try {
            ShipmentResult response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/shipments/admin")
                            .queryParam("orderId", orderId)
                            .build())
                    .retrieve()
                    .body(ShipmentResult.class);

            return requireResponse(response);

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Không thể lấy thông tin vận chuyển cho đơn hàng: " + orderId,
                    ex
            );
        }
    }

    private ShipmentResult requireResponse(ShipmentResult response) {
        if (response == null) {
            throw new IllegalStateException(
                    "Phản hồi từ dịch vụ vận chuyển rỗng"
            );
        }
        return response;
    }

    private record CreateShipmentRequest(
            UUID orderId,
            String shippingAddress
    ) {}
}

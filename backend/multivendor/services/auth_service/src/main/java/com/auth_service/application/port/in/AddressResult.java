package com.auth_service.application.port.in;

public record AddressResult(
        Long addressId,
        String receiverName,
        String phone,
        String location,
        String detail,
        boolean isDefault
) {
}

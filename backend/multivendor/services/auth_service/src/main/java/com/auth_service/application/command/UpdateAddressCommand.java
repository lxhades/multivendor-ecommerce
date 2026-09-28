package com.auth_service.application.command;

public record UpdateAddressCommand(
        String userId,
        String addressId,
        String receiverName,
        String phone,
        String location,
        String detail,
        boolean isDefault
) {
}

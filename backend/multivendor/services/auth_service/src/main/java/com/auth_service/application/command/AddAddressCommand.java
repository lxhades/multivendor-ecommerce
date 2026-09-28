package com.auth_service.application.command;

public record AddAddressCommand(
        String userId,
        String receiverName,
        String phone,
        String location,
        String detail,
        boolean isDefault
) {
}

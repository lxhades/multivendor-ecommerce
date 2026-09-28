package com.auth_service.application.command;

public record DeleteAddressCommand(
        String userId,
        String addressId
) {
}

package com.auth_service.domain.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(Long addressId) {
        super("Address not found: " + addressId);
    }
}

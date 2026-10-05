package com.auth_service.application.port.out;

public interface RefreshTokenHasher {

    String hash(String refreshToken);
}

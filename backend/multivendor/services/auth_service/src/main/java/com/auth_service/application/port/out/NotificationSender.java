package com.auth_service.application.port.out;

public interface NotificationSender {

    void send(
            String destination,
            String message
    );
}

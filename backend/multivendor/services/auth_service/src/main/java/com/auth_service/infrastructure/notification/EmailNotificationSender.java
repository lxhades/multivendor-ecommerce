package com.auth_service.infrastructure.notification;

import com.auth_service.application.port.out.NotificationSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;

    public EmailNotificationSender(
            JavaMailSender mailSender
    ) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(
            String destination,
            String message
    ) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(destination);
        mail.setSubject("Your new password");
        mail.setText(
                "Your new password is: " + message
        );

        mailSender.send(mail);
    }
}
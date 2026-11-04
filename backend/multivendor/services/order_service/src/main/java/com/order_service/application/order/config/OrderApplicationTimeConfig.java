package com.order_service.application.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class OrderApplicationTimeConfig {
    @Bean
    public Clock orderClock() {
        return Clock.systemUTC();
    }
}

package com.multivendor.shipping.config;

import org.springframework.context.annotation.*;

import java.time.Clock;

@Configuration
public class ClockConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

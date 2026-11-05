package com.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
        properties = "order.outbox.enabled=false"
)
class OrderServiceApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsWithDatabaseConnectionAndConfiguredTimeZone() {
        assertEquals(1, jdbcTemplate.queryForObject("SELECT 1", Integer.class));
        assertEquals("Asia/Ho_Chi_Minh", jdbcTemplate.queryForObject(
                "SELECT current_setting('TimeZone')", String.class));
    }

}

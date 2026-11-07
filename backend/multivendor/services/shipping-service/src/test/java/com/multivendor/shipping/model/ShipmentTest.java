package com.multivendor.shipping.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentTest {
    @Test
    void transitions() {
        var t = Instant.parse("2026-11-07T00:00:00Z");
        var s = new Shipment(UUID.randomUUID(), "Ha Noi", t);
        assertFalse(s.advance(t.plusSeconds(2)));
        assertTrue(s.advance(t.plusSeconds(3)));
        assertEquals(ShipmentStatus.SHIPPED, s.getStatus());
        assertFalse(s.advance(t.plusSeconds(5)));
        assertTrue(s.advance(t.plusSeconds(6)));
        assertEquals(ShipmentStatus.DELIVERED, s.getStatus());
    }
}

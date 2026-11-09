package com.multivendor.shipping.controller;

import com.multivendor.shipping.dto.*;
import com.multivendor.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController

@RequestMapping("/api/shipments/admin")
public class ShipmentController {
    private final ShipmentService service;

    public ShipmentController(ShipmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{trackingCode}")
    public ShipmentResponse byTracking(@PathVariable String trackingCode) {

        return service.byTracking(trackingCode);
    }

    @GetMapping(params = "orderId")
    public ShipmentResponse byOrder(@RequestParam UUID orderId) {

        return service.byOrder(orderId);
    }
}

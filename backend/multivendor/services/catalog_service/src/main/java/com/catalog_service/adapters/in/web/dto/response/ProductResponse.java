package com.catalog_service.adapters.in.web.dto.response;
public record ProductResponse(String productId, String sellerId, String name, String description, String price, String status, String createdAt, String updatedAt) {}

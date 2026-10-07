package com.catalog_service.adapters.in.web.dto.request;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
public record UpdateProductRequest(@NotBlank String name,String description,@NotBlank @DecimalMin("0.00") String price,@NotBlank String status) {}

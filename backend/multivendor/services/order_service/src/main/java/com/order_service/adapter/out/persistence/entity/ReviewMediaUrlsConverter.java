package com.order_service.adapter.out.persistence.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Converter
public class ReviewMediaUrlsConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> urls) {
        return JSON.writeValueAsString(urls == null ? List.of() : urls);
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return List.of();
        return List.copyOf(JSON.readValue(json, new TypeReference<List<String>>() {}));
    }
}

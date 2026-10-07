package com.catalog_service.application.command;
public record UpdateCategoryCommand(String categoryId, String name, String slug) {}

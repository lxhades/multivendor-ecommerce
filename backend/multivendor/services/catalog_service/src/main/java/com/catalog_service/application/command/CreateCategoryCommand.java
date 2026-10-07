package com.catalog_service.application.command;
public record CreateCategoryCommand(String parentId, String name, String slug) {}

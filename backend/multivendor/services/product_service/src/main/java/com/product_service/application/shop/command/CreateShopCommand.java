package com.product_service.application.shop.command;
import java.util.UUID;
public record CreateShopCommand(UUID sellerId, String name, String slug, String logoUrl,
                                String bannerUrl, String description, String address, String bankDetails) {}

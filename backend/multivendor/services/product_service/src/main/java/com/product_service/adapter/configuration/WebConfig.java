package com.product_service.adapter.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.media.storage-path}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();
        String uploadPathUri = uploadDir.toUri().toString();

        if (!uploadPathUri.endsWith("/")) {
            uploadPathUri += "/";
        }

        String finalPath = uploadPathUri + "media/";

        // 👇 IN RA MÀN HÌNH ĐỂ KIỂM TRA ĐƯỜNG DẪN
        System.out.println("=== KIỂM TRA ĐƯỜNG DẪN MEDIA ===");
        System.out.println("URL Prefix: /media/**");
        System.out.println("Thư mục vật lý sẽ tìm: " + finalPath);
        System.out.println("=================================");

        registry.addResourceHandler("/media/**")
                .addResourceLocations(finalPath);
    }
}
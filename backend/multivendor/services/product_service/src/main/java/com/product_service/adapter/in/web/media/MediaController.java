package com.product_service.adapter.in.web.media;

import com.product_service.application.media.LocalMediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    private final LocalMediaStorageService storageService;

    public record MediaUploadResponse(String url) {}

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadMedia(
            @RequestParam String domain,          // "products", "sellers", "users"
            @RequestParam UUID entityId,          // ID của product/seller/user
            @RequestParam(required = false) UUID variantId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        // Validate domain để tránh path traversal
        if (!domain.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid domain");
        }
        String url = storageService.upload(domain, entityId, variantId, file);
        return ResponseEntity.ok(new MediaUploadResponse(url));
    }
}
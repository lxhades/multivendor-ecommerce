
package com.product_service.application.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalMediaStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(LocalMediaStorageService.class);

    private final Path root;
    private final Set<String> allowedMimeTypes;

    public LocalMediaStorageService(
            @Value("${app.media.storage-path}") String path,
            @Value("${app.media.allowed-mime-types}") List<String> allowedTypes) {

        this.root = Paths.get(path).toAbsolutePath().normalize();
        this.allowedMimeTypes = new HashSet<>(allowedTypes);

        log.info("Khởi tạo LocalMediaStorageService. Thư mục lưu trữ: {}", this.root);
    }

    public String upload(
            String domain,
            UUID entityId,
            UUID variantId,
            MultipartFile file) throws IOException {

        log.info("Bắt đầu upload: domain={}, entityId={}, variantId={}, fileName={}",
                domain, entityId, variantId, file.getOriginalFilename());

        // 1. Kiểm tra file rỗng
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // 2. Kiểm tra MIME type
        String contentType = file.getContentType();

        if (!allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported media type: " + contentType);
        }

        // 3. Xác định extension và thư mục
        String extension = getExtension(contentType);
        String folder = contentType.startsWith("video") ? "videos" : "images";

        // 4. Tạo đường dẫn lưu file
        String relativeDir = "media/" + domain + "/" + entityId;

        if (variantId != null) {
            relativeDir += "/variants/" + variantId;
        }

        relativeDir += "/" + folder;

        Path directory = root.resolve(relativeDir).normalize();

        // 5. Chống Path Traversal
        if (!directory.startsWith(root)) {
            throw new SecurityException("Invalid path");
        }

        Files.createDirectories(directory);

        // 6. Lưu file
        String fileName = UUID.randomUUID() + extension;
        Path destination = directory.resolve(fileName).normalize();

        if (!destination.startsWith(root)) {
            throw new SecurityException("Invalid file path");
        }

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination);
        }

        // 7. Luôn trả về URL tương đối
        // Ví dụ: /media/products/{id}/images/{filename}.jpg
        String publicUrl = "/" + relativeDir + "/" + fileName;

        log.info("Upload hoàn tất. Relative URL: {}", publicUrl);

        return publicUrl;
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            default -> throw new IllegalArgumentException("Unsupported type");
        };
    }

    // Xóa file dựa trên relative URL.
    // Ví dụ: /media/products/{id}/images/{filename}.jpg
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // Hỗ trợ cả relative URL và URL tuyệt đối cũ.
            URI uri = URI.create(fileUrl);
            String urlPath = uri.getPath();

            if (urlPath == null || !urlPath.startsWith("/media/")) {
                log.warn("URL media không hợp lệ, bỏ qua xóa: {}", fileUrl);
                return;
            }

            String relativePath = urlPath.substring(1);

            Path filePath = root.resolve(relativePath).normalize();

            // Chống xóa file nằm ngoài thư mục lưu trữ.
            if (!filePath.startsWith(root) || filePath.equals(root)) {
                log.warn("Từ chối xóa file ngoài thư mục root: {}", filePath);
                return;
            }

            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("Đã xóa file: {}", filePath);
            } else {
                log.warn("File không tồn tại: {}", filePath);
            }

        } catch (IllegalArgumentException | IOException e) {
            log.warn("Không thể xóa file media: {}", fileUrl, e);
        }
    }
}

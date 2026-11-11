package com.product_service.application.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalMediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalMediaStorageService.class);

    private final Path root;
    private final String baseUrl;
    private final Set<String> allowedMimeTypes;

    public LocalMediaStorageService(
            @Value("${app.media.storage-path}") String path,
            @Value("${app.media.public-base-url}") String baseUrl,
            @Value("${app.media.allowed-mime-types}") List<String> allowedTypes) {
        this.root = Paths.get(path).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.allowedMimeTypes = new HashSet<>(allowedTypes);
        log.info("Khởi tạo LocalMediaStorageService. Thư mục lưu trữ gốc: {}", this.root);
    }

    public String upload(String domain, UUID entityId, UUID variantId, MultipartFile file) throws IOException {
        log.info("Bắt đầu upload file: domain={}, entityId={}, variantId={}, fileName={}",
                domain, entityId, variantId, file.getOriginalFilename());

        // 1. Kiểm tra file rỗng
        if (file.isEmpty()) {
            log.warn("Upload thất bại: File rỗng");
            throw new IllegalArgumentException("File is empty");
        }

        // 2. Kiểm tra MIME type (bảo mật)
        String contentType = file.getContentType();
        log.info("Kiểm tra định dạng file. ContentType={}", contentType);
        if (!allowedMimeTypes.contains(contentType)) {
            log.warn("Upload thất bại: Định dạng không được hỗ trợ - {}", contentType);
            throw new IllegalArgumentException("Unsupported media type: " + contentType);
        }

        // 3. Xác định extension và thư mục con (images/videos)
        String extension = getExtension(contentType);
        String folder = contentType.startsWith("video") ? "videos" : "images";
        log.info("Xác định thư mục con: {}, extension: {}", folder, extension);

        // 4. Tạo đường dẫn tương đối: media/{domain}/{entityId}/...
        String relativeDir = "media/" + domain + "/" + entityId;
        if (variantId != null) {
            relativeDir += "/variants/" + variantId;
        }
        relativeDir += "/" + folder;

        Path directory = root.resolve(relativeDir).normalize();
        log.info("Đường dẫn thư mục đích tương đối: {}", relativeDir);

        // 5. Bảo mật: Chống Path Traversal
        if (!directory.startsWith(root)) {
            log.error("Bảo mật: Phát hiện nỗ lực Path Traversal! Thư mục đích nằm ngoài root: {}", directory);
            throw new SecurityException("Invalid path");
        }

        Files.createDirectories(directory);

        // 6. Tạo tên file duy nhất và lưu
        String fileName = UUID.randomUUID() + extension;
        Path destination = directory.resolve(fileName);
        log.info("Tiến hành lưu file tại: {}", destination);

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination);
            log.info("Lưu file vật lý thành công!");
        } catch (IOException e) {
            log.error("Lỗi khi đang ghi file vật lý", e);
            throw e;
        }

        // 7. Trả về URL công khai
        String publicUrl = baseUrl + "/" + relativeDir + "/" + fileName;
        log.info("Upload hoàn tất. Trả về URL: {}", publicUrl);
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

    // Phương thức xóa file vật lý (dùng khi update product/variant)
    public void deleteFileByUrl(String fileUrl) {
        log.info("Yêu cầu xóa file vật lý theo URL: {}", fileUrl);

        if (fileUrl == null || fileUrl.isBlank()) {
            log.warn("Bỏ qua xóa file vì URL rỗng.");
            return;
        }

        try {
            // Cắt bỏ baseUrl để lấy đường dẫn tương đối (VD: /media/products/1/images/a.png)
            String relativePath = fileUrl.replace(baseUrl, "");

            // Loại bỏ dấu / ở đầu
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = root.resolve(relativePath).normalize();
            log.info("Đường dẫn vật lý cần xóa: {}", filePath);

            // Bảo mật: Đảm bảo file nằm trong thư mục root
            if (filePath.startsWith(root)) {
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    log.info("Đã xóa file thành công: {}", filePath);
                } else {
                    log.warn("File không tồn tại để xóa: {}", filePath);
                }
            } else {
                log.error("Bảo mật: Cố gắng xóa file nằm ngoài thư mục root! Từ chối thao tác: {}", filePath);
            }
        } catch (IOException e) {
            // Log warning, không throw exception để không block luồng update của user
            log.warn("Lỗi IOException khi cố gắng xóa file vật lý: {}", fileUrl, e);
        }
    }
}
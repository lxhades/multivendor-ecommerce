package com.product_service.adapter.in.web.media;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import com.product_service.application.media.MediaQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaCleanupJob {

    private final MediaQueryRepository mediaQueryRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.media.storage-path}")
    private String storagePath;

    @Value("${app.media.public-base-url}")
    private String baseUrl;

    // Chạy vào 2h sáng mỗi ngày
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOrphanedFiles() {
        log.info("Bắt đầu tiến trình dọn dẹp file rác (Orphaned Files)...");

        Path root = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            log.warn("Thư mục lưu trữ không tồn tại: {}", root);
            return;
        }

        // 1. Lấy tất cả các URL đang được sử dụng trong DB
        Set<String> usedUrls = collectActiveMediaUrls();
        log.info("Tìm thấy {} URLs hợp lệ đang được sử dụng trong DB.", usedUrls.size());

        String cleanBaseUrl = baseUrl.replaceAll("/+$", "");

        // 2. Quét thư mục vật lý
        try (var paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        // Chỉ xử lý file cũ hơn 24 giờ để tránh xóa nhầm file đang trong quá trình upload
                        if (isOlderThan24Hours(path)) {
                            String relativeUrl = root.relativize(path).toString().replace("\\", "/");
                            String fullUrl = cleanBaseUrl + "/" + relativeUrl;

                            // 3. Nếu file không có trong DB -> Xóa
                            if (!usedUrls.contains(fullUrl)) {
                                try {
                                    Files.delete(path);
                                    log.info("Đã xóa file rác: {}", path);
                                } catch (IOException e) {
                                    log.error("Không thể xóa file: {}", path, e);
                                }
                            }
                        }
                    });
        } catch (IOException e) {
            log.error("Lỗi khi quét thư mục media", e);
        }

        log.info("Hoàn tất tiến trình dọn dẹp file rác.");
    }

    /**
     * Hàm private gom tất cả URL từ Database (Product, Variant, Shop)
     */
    private Set<String> collectActiveMediaUrls() {
        Set<String> activeUrls = new HashSet<>();

        try {
            // 1. Variant & Shop (URL đơn lẻ)
            activeUrls.addAll(mediaQueryRepository.findAllVariantImageUrls());
            activeUrls.addAll(mediaQueryRepository.findAllShopLogoUrls());
            activeUrls.addAll(mediaQueryRepository.findAllShopBannerUrls());

            // 2. Product (Chuỗi mảng JSON)
            List<String> productMediaJsons = mediaQueryRepository.findAllProductMediaJson();
            for (String json : productMediaJsons) {
                try {
                    if (json != null && !json.isBlank()) {
                        // Jackson 3 sẽ dùng TypeReference của tools.jackson.core
                        List<String> urls = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                        activeUrls.addAll(urls);
                    }
                } catch (Exception e) {
                    log.warn("Lỗi parse JSON media của Product: {}", json, e);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách URL từ Database", e);
        }

        return activeUrls;
    }

    /**
     * Kiểm tra file đã tạo cách đây hơn 24h chưa
     */
    private boolean isOlderThan24Hours(Path path) {
        try {
            Instant fileTime = Files.getLastModifiedTime(path).toInstant();
            return fileTime.isBefore(Instant.now().minus(24, ChronoUnit.HOURS));
        } catch (IOException e) {
            return false;
        }
    }
}
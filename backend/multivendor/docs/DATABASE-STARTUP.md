# Khởi tạo database tự động

Cả 5 service Auth, Product, Inventory, Order và Shipping mặc định bật Flyway, đọc `classpath:db/migration` và `classpath:db/seed`. Không cần bật profile local.

1. Khởi động hạ tầng bằng `docker compose -f docker-compose.infrastructure.yml up -d --build --wait --wait-timeout 300` tại thư mục backend/multivendor.
2. Khởi động các service bằng IDE. Mỗi service tự kết nối database riêng, chạy migration chưa thực hiện rồi Hibernate kiểm tra schema.
3. Truy cập API qua Kong tại http://localhost:8000.

Auth sử dụng mật khẩu khởi tạo `ducanhnguyen` (BCrypt) cho các tài khoản seed; admin: `quan.tri@example.com`. Có thể đặt `AUTH_INITIAL_PASSWORD_HASH` thành BCrypt khác trước lần khởi tạo đầu. Đây là cấu hình dữ liệu phát triển; cần đổi mật khẩu khi dùng thực tế.

Product và Order cùng dùng `CATALOG_MEDIA_BASE_URL`, mặc định http://localhost:8000, cho URL ảnh khởi tạo.

Flyway ghi nhận version trong `flyway_schema_history`: khởi động lại không chèn trùng dữ liệu đã migrate. Xóa từng bản ghi sau đó sẽ không làm seed chạy lại. Thay đổi dữ liệu sau khi đã migrate cần migration version mới.

Container PostgreSQL mới nhưng gắn volume cũ vẫn dùng dữ liệu cũ. Muốn khởi tạo từ đầu cần database/volume trống; cấu hình này không tự xóa dữ liệu. Database có bảng nhưng chưa có lịch sử Flyway cần xử lý chuyển đổi riêng, không tự baseline.

Các file SQL schema và seed vẫn là tài nguyên bắt buộc của từng service. Không chạy build, test hoặc migration trong lần cập nhật cấu hình này.


## Chạy toàn bộ Docker và migration mới

Chế độ toàn bộ Docker dùng `docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build --wait --wait-timeout 300`. Xem hướng dẫn [Docker/IDE](DOCKER-DEVELOPMENT.md).

Order service hiện có V1–V5 schema/index, V6–V8 seed và V9__admin_order_query_indexes.sql cho danh sách đơn/doanh thu Admin. Không sửa SQL đã áp dụng hoặc chạy seed thủ công để cập nhật dữ liệu; dùng migration mới. Những service khác giữ migration/seed riêng trong src/main/resources/db, không gom vào init SQL của container PostgreSQL.

Actuator readiness kiểm tra trạng thái ứng dụng và database, Auth thêm Redis. PostgreSQL healthy chỉ xác nhận server nhận kết nối; readiness của Java sau khởi tạo mới là gate cho backend. Flyway validate hoặc migrate lỗi phải xử lý theo log, không tự bật baseline/repair để bỏ qua.

Giữ nguyên project name và volume khi chuyển Docker/IDE. Không dùng down -v nếu cần giữ dữ liệu. Chưa chạy migration hoặc xác minh DB trong lần cập nhật tài liệu 14/11/2026.

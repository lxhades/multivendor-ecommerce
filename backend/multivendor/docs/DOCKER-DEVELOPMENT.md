# Chạy backend bằng Docker và IDE

Cập nhật 14/11/2026 theo source hiện tại. Frontend `my-website` chạy local và gọi Kong `http://localhost:8000`. Các lệnh thực hiện tại `backend/multivendor`, dùng Docker Desktop Linux containers. Chưa chạy build/test/Compose hoặc kiểm tra hoạt động thực tế trong lần cập nhật tài liệu.

## Chọn chế độ chạy

| File | Vai trò |
| --- | --- |
| docker-compose.infrastructure.yml | Chạy riêng 5 PostgreSQL, Redis, Kafka, Kong; backend chạy IDE; Kafka UI tùy chọn tools |
| docker-compose.yml | Định nghĩa gốc hạ tầng và 5 backend theo profiles; hỗ trợ IDE/kết hợp |
| docker-compose.apps.yml | Override cho toàn bộ backend Docker: DNS nội bộ, Kong chờ các backend healthy; dùng cùng file gốc và profile apps |

Infrastructure dùng extends từ file gốc để giữ đồng nhất healthcheck, network và volume. Không ghép infrastructure với apps. Giữ cùng project name khi đổi chế độ để tái sử dụng dữ liệu.

## Phát triển hằng ngày: hạ tầng Docker, backend IDE

```powershell
docker compose -f docker-compose.infrastructure.yml up -d --build --wait --wait-timeout 300
```

Lần đầu chỉ build image Kafka tùy chỉnh, không build Java. Sau đó có thể bỏ --build. Kafka UI chỉ bật khi cần:

```powershell
docker compose -f docker-compose.infrastructure.yml --profile tools up -d --build
```

Mở các backend cần dùng bằng IDE. Database/Flyway được khởi tạo khi Java service chạy; PostgreSQL healthy không đồng nghĩa schema ứng dụng đã có. Kong gọi backend qua host.docker.internal và không chờ IDE: route của service chưa chạy có thể trả 502 dù Kong healthy.

| Thành phần | Địa chỉ từ máy host |
| --- | --- |
| Auth DB | localhost:5432/auth_db |
| Product DB | localhost:5433/product_db |
| Inventory DB | localhost:5434/inventory_db |
| Order DB | localhost:5435/order_db |
| Shipping DB | localhost:5436/shipping_db |
| Redis | localhost:6379, mật khẩu REDIS_PASSWORD |
| Kafka | localhost:9092 |
| Kong | localhost:8000, thay bằng KONG_PROXY_PORT nếu cấu hình |
| Kafka UI | localhost:9091, profile tools |
| Backend | Auth8081 / Product8082 / Order8084 / Inventory8085 / Shipping8086 |

## Toàn bộ backend Docker

Dừng các instance backend trong IDE để tránh trùng port.

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build --wait --wait-timeout 300
```

Dùng DNS nội bộ auth-service/product-service/shipping-service; Kong mount kong-gateway/config/kong.docker.yml. Thứ tự phụ thuộc: database/Redis/Kafka sẵn sàng; Auth healthy; các service còn lại healthy; Kong khởi động. Auth chỉ cần database riêng và Redis nên có thể khởi động song song Kafka. Backend không chờ Kong.

Chỉ rebuild service có thay đổi, ví dụ:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build product-service
```

Dockerfile Java dùng Maven build module được chọn và dependencies; bỏ test khi build image. --wait chỉ đợi container healthy/running, không xác minh nghiệp vụ. Có thể tăng --wait-timeout trên máy chậm.

## Chạy kết hợp và chuyển chế độ

Ví dụ Product Docker, các service khác IDE:

```powershell
docker compose --profile product up -d --build
```

Dùng file base, không dùng override apps. Khi chuyển từ full Docker sang IDE, dừng backend container và tạo lại Kong với upstream host:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps stop auth-service product-service order-service inventory-service shipping-service
docker compose -f docker-compose.infrastructure.yml up -d --build --wait --wait-timeout 300
```

Không dùng --remove-orphans với file infrastructure vì các backend không nằm trong file này. Nếu còn container init từ cấu hình Kafka cũ, chỉ dọn đúng hai container cũ sau khi xác định chúng; không xóa volume. Không chạy hai Compose project khác tên có cùng container_name/port.

## Media dùng chung

Docker bind mount `${MEDIA_HOST_PATH:-./uploads}` vào `/app/uploads` của Product. IDE cần cùng thư mục tuyệt đối, ví dụ:

```text
APP_MEDIA_STORAGE_PATH=D:/Project/Java Project/do_an_chuyen_nganh/multivendor/backend/multivendor/uploads
APP_MEDIA_PUBLIC_BASE_URL=http://localhost:8000
CATALOG_MEDIA_BASE_URL=http://localhost:8000
```

MEDIA_HOST_PATH là cấu hình Compose, APP_MEDIA_STORAGE_PATH là cấu hình Spring. Media theo media/{domain}/{entityId}/...; seed catalog đóng gói trong classpath. Giữ Product và Order cùng CATALOG_MEDIA_BASE_URL, không thay placeholder migration đã áp dụng. Không ghi đè .env hiện có; tham khảo .env.example. Backup PostgreSQL và uploads riêng.

## Readiness và Kafka hiện tại

- Actuator chỉ expose health, ẩn chi tiết/thành phần. Readiness gồm readinessState + db; Auth thêm redis. Liveness chỉ livenessState. Security cho phép 3 endpoint health trực tiếp trên port service; Kong không có route /actuator.
- Flyway chạy trong lúc khởi tạo Spring; migration lỗi không đạt readiness. Runtime image có curl để HTTP healthcheck.
- PostgreSQL kiểm tra TCP bằng pg_isready; Redis phải trả đúng PONG sau xác thực.
- Kafka là **một container**, không còn kafka-init/kafka-volume-init. Image docker/kafka/Dockerfile kế thừa apache/kafka:3.7.0; entrypoint sửa quyền named volume bằng root rồi hạ xuống appuser; start-kafka.sh giám sát broker, chạy init-topics.sh, tạo marker khi thành công. Healthcheck cần marker và broker đáp ứng.
- 7 topic: product.inventory-init.v1, inventory.reserve.requested.v1, inventory.reservation.result.v1, inventory.release.requested.v1, order.shipped.v1, order.delivered.v1, shipping.status.v1. Khởi tạo idempotent, 1 partition/replica cho dev; đổi topic config thì cập nhật script. Không reset offset hoặc xóa topic.
- kafka_data giữ log ở /tmp/kraft-combined-logs và cluster ID ổn định. Giữ nguyên tên volume; volume mới không tự sao chép dữ liệu từ writable layer container cũ.
- Readiness HTTP backend không kiểm tra Kafka runtime hoặc mọi HTTP dependency. depends_on chỉ gate khởi tạo Compose; unhealthy về sau không tự dừng/restart các service phụ thuộc. restart policy áp dụng khi process thoát; retry/reconnect vẫn cần ở ứng dụng.

## Xem log và xử lý lỗi

```powershell
docker compose -f docker-compose.infrastructure.yml ps
docker compose -f docker-compose.infrastructure.yml logs --tail=100 kafka kong
docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps logs --tail=100 order-service
```

Nếu Kafka không ghi được bootstrap.checkpoint.tmp: xem log quyền volume/dung lượng và entrypoint; không xóa meta.properties, đổi cluster ID hoặc dùng down -v để chữa lỗi quyền. Nếu API chưa chạy, kiểm tra log service và /actuator/health/readiness trực tiếp. Kiểm tra SQL/Flyway trước khi coi lỗi là gateway.

Dừng hạ tầng: `docker compose -f docker-compose.infrastructure.yml stop`. Không xóa volume khi chỉ đổi chế độ. Cấu hình/khóa/mật khẩu seed phục vụ phát triển; chưa phải cấu hình production.

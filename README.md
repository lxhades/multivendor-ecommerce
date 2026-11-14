# Tại folder multiverdor
cách 1: Khởi chạy toàn bộ dự án với docker: 
docker compose -f docker-compose.yml -f docker-compose.apps.yml --profile apps up -d --build --remove-orphans --wait --wait-timeout 300

cách 2: Khởi chạy hạ tầng, service chạy ở IDE
docker compose -f docker-compose.infrastructure.yml up -d --build --wait --wait-timeout 300

Đảm bảo cac container đã chaỵ ( Kong thường bị tắt do thiếu ram, hãy mở và restart lại kong)

# Tại folder my website chạy: 
npm ci
npm run dev

tài khoản qtri: quan.tri@example.com
tài khoản seller: quang.huy@example.com
pass: ducanhnguyen

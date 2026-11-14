# API Multivendor cho frontend

Cập nhật 14/11/2026 theo source; chưa chạy build/test hoặc nghiệm thu API thật. Hướng dẫn vận hành: [Docker/IDE](DOCKER-DEVELOPMENT.md), [Flyway](DATABASE-STARTUP.md), [chi tiết báo cáo Seller](SELLER-REPORTS.md).

Tài liệu theo route trực tiếp của các service. Kong đã khai báo các route nghiệp vụ trong `kong-gateway/config/kong.yml`. Kong chuyển tiếp `/api/media/upload` và GET `/media/**` về product_service, `/api/reviews` về order_service; gateway local dùng `http://localhost:8000`.

| Service | Base URL mặc định |
|---|---|
| Auth | `http://localhost:8081` |
| Product | `http://localhost:8082` |
| Order | `http://localhost:8084` |
| Inventory | `http://localhost:8085` |
| Shipping | `http://localhost:8086` |

## Quy ước gọi API

- Route có quyền yêu cầu header `Authorization: Bearer <accessToken>`. Client lấy token từ `data.accessToken` khi login và thay cả access token lẫn refresh token khi refresh thành công.
- Body gửi JSON với `Content-Type: application/json`. Hầu hết API trả `{ "message": "...", "data": ..., "timestamp": "..." }`. `data` có thể không xuất hiện khi thao tác không trả dữ liệu.
- Upload media trả trực tiếp `{ "url": "..." }`, không bọc `data`. File tại `/media/**` trả nội dung ảnh/video trực tiếp.
- Shipping trả đối tượng vận đơn trực tiếp. JWKS trả `{ "keys": [...] }`. Khi có lỗi, ưu tiên HTTP status. Body lỗi có thể khác nhau giữa service.
- `401` thường là thiếu token hoặc token không hợp lệ. `403` là thiếu quyền hoặc sai chủ sở hữu. `409` dùng cho một số xung đột nghiệp vụ, ví dụ hủy đơn còn `PENDING`.
- ID thường là UUID. `addressId` là số. Timestamp là ISO-8601. Query ngày dùng `YYYY-MM-DD`.
- API hồ sơ và quản trị người dùng trả `role` là vai trò chính lưu trong Auth. Claim `roles` trong access token chứa các quyền theo cấp bậc để phân quyền.
- Route công khai dùng ký hiệu **Công khai**. **Refresh token** nghĩa là API nhận token trong body thay cho access token.

## Luồng mua hàng COD

1. Xem catalog, chi tiết sản phẩm và số lượng còn bán của biến thể.
2. Đăng nhập Buyer, tạo checkout với `Idempotency-Key` riêng cho mỗi lần đặt. Khi retry cùng lần đặt, dùng lại key và body cũ.
3. Lưu `data.checkout.id` và các `data.orders[].id`. Một checkout có thể tạo nhiều đơn theo shop. Kiểm tra trạng thái checkout và từng đơn vì giữ chỗ xử lý bất đồng bộ.
4. Seller giao đơn ở `CONFIRMED`. Theo dõi trạng thái đơn đến `DELIVERED`. Inventory chốt giữ chỗ qua Kafka và số tồn có thể cập nhật sau Order.
5. Với đơn COD ở `CONFIRMED`, Buyer hoặc Seller có quyền có thể hủy. Order trả `CANCELLED` trước khi Inventory xử lý nhả giữ chỗ xong.

### Ví dụ tạo checkout

```http
POST /api/checkouts
Authorization: Bearer <buyerAccessToken>
Idempotency-Key: <uuid-cua-lan-dat>
Content-Type: application/json
```

```json
{
  "paymentMethod": "COD",
  "shippingAddress": "12 Đường Test, TP. Hồ Chí Minh",
  "items": [{ "variantId": "<variantId>", "quantity": 1 }]
}
```

### Dữ liệu cho dashboard Seller

Ưu tiên dùng hai API báo cáo `/api/orders/seller/shops/{shopId}/reports/demand` và `/sales`, không tải toàn bộ đơn để tổng hợp ở frontend. Sales tính DELIVERED theo updatedAt, trả summary/periods và bestSellingVariants; demand tính ngày tạo/trạng thái hiện tại. Xem [chi tiết báo cáo Seller](SELLER-REPORTS.md).

GET `/api/orders/seller/shops/{shopId}?page=0&size=20&status=DELIVERED` trả `data.items[]` bọc `{order,createdAt,updatedAt}`; order là OrderResult có items sản phẩm. Bộ lọc ngày của danh sách Seller theo createdAt. updatedAt có thể thay đổi sau giao hàng, chưa phải deliveredAt bất biến.

## Danh sách API theo service

### Auth

API tài khoản dùng JWT của chính tài khoản. Thay `buyerAccessToken` bằng token Seller/Admin khi gọi API tài khoản của vai trò đó.

| HTTP | Route | Quyền | Use case | Body mẫu |
|---|---|---|---|---|
| POST | `/auth/register` | Công khai | Đăng ký Buyer | `register` |
| POST | `/auth/login` | Công khai | Đăng nhập tài khoản | `login` |
| GET | `/auth/me` | BUYER | Xem tài khoản | — |
| PUT | `/auth/forgotpassword` | Công khai | Quên mật khẩu (gửi email) | `forgot` |
| PUT | `/auth/changepassword` | BUYER | Đổi mật khẩu | `password` |
| POST | `/auth/refresh` | Refresh token | Đổi access và refresh token | `refresh` |
| POST | `/auth/logout` | Refresh token | Thu hồi refresh token | `refresh` |
| POST | `/auth/me/register-seller` | BUYER | Đăng ký làm Seller | — |
| GET | `/auth/users/{buyerId}/addresses` | BUYER | Danh sách địa chỉ của mình | — |
| POST | `/auth/users/{buyerId}/addresses` | BUYER | Thêm địa chỉ | `address` |
| PUT | `/auth/users/{buyerId}/addresses/{addressId}` | BUYER | Sửa địa chỉ | `address` |
| DELETE | `/auth/users/{buyerId}/addresses/{addressId}` | BUYER | Xóa địa chỉ | — |
| GET | `/api/users/admin?page=0&size=20` | ADMIN | Danh sách người dùng | — |
| GET | `/api/users/admin/{buyerId}` | ADMIN | Chi tiết người dùng | — |
| PUT | `/api/users/admin/{userId}` | ADMIN đang ACTIVE | Sửa email/số điện thoại, giữ role/password | `{ "email": "buyer@example.com", "phone": "0901234567" }` |
| PATCH | `/api/users/admin/{userId}/status` | ADMIN đang ACTIVE | Cấm/bỏ cấm tài khoản | `{ "status": "LOCKED" }` hoặc `{ "status": "ACTIVE" }` |
| DELETE | `/api/users/admin/{deleteUserId}` | ADMIN đang ACTIVE | Xóa tài khoản; không cho tự xóa | — |

Quản trị user (cập nhật 14/11/2026): danh sách nhận `keyword` tìm email/điện thoại, page bắt đầu 0, size tối đa 100; response `{users, page, size, totalElements, totalPages}`. PUT/PATCH trả `AdminUserResult` gồm userId/email/phone/status/role. PUT email tối đa 100 ký tự và phone 10 số bắt đầu 0; không nhận quyền hoặc mật khẩu. PATCH dùng trạng thái đích nên gọi lại cùng trạng thái không đảo trạng thái. Không cho tự LOCKED hoặc tự DELETE (409); email trùng trả 409, user không tồn tại trả 404. Các API quản trị kiểm tra role ADMIN và trạng thái ACTIVE từ DB bên cạnh JWT.

`LOCKED` chặn login và refresh; access token đã phát vẫn có thể được các service khác chấp nhận đến hết hạn vì JWT stateless. DELETE xóa user/địa chỉ trong Auth, không xóa lịch sử orders thuộc service khác. Không có migration mới; cần khởi động lại auth_service sau cập nhật code.

| GET | `/oauth2/jwks` | Công khai | Khóa công khai JWT | — |

### Product

Catalog công khai. Seller quản lý dữ liệu của shop mình. Route `/internal/products/variants/verify` phục vụ Order khi checkout và yêu cầu JWT Buyer.

| HTTP | Route | Quyền | Use case | Body mẫu |
|---|---|---|---|---|
| GET | `/shops?page=0&size=20` | Công khai | Danh sách shop | — |
| GET | `/shops/{shopId}` | Công khai | Chi tiết shop | — |
| GET | `/categories` | Công khai | Danh sách danh mục | — |
| GET | `/categories/{categoryId}` | Công khai | Chi tiết danh mục | — |
| GET | `/categories/{categoryId}/children` | Công khai | Danh mục con | — |
| GET | `/api/products?shopId={shopId}&includeChildren=false` | Công khai | Danh sách sản phẩm | — |
| GET | `/api/products/{productId}` | Công khai | Chi tiết sản phẩm và biến thể | — |
| POST | `/internal/products/variants/verify` | BUYER | Xác minh biến thể nội bộ | `verify` |
| POST | `/api/media/upload` | Công khai (cấu hình hiện tại) | Upload một ảnh/video, trả URL | multipart, xem phần Media |
| GET | `/media/{domain}/{entityId}/images/{fileName}` | Công khai | Xem ảnh đã upload | — |
| GET | `/media/{domain}/{entityId}/videos/{fileName}` | Công khai | Xem video đã upload | — |
| GET | `/media/{domain}/{entityId}/variants/{variantId}/images/{fileName}` | Công khai | Xem ảnh upload vào thư mục variant | — |
| GET | `/media/{domain}/{entityId}/variants/{variantId}/videos/{fileName}` | Công khai | Xem video upload vào thư mục variant | — |
| POST | `/api/shops/seller` | SELLER | Tạo shop | `shop` |
| GET | `/api/shops/seller/my-shop` | SELLER | Shop của Seller | — |
| PUT | `/api/shops/seller/{shopId}` | SELLER | Cập nhật shop | `shop` |
| DELETE | `/api/shops/seller/{shopId}` | SELLER | Đóng shop | — |
| PATCH | `/api/shops/seller/{shopId}` | SELLER | Mở lại shop | — |
| GET | `/api/shops/admin?page=0&size=20` | ADMIN | Admin liệt kê shop | — |
| GET | `/api/shops/admin/{shopId}` | ADMIN | Admin xem shop | — |
| PATCH | `/api/shops/admin/{shopId}/status?shopStatus=ACTIVE` | ADMIN | Admin đổi trạng thái shop | — |
| DELETE | `/api/shops/admin/{shopId}` | ADMIN | Admin đóng shop | — |
| POST | `/api/categories/admin` | ADMIN | Tạo danh mục | `category` |
| PUT | `/api/categories/admin/{categoryId}` | ADMIN | Sửa danh mục | `category` |
| PATCH | `/api/categories/admin/{categoryId}/status` | ADMIN | Đổi trạng thái danh mục | `status` |
| DELETE | `/api/categories/admin/{categoryId}` | ADMIN | Xóa danh mục | — |
| GET | `/api/products/seller/shops/{shopId}` | SELLER | Seller liệt kê sản phẩm | — |
| POST | `/api/products/seller/shops/{shopId}` | SELLER | Tạo sản phẩm | `product` |
| GET | `/api/products/seller/shops/{shopId}/{productId}` | SELLER | Seller xem sản phẩm | — |
| PUT | `/api/products/seller/shops/{shopId}/{productId}` | SELLER | Sửa sản phẩm | `product` |
| PATCH | `/api/products/seller/shops/{shopId}/{productId}/status` | SELLER | Đổi trạng thái sản phẩm | `status` |
| DELETE | `/api/products/seller/shops/{shopId}/{productId}` | SELLER | Xóa sản phẩm | — |
| POST | `/api/products/seller/shops/{shopId}/{productId}/variants` | SELLER | Thêm biến thể | `variant` |
| PUT | `/api/products/seller/shops/{shopId}/{productId}/variants/{variantId}` | SELLER | Sửa biến thể | `variant` |
| DELETE | `/api/products/seller/shops/{shopId}/{productId}/variants/{variantId}` | SELLER | Xóa biến thể | — |
| GET | `/api/products/admin/pending` | ADMIN | Sản phẩm chờ duyệt | — |
| GET | `/api/products/admin/{productId}` | ADMIN | Admin xem sản phẩm | — |
| POST | `/api/products/admin/{productId}/approve` | ADMIN | Duyệt sản phẩm | `decision` |
| POST | `/api/products/admin/{productId}/reject` | ADMIN | Từ chối sản phẩm | `decision` |

#### Media: upload ảnh/video và lưu URL

Phần này mô tả `MediaController`, `LocalMediaStorageService`, `WebConfig` và `application.properties` hiện tại; thay cho các route dự kiến trong kế hoạch media trước đó. Module hiện ghi file vào `./uploads/media/...`, phục vụ tại `/media/**`, không dùng `/public/**`.

**Upload:** `POST http://localhost:8082/api/media/upload`, body `multipart/form-data`, mỗi request một file. `domain`, `entityId`, `variantId` là request parameter: có thể đặt trong query hoặc trường text của form-data. `file` là part nhị phân.

| Tên | Kiểu | Bắt buộc | Ý nghĩa |
|---|---|---|---|
| `domain` | string | Có | Nhóm lưu trữ, thường dùng `products`, `sellers`, `users`; code chấp nhận mọi giá trị khớp `^[a-zA-Z0-9_-]+$`, chưa giới hạn ở ba giá trị này |
| `entityId` | UUID | Có | ID sản phẩm/shop/người dùng theo nhóm lưu trữ; upload chưa kiểm tra bản ghi có tồn tại |
| `variantId` | UUID | Không | Thêm thư mục `variants/{variantId}`; upload chưa kiểm tra variant thuộc entityId |
| `file` | binary | Có | Một file JPEG, PNG, WebP hoặc MP4 |

Ví dụ upload ảnh product (thay UUID và đường dẫn file bằng giá trị thực):

```bash
curl -X POST "http://localhost:8082/api/media/upload" \
  -F "domain=products" \
  -F "entityId=11111111-1111-4111-8111-111111111111" \
  -F "file=@./photo.webp;type=image/webp"
```

Upload ảnh variant dùng cùng endpoint, thêm `-F "variantId=<variantId>"`. Upload video thay part file bằng `-F "file=@./clip.mp4;type=video/mp4"`. Không tự đặt header `Content-Type: application/json`; khi dùng FormData trong browser, để browser sinh header multipart cùng boundary.

Response thành công **HTTP 200**, không có wrapper ApiResponse:

```json
{
  "url": "http://localhost:8000/media/products/11111111-1111-4111-8111-111111111111/images/22222222-2222-4222-8222-222222222222.webp"
}
```

Frontend đọc `response.url` khi dùng fetch JSON, hoặc `response.data.url` nếu dùng response Axios nguyên bản. Không đọc `data.data.url` theo wrapper API nghiệp vụ.

**Đường dẫn và định dạng:** tên file là UUID do backend sinh. Phần mở rộng lấy từ MIME khai báo, không lấy tên file gốc:

| MIME được chấp nhận | Thư mục | Đuôi file |
|---|---|---|
| `image/jpeg` | `images` | `.jpg` |
| `image/png` | `images` | `.png` |
| `image/webp` | `images` | `.webp` |
| `video/mp4` | `videos` | `.mp4` |

URL không có variant: `{baseUrl}/media/{domain}/{entityId}/{images|videos}/{fileUuid}.{ext}`. Có variant: `{baseUrl}/media/{domain}/{entityId}/variants/{variantId}/{images|videos}/{fileUuid}.{ext}`. Dùng URL backend trả về cho `<img src>` hoặc `<video src controls>`, không ghép thêm `/media` hay đổi sang `/public`.

**Giới hạn thực tế:** `spring.servlet.multipart.max-file-size=10MB`, `max-request-size=100MB`; cả ảnh và video đều chịu giới hạn 10MB mỗi file. `app.media.max-file-size=10MB` hiện chưa được service đọc để kiểm tra riêng. Service chỉ kiểm tra file không rỗng và Content-Type trong danh sách cho phép, chưa kiểm tra nội dung thực tế, giải mã ảnh hoặc codec video. Chưa có quota số ảnh/video mỗi product; hiện cũng chấp nhận video khi truyền variantId.

**Luồng lưu vào sản phẩm:** upload chỉ ghi file và trả URL, không tự cập nhật DB. Thực hiện:

1. Tạo product/variant bằng API JSON để lấy ID, hoặc dùng ID đã có khi sửa.
2. Upload từng ảnh/video với domain=products và entityId=productId; thêm variantId cho file của variant.
3. Gửi lại URL qua API sửa product trong `mediaJson` hoặc API sửa variant trong `imageUrl`. Các API PUT nhận toàn bộ dữ liệu cần thiết, cần giữ các trường nghiệp vụ hiện có khi cập nhật media.
4. GET product trả lại URL đã lưu; frontend parse mediaJson thành mảng và hiển thị media. Upload thành công nhưng chưa lưu URL vào DB chưa hoàn tất bước gắn media.

Ví dụ phần dữ liệu media trong body/response product (các trường khác giữ theo DTO product):

```json
{
  "mediaJson": "[\"http://localhost:8000/media/products/11111111-1111-4111-8111-111111111111/images/22222222-2222-4222-8222-222222222222.webp\",\"http://localhost:8000/media/products/11111111-1111-4111-8111-111111111111/videos/33333333-3333-4333-8333-333333333333.mp4\"]"
}
```

`mediaJson` hiện là **chuỗi JSON mảng URL**, không phải mảng `{id,type,url,position}` và chưa có trường response `media` như thiết kế trước. Phân biệt video bằng phần mở rộng `.mp4` trên pathname URL; còn ảnh dùng JPEG/PNG/WebP. URL ảnh variant được gửi trong `imageUrl` của body variant; response variant trả cùng tên trường. Không có API media DELETE/sắp xếp riêng trong controller hiện tại. Sắp xếp/gỡ URL product bằng cách gửi lại toàn bộ mảng mediaJson; xóa ảnh variant bằng imageUrl=null.

**Cấu hình truy cập:** `app.media.storage-path=./uploads` tính theo working directory; `app.media.public-base-url=http://localhost:8000` là địa chỉ cố định hiện tại, nên URL upload trả về trỏ qua Kong dù request upload gọi port 8082. WebConfig ánh xạ `/media/**` đến `<storage-path>/media/`. Có thể GET trực tiếp cùng pathname qua port 8082 để kiểm tra file; cần cấu hình public-base-url phù hợp khi triển khai. Kong đã chuyển tiếp `/api/media/**` qua product-api, nên có thể upload tại `http://localhost:8000/api/media/upload`. Route file `/media/**` hiện giới hạn method GET, chưa khai báo HEAD qua gateway.

**Lỗi và hành vi cần biết:**

- Thiếu domain/entityId được xử lý như request thiếu tham số. Domain không khớp regex, file rỗng hoặc MIME không được hỗ trợ phát sinh IllegalArgumentException (handler chung ánh xạ 400).
- Lỗi ghi file phát sinh IOException (handler chung ánh xạ 500). Lỗi thiếu part file, UUID sai hoặc vượt multipart limit phụ thuộc exception handler/framework hiện tại; module chưa có mapping riêng để cam kết toàn bộ các lỗi này là 400/413/415.
- SecurityConfig đang permitAll `/api/media/**` và `/media/**`; upload không yêu cầu JWT, không xác minh quyền sở hữu. Các API lưu URL vào product/variant vẫn yêu cầu quyền Seller và sở hữu shop.
- Mỗi lần upload tạo file mới; retry không có idempotency và có thể tạo nhiều file. Ghi thẳng vào thư mục chính thức, chưa có staging và chưa dọn ngay file ghi dở khi IOException.
- Job cleanup chạy 02:00 mỗi ngày theo timezone JVM (ứng dụng main đặt Asia/Ho_Chi_Minh), quét file cũ hơn 24 giờ và giữ URL tham chiếu trong product.mediaJson, variant.imageUrl, shop.logoUrl/bannerUrl. URL chỉ được upload mà không lưu vào các trường này có thể bị xóa. Chưa tra tham chiếu từ Auth cho domain=users.
- Job chưa xét OrderItem.variantImageUrl: khi ảnh cũ không còn ở catalog, file có thể bị xóa dù đơn hàng vẫn lưu URL cũ. Job còn bắt lỗi đọc DB/parse JSON rồi tiếp tục với tập URL thiếu, nên lỗi truy vấn hoặc dữ liệu JSON lỗi có thể khiến media đang dùng bị nhận nhầm là file rác. Chưa bảo đảm giữ ảnh lịch sử theo kế hoạch trước.

#### Tên và ảnh biến thể

Các response sản phẩm có `variants[]` trả thêm `variantName` và `imageUrl`:

```json
{
  "id": "<variantId>",
  "sku": "AO-BLUE-M",
  "price": 125000,
  "attributesJson": "{\"color\":\"Blue\",\"size\":\"M\"}",
  "active": true,
  "soldCount": 0,
  "deletedAt": null,
  "variantName": "Blue - M",
  "imageUrl": "https://example.com/variants/ao-blue-m.jpg"
}
```

- `variantName` do backend suy ra từ các giá trị chuỗi, số hoặc boolean trong `attributesJson`, nối bằng ` - `. Giá trị rỗng, JSON lỗi hoặc không có thuộc tính phù hợp trả `null`. Frontend không gửi `variantName` khi tạo/sửa biến thể.
- `imageUrl` là URL ảnh riêng của biến thể, có thể `null`. Gửi trường này trong body `variant` khi thêm/sửa biến thể hoặc trong `product.variants[]` khi tạo sản phẩm. Khi sửa biến thể bằng PUT, bỏ qua `imageUrl` hoặc gửi `null` sẽ xóa URL ảnh hiện tại.
- Nếu không có ảnh biến thể, frontend có thể dùng ảnh sản phẩm từ `mediaJson` hoặc ảnh mặc định. Backend không tự thay `imageUrl` bằng ảnh sản phẩm.

### Order

Buyer chỉ thấy đơn của mình. Seller chỉ thấy đơn của shop mình. `from`/`to` ở danh sách đơn Seller tính theo ngày tạo đơn, dạng `YYYY-MM-DD`, bao gồm cả ngày cuối theo giờ Việt Nam.

| HTTP | Route | Quyền | Use case | Body mẫu |
|---|---|---|---|---|
| POST | `/api/checkouts` | BUYER | Tạo checkout COD | `checkout` |
| GET | `/api/checkouts/{checkoutId}` | BUYER | Buyer xem checkout | — |
| GET | `/api/checkouts/{checkoutId}` | ADMIN | Admin xem checkout | — |
| GET | `/api/orders` | BUYER | Đơn của Buyer | — |
| GET | `/api/orders?buyerId={buyerId}` | BUYER | Lọc đơn theo Buyer hiện tại | — |
| GET | `/api/orders?checkoutId={checkoutId}` | BUYER | Buyer xem đơn trong checkout | — |
| GET | `/api/orders/{orderId}` | BUYER | Buyer xem chi tiết đơn | — |
| POST | `/api/orders/{orderId}/cancel` | BUYER | Buyer hủy đơn COD | — |
| GET | `/api/orders/seller/shops/{shopId}?page=0&size=20&status=DELIVERED` | SELLER | Seller xem trang đơn đã giao | — |
| GET | `/api/orders/seller?shopId={shopId}` | SELLER | Seller xem đơn theo shop | — |
| GET | `/api/orders/seller?checkoutId={checkoutId}` | SELLER | Seller xem đơn theo checkout | — |
| GET | `/api/orders/seller/{orderId}` | SELLER | Seller xem chi tiết đơn | — |
| POST | `/api/orders/seller/{orderId}/cancel` | SELLER | Seller hủy đơn COD | — |
| POST | `/api/orders/seller/{orderId}/ship` | SELLER | Seller gửi đơn cho Shipping | — |
| GET | `/api/orders/admin/page` | ADMIN | Danh sách toàn hệ thống, lọc/sort/phân trang server | Query, xem mục API Admin bên dưới |
| GET | `/api/orders/admin/reports/revenue` | ADMIN | Doanh thu tổng hợp server, tổng toàn phạm vi và shop phân trang | from/to, shopId tùy chọn, page/size |
| GET | `/api/orders/admin?shopId={shopId}` | ADMIN | Admin xem đơn theo shop | — |
| GET | `/api/orders/admin?checkoutId={checkoutId}` | ADMIN | Admin xem đơn theo checkout | — |
| GET | `/api/orders/admin?buyerId={buyerId}` | ADMIN | Admin xem đơn theo Buyer | — |
| GET | `/api/orders/admin/{orderId}` | ADMIN | Admin xem chi tiết đơn | — |
| POST | `/api/orders/admin/{orderId}/cancel` | ADMIN | Admin hủy đơn COD | — |
| POST | `/api/orders/admin/{orderId}/ship` | ADMIN | Admin gửi đơn cho Shipping | — |

#### Tên và ảnh biến thể trong đơn hàng

Mỗi phần tử `OrderResult.items[]` có cấu trúc sau. Dữ liệu này có trong `data.orders[].items[]` của response tạo checkout và `data.items[]` của response chi tiết đơn Buyer/Seller/Admin:

```json
{
  "id": "<orderItemId>",
  "productId": "<productId>",
  "variantId": "<variantId>",
  "productName": "Áo thun",
  "variantName": "Blue - M",
  "variantImageUrl": "https://example.com/variants/ao-blue-m.jpg",
  "unitPrice": 125000,
  "quantity": 1
}
```

| Trường | Ý nghĩa |
|---|---|
| `productName` | Tên sản phẩm tại thời điểm checkout |
| `variantName` | Tên biến thể tại thời điểm checkout; có thể `null` |
| `variantImageUrl` | URL ảnh biến thể tại thời điểm checkout; có thể `null` |
| `unitPrice` | Đơn giá được product service xác minh tại thời điểm checkout |

Order lấy tên và ảnh từ product service rồi lưu cùng dòng hàng. Sửa tên thuộc tính hoặc ảnh biến thể sau này không thay đổi dữ liệu đã lưu trong đơn. Tuy nhiên, job media cleanup hiện chưa kiểm tra tham chiếu từ OrderItem, nên URL ảnh cũ vẫn có thể mất file vật lý; xem phần Media. Đơn cũ chưa lưu các trường này trả `null`; frontend cần xử lý tên/ảnh thiếu. Tên trường ảnh trong response sản phẩm là `imageUrl`, còn trong dòng hàng của đơn là `variantImageUrl`.

Body tạo checkout vẫn chỉ gửi `variantId` và `quantity` cho mỗi dòng hàng. Khi retry cùng `Idempotency-Key`, backend trả dữ liệu đơn đã lưu. Danh sách Seller phân trang bọc OrderResult trong data.items[].order; API Admin /page trả trực tiếp OrderResult trong data.items[]. Cả hai có items sản phẩm; có thể GET chi tiết để lấy trạng thái mới nhất.

### Báo cáo Seller (Order service)

Đã dựng code, **chưa chạy build/test hoặc SQL** theo yêu cầu. Thiết kế và response mẫu đầy đủ tại [SELLER-REPORTS.md](SELLER-REPORTS.md).

| HTTP | Route | Quyền | Nội dung |
|---|---|---|---|
| GET | `/api/orders/seller/shops/{shopId}/reports/demand?from=2026-11-01&to=2026-11-30&groupBy=DAY` | SELLER owner | Đặt mua/hủy theo createdAt; DAY/WEEK/MONTH |
| GET | `/api/orders/seller/shops/{shopId}/reports/sales?from=2026-01-01&to=2026-12-31&groupBy=MONTH&variantPage=0&variantSize=20` | SELLER owner | Đơn DELIVERED theo updatedAt; DAY/WEEK/MONTH/YEAR |

Cả hai trả ApiResponse với data chứa shopId/from/to/groupBy/timezone, summary và periods. from/to bắt buộc dạng YYYY-MM-DD, bao gồm ngày cuối theo giờ Việt Nam; tuần thứ Hai đến Chủ nhật. Kỳ trống trả 0. Phạm vi tối đa 3661 ngày. Không sở hữu shop trả 403; không xác minh được quyền trả 503; sai tham số trả 400. Matcher Security và route Kong `/api/orders/**` hiện tại đã bao phủ các API này.

- Demand: totalOrders/nonCancelledOrders/cancelledOrders, totalQuantity/nonCancelledQuantity/cancelledQuantity và cancellationRate (%). Số đơn khác số buyer duy nhất; không phải đếm số thao tác hủy trong kỳ. Mặc định groupBy=DAY.
- Sales: deliveredOrders/soldQuantity/revenue/shippingFee/totalOrderAmount. revenue là tổng tiền hàng orders.subtotal, chưa gồm vận chuyển; totalOrderAmount là tổng orders.totalAmount. Response có dateBasis=updatedAt. Mặc định groupBy=MONTH.
- Sales trả thêm bestSellingVariants (productId/productName/variantId/variantName/soldQuantity/revenue) cho toàn bộ khoảng ngày; tên lấy từ snapshot order_items, không gọi catalog và không có attributesJson. Phân trang riêng qua variantPage (từ 0), variantSize (1–100); response có totalVariants/totalVariantPages. Summary và periods không phụ thuộc trang variant.
- updatedAt là giải pháp tạm: cập nhật đơn DELIVERED sau này có thể dịch số liệu sang kỳ khác. Các biên periodStart/periodEnd là kỳ lịch, nhưng số liệu chỉ nằm trong from/to đã yêu cầu.

### Product Review (Order service)

Review được lưu riêng trong bảng `product_reviews` của order_db. Không gọi product/auth để xác minh mua hàng và không upload file vào Order service. Route Kong `/api/reviews` chuyển tiếp về order_service (port 8084).

| HTTP | Route | Quyền | Kết quả |
|---|---|---|---|
| POST | `/api/reviews` | BUYER | HTTP 201, ApiResponse chứa đánh giá vừa tạo |
| GET | `/api/reviews/products/{productId}?page=0&size=20` | Công khai | HTTP 200, ApiResponse chứa trang review và trung bình sao |

#### Tạo review

```json
{
  "orderId": "11111111-1111-4111-8111-111111111111",
  "productId": "22222222-2222-4222-8222-222222222222",
  "rating": 5,
  "comment": "Sản phẩm tốt, giao hàng đúng hẹn.",
  "mediaUrls": [
    "http://localhost:8000/media/reviews/11111111-1111-4111-8111-111111111111/images/33333333-3333-4333-8333-333333333333.jpg"
  ]
}
```

- orderId/productId là UUID bắt buộc; không nhận variantId, buyerId hoặc buyerName từ client để quyết định quyền/người đánh giá.
- Buyer được xác định từ subject của JWT. orderId phải tồn tại, thuộc Buyer hiện tại, có orderStatus=DELIVERED và có ít nhất một OrderItem mang productId. Cả bốn trường hợp vi phạm trả **403**, message: `Bạn chưa mua sản phẩm này hoặc đơn hàng chưa hoàn thành`.
- Mỗi orderId + productId chỉ được đánh giá một lần, kể cả đơn có nhiều variant của cùng product. Đánh giá trùng trả **409**, message: `Bạn đã đánh giá sản phẩm này trong đơn hàng này`. Mua cùng product ở đơn khác vẫn có thể đánh giá.
- rating bắt buộc là số nguyên 1–5. comment bắt buộc, không chỉ có khoảng trắng, tối đa 2000 ký tự trước khi bỏ khoảng trắng hai đầu. Sai dữ liệu trả 400.
- mediaUrls là mảng URL HTTP/HTTPS tuyệt đối, mỗi URL tối đa 2048 ký tự, không chứa user/password trong URL. Bỏ qua hoặc null được chuẩn hóa thành []; không truyền file nhị phân. Order không tải URL về, không xác minh file tồn tại và không gọi Media service.
- buyerName lưu tên hiển thị từ claim `name` trong JWT đã xác thực; nếu thiếu/rỗng dùng `Người mua`. JWT Auth hiện chưa phát hành claim name, nên hiện dùng tên mặc định. Không dùng email làm tên công khai và không gọi Auth để lấy tên.

Frontend upload từng file qua `/api/media/upload` trước (có thể dùng domain=reviews, entityId=orderId), lấy trường url và gửi vào mediaUrls. Sau khi review được tạo, response trả lại mảng URL để dùng trong img/video. Chú ý vòng đời media bên dưới.

#### Response danh sách

```json
{
  "message": "Thành công",
  "data": {
    "items": [
      {
        "id": "44444444-4444-4444-8444-444444444444",
        "productId": "22222222-2222-4222-8222-222222222222",
        "buyerName": "Người mua",
        "rating": 5,
        "comment": "Sản phẩm tốt, giao hàng đúng hẹn.",
        "mediaUrls": [],
        "createdAt": "2026-11-11T10:00:00Z",
        "updatedAt": "2026-11-11T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "averageRating": 5.0
  },
  "timestamp": "2026-11-11T10:00:00Z"
}
```

Response tạo review dùng cùng đối tượng review trong data, không bọc thêm items. Không công khai buyerId/orderId trong response. Danh sách sắp xếp createdAt giảm dần, tiếp đến id giảm dần để ổn định phân trang; page bắt đầu từ 0, size mặc định 20, tối đa 100. averageRating tính trên **toàn bộ review của product**, không chỉ trang hiện tại; totalElements là tổng số review. Product chưa có review (hoặc chưa xuất hiện trong DB Order) trả items=[], totalElements=0, totalPages=0, averageRating=0.0. Trang vượt cuối trả items=[] nhưng giữ tổng số và trung bình của product. API không xác minh product tồn tại bên Product service.

Schema dev dùng Hibernate ddl-auto=update để tạo bảng. Với môi trường quản lý schema thủ công, dùng `services/order_service/db/create-product-reviews.sql` trên order_db. Bảng có unique constraint (order_id, product_id), check rating/comment và index cho phân trang theo product.

**Giới hạn tích hợp media hiện tại:** MediaCleanupJob của Product chỉ thu thập URL từ Product/Variant/Shop, chưa thu thập product_reviews trong DB Order. Vì vậy file chỉ được review tham chiếu có thể bị dọn sau 24 giờ. Cần chính sách giữ media review riêng trước khi dùng lâu dài; việc lưu URL trong Review không tự làm job Media giữ file. Tính năng review này không thêm internal API, truy cập DB chéo service hoặc sửa module Media.

### Inventory

Khách chỉ nhận `variantId` và `availableQuantity`. Tồn quản trị có `onHandQuantity`, `reservedQuantity`, `availableQuantity`. Thao tác giữ/chốt/nhả thủ công dành cho Admin trên fixture riêng.

| HTTP | Route | Quyền | Use case | Body mẫu |
|---|---|---|---|---|
| GET | `/api/inventories/variant/{variantId}` | Công khai | Số lượng còn bán | — |
| GET | `/api/inventories/seller?shopId={shopId}` | SELLER | Seller xem kho theo shop | — |
| GET | `/api/inventories/seller/{inventoryId}` | SELLER | Seller xem chi tiết kho | — |
| GET | `/api/inventories/seller/variants/{variantId}` | SELLER | Seller xem kho theo biến thể | — |
| PATCH | `/api/inventories/seller/variants/{variantId}/stock` | SELLER | Seller điều chỉnh tồn | `adjust` |
| GET | `/api/inventories/admin?shopId={shopId}` | ADMIN | Admin xem kho theo shop | — |
| GET | `/api/inventories/admin/{inventoryId}` | ADMIN | Admin xem chi tiết kho | — |
| GET | `/api/inventories/admin/variants/{variantId}` | ADMIN | Admin xem kho theo biến thể | — |
| POST | `/api/inventories/admin` | ADMIN | Khởi tạo kho thủ công | `inventory` |
| PATCH | `/api/inventories/admin/increase-stock` | ADMIN | Tăng tồn | `increase` |
| POST | `/api/reservations/admin` | ADMIN | Tạo giữ chỗ thủ công | `reserve` |
| GET | `/api/reservations/admin?orderId={orderId}` | ADMIN | Danh sách giữ chỗ theo đơn | — |
| GET | `/api/reservations/admin/{reservationId}` | ADMIN | Chi tiết giữ chỗ | — |
| POST | `/api/reservations/admin/{reservationId}/confirm` | ADMIN | Chốt giữ chỗ thủ công | — |
| POST | `/api/reservations/admin/{reservationId}/release` | ADMIN | Nhả giữ chỗ thủ công | — |

### Shipping

API vận đơn dành cho Admin. Shipping nhận `OrderShipped` qua Kafka để tạo vận đơn và tự cập nhật trạng thái trong môi trường mô phỏng.

| HTTP | Route | Quyền | Use case | Body mẫu |
|---|---|---|---|---|
| POST | `/api/shipments/admin` | ADMIN | Tạo vận đơn thủ công | `shipment` |
| GET | `/api/shipments/admin?orderId={orderId}` | ADMIN | Tra vận đơn theo đơn | — |
| GET | `/api/shipments/admin/{trackingCode}` | ADMIN | Tra vận đơn theo mã | — |

## Body mẫu

Các tên trong cột Body mẫu tham chiếu danh sách này. Thay giá trị trong dấu `{}` bằng dữ liệu thực. `attributesJson` và `mediaJson` là **chuỗi JSON** theo DTO hiện tại.

```json
{
  "register": {
    "email": "{newBuyerEmail}",
    "password": "{buyerPassword}",
    "phone": "0912345678"
  },
  "forgot": {
    "email": "{buyerEmail}"
  },
  "password": {
    "oldPassword": "{buyerPassword}",
    "newPassword": "{newPassword}"
  },
  "refresh": {
    "refreshToken": "{buyerRefreshToken}"
  },
  "address": {
    "receiverName": "Người nhận test",
    "phone": "0912345678",
    "location": "TP. Hồ Chí Minh",
    "detail": "12 Đường Test",
    "isDefault": true
  },
  "verify": {
    "variantIds": [
      "{variantId}"
    ]
  },
  "shop": {
    "name": "Shop Postman",
    "slug": "{shopSlug}",
    "description": "Dữ liệu test",
    "address": "TP. Hồ Chí Minh"
  },
  "category": {
    "name": "Danh mục test",
    "slug": "{categorySlug}",
    "displayOrder": 0
  },
  "status": {
    "status": "ACTIVE"
  },
  "product": {
    "categoryId": "{categoryId}",
    "name": "Sản phẩm Postman",
    "slug": "{productSlug}",
    "description": "Dữ liệu test",
    "mediaJson": "[]",
    "variants": [
      {
        "sku": "{sku}",
        "price": 125000,
        "attributesJson": "{\"color\":\"Blue\"}",
        "active": true,
        "imageUrl": "https://example.com/variants/blue.jpg"
      }
    ]
  },
  "variant": {
    "sku": "{sku}",
    "price": 125000,
    "attributesJson": "{\"color\":\"Blue\"}",
    "active": true,
    "imageUrl": "https://example.com/variants/blue.jpg"
  },
  "decision": {
    "reason": "Kiểm thử Postman"
  },
  "adjust": {
    "newQuantity": 100
  },
  "inventory": {
    "productId": "{productId}",
    "variantId": "{variantId}",
    "shopId": "{shopId}"
  },
  "increase": {
    "variantId": "{variantId}",
    "quantity": 5
  },
  "reserve": {
    "orderId": "{manualOrderId}",
    "variantId": "{variantId}",
    "quantity": 1
  },
  "checkout": {
    "paymentMethod": "COD",
    "shippingAddress": "12 Đường Test, TP. Hồ Chí Minh",
    "items": [
      {
        "variantId": "{variantId}",
        "quantity": 1
      }
    ]
  },
  "shipment": {
    "orderId": "{manualOrderId}",
    "shippingAddress": "12 Đường Test, TP. Hồ Chí Minh"
  },
  "login": {
    "email": "<email>",
    "password": "<password>"
  }
}
```

## Chạy Postman

1. Import `postman/Multivendor.postman_collection.json` và `postman/Multivendor.Local.postman_environment.json`, rồi chọn Environment **Multivendor Local Dev**.
2. Điền thông tin đăng nhập Buyer, Seller và Admin trong Environment local. Dùng shop đang hoạt động do Seller sở hữu và một biến thể đang bán, thuộc sản phẩm đã duyệt, có tồn khả dụng ít nhất 1. Điền `shopId` và `variantId`. Khởi động các service, PostgreSQL, Redis và Kafka theo cấu hình dev.
3. Chạy **một** thư mục con trong **Tổng hợp** bằng Collection Runner, 1 iteration, delay khoảng **1500 ms** mỗi request. Happy Path tạo đơn COD, chờ xác nhận, giao hàng rồi đối chiếu tồn. Luồng hủy tạo checkout riêng, chờ xác nhận rồi đối chiếu nhả giữ chỗ. Các bước chờ thử tối đa 20 lần. Dùng biến thể test riêng để số tồn không bị request khác thay đổi.
4. Đăng nhập Buyer trước khi chạy thư mục phân quyền. Các ví dụ 403 giả định tài khoản Buyer không có thêm role Seller/Admin.
5. Với các thư mục Auth, Product, Inventory, Order và Shipping, chạy từng request phù hợp với trạng thái dữ liệu. Một số thao tác loại trừ nhau, ví dụ duyệt hoặc từ chối, chốt hoặc nhả cùng một giữ chỗ. Không chạy cả collection như một luồng duy nhất. Thao tác xóa người dùng dùng `deleteUserId` riêng. Luồng quên mật khẩu có thể gửi email và đổi mật khẩu, chỉ dùng tài khoản test.
6. Token và ID được ghi vào Environment sau khi request thành công. Khi thử checkout riêng ngoài Runner, xóa `checkoutKey` để bắt đầu lần đặt mới. Với các request thủ công cần `manualOrderId`, nhập một UUID riêng cho fixture.

Collection mô tả các API HTTP hiện có. Hai luồng tổng hợp kiểm tra Kafka gián tiếp qua trạng thái đơn, vận đơn và tồn kho.


## API Admin phân trang đơn hàng và tổng hợp doanh thu — 14/11/2026

Hai API mới dưới đây yêu cầu JWT role ADMIN theo SecurityConfig hiện có. API tra cứu mảng cũ vẫn giữ tương thích; frontend mới không dùng chúng để tải toàn bộ đơn phục vụ báo cáo. Không thêm REST nội bộ sang service khác.

### 1. GET `/api/orders/admin/page`

Danh sách toàn hệ thống nếu không truyền bộ lọc. Các bộ lọc kết hợp bằng AND:

| Query | Kiểu / mặc định | Ý nghĩa |
| --- | --- | --- |
| `page`, `size` | integer; 0, 20 | Trang từ 0, size 1–100; offset không vượt Integer.MAX_VALUE |
| `orderId`, `checkoutId`, `shopId`, `buyerId` | UUID; tùy chọn | Lọc khớp chính xác từng mã |
| `status` | OrderStatus; tùy chọn | PENDING, PENDING_PAYMENT, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED |
| `paymentStatus` | PaymentStatus; tùy chọn | UNPAID, PENDING, PAID, FAILED, REFUND_PENDING |
| `paymentMethod` | PaymentMethod; tùy chọn | COD, VNPAY, MOCK, ATM |
| `from`, `to` | YYYY-MM-DD; tùy chọn | Ngày bắt đầu/kết thúc, có thể chỉ truyền một đầu; inclusive theo Asia/Ho_Chi_Minh |
| `dateBasis` | CREATED_AT (mặc định) hoặc UPDATED_AT | Trường thời gian dùng lọc và sort |
| `direction` | DESC (mặc định) hoặc ASC | Thứ tự ngày, thêm id cùng chiều để ổn định phân trang |
| `q` | string <=150 ký tự | Tìm không phân biệt hoa/thường trong UUID dạng text, shippingAddress, productName/variantName snapshot; không tìm tên shop/buyer hoặc SKU vì không có trong DB order |

Ví dụ: `/api/orders/admin/page?page=0&size=20&status=DELIVERED&from=2026-11-01&to=2026-11-14&direction=DESC`.

`ApiResponse.data`:

```json
{"items": [], "page": 0, "size": 20, "totalElements": 0, "totalPages": 0}
```

Mỗi phần tử items là **OrderResult trực tiếp**, có đầy đủ items sản phẩm và các trường giống GET chi tiết admin, không bọc thêm `{order: ...}` như Seller page. Server đếm/lọc/sort/phân trang trước khi đọc order_items cho đúng các đơn trong trang (một query batch); không phân trang trên join làm lặp/mất đơn. Từ khóa tìm chuỗi con ILIKE, escape ký tự %, _ và !; không cam kết tìm bỏ dấu. Trang vượt cuối trả items rỗng và metadata thật, UI lùi về trang hợp lệ.

### 2. GET `/api/orders/admin/reports/revenue`

Query: `from`, `to` bắt buộc (YYYY-MM-DD), `shopId` tùy chọn UUID, `page=0`, `size=10` (1–100). Khoảng ngày hợp lệ, năm 1–9998, tối đa 3661 ngày. Toàn bộ ngày kết thúc theo giờ Việt Nam được chuyển thành điều kiện `[start, endExclusive)`.

Chỉ tổng hợp đơn **DELIVERED**, tạm dùng **updatedAt**. Tiền hàng = SUM(subtotal), phí vận chuyển = SUM(shippingFee), tổng đơn = SUM(totalAmount). Phân chia tổng giá trị đơn theo paymentStatus=PAID và khác PAID; đây không phải thực thu ngân hàng hoặc số tiền phải trả shop.

```json
{
  "from": "2026-11-01", "to": "2026-11-14",
  "timezone": "Asia/Ho_Chi_Minh", "dateBasis": "updatedAt",
  "summary": {
    "deliveredOrders": 0, "paidOrders": 0, "revenue": 0,
    "shippingFee": 0, "totalOrderAmount": 0,
    "paidOrderAmount": 0, "unpaidOrderAmount": 0
  },
  "items": [], "page": 0, "size": 10, "totalElements": 0, "totalPages": 0
}
```

`items` là `{shopId, summary}` cho từng shop có ít nhất một đơn DELIVERED khớp kỳ, sort revenue DESC rồi shopId ASC; shop không phát sinh đơn phù hợp không xuất hiện. `totalElements` là số shop khớp kỳ, không phải số đơn hoặc tổng shop hệ thống. **summary ngoài cùng luôn tổng toàn bộ phạm vi lọc**, độc lập page/size. Lọc shopId sẽ giới hạn cả summary lẫn items. Tiền tổng hợp dùng PostgreSQL numeric/Java BigDecimal; frontend kiểm tra giới hạn số nguyên an toàn trước hiển thị.

Backend chỉ query orders, không gọi product/auth và không tải order_items để tính doanh thu. Tên shop được frontend bổ sung bằng API admin shop cho tối đa các shop trong trang; thiếu tên không ảnh hưởng số tiền. Dialog các đơn cấu thành gọi API `/page` với shopId/status=DELIVERED/dateBasis=UPDATED_AT và cùng from/to; có thể thay bộ lọc để xem trạng thái khác.

Count và dữ liệu mỗi response đọc trong transaction read-only REPEATABLE_READ. Các request ở các trang hoặc request mở dialog khác nhau vẫn có thể thấy cập nhật mới; đây chưa phải bản chốt kế toán. Chưa có deliveredAt bất biến, hoa hồng, hoàn tiền, công nợ hoặc chốt/chuyển tiền.

Lỗi tham số UUID/enum/ngày/page/size/q trả HTTP 400; thiếu đăng nhập hoặc sai quyền do security xử lý. Migration mới `V9__admin_order_query_indexes.sql` bổ sung index cho ngày và báo cáo DELIVERED; không sửa migration/seed cũ. Chỉ dựng code/tài liệu, chưa chạy build/test hoặc thực thi migration.


## Vận hành health và Media hiện tại

GET trực tiếp `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` trên port mỗi service không cần token, chỉ expose health và không trả chi tiết. Readiness gồm application readiness + db, Auth thêm redis; không xác nhận mọi dependency nghiệp vụ. Gateway hiện không route /actuator. Chế độ hạ tầng cho IDE dùng docker-compose.infrastructure.yml; full Docker dùng file base + apps. Xem DOCKER-DEVELOPMENT.md.

Thiết kế Media cũ dùng media object/MediaStoragePort/staging không phải implementation hiện tại. MediaController dùng LocalMediaStorageService và mediaJson chuỗi mảng URL/imageUrl; MediaCleanupJob hiện không chạy cleanup. Không tự suy luận có API xóa upload hoặc giới hạn 10 ảnh/1 video backend từ thiết kế cũ. Khi gỡ ảnh cần giữ tham chiếu lịch sử trong order.

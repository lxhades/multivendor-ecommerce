# Danh mục API Backend

Đối chiếu tĩnh với controller và cấu hình Kong trong workspace hiện tại. Routes below are relative to the Kong proxy (Compose defaults to port 8000). **Routed** means the checked-in Kong config forwards the path. Gateway exposure below reflects the checked-in configuration. Runtime reachability requires Kong and the upstream services to be running. Authentication is enforced by Spring. Kong's sample JWT plugin is commented out.

[Gateway config](../../backend/multivendor/kong-gateway/config/kong.yml) · [Compose](../../backend/multivendor/docker-compose.yml) · [Backend service sources](../../backend/multivendor/services)

[Tra các trường trong response data](#các-trường-trong-response-data)

## Reading the tables

- All successful responses are `ApiResponse<T>` unless marked raw. Its fields are `message`, `timestamp` and `data`. Null fields are omitted, including `data` on `Void` responses. `T[]` means a list. `Page<ShopResult>` is Spring Data's page serialization with `content` and pagination metadata. product_service enables VIA_DTO: metadata is nested at `data.page` (`number`, `size`, `totalElements`, `totalPages`); shop API adapters normalize those fields for UI consumers. Pageable query parameters are `page`, `size`, and `sort`.
- Success is HTTP 200 unless shown as 201. A dash means no request body or parameters beyond the path. DTO names refer to the Java wire types, including nested types. IDs used in paths and filters are UUIDs except auth's string-bound IDs.
- Public means no bearer token required. Authenticated means a valid `Authorization: Bearer <accessToken>`. Role labels require that role and may also require resource ownership. Refresh and logout take the refresh token in JSON.

## Auth and account · Routed

| Method | Path | Mục đích | Input | Response data | Access |
| --- | --- | --- | --- | --- | --- |
| POST | /auth/register | Đăng ký tài khoản mua hàng | RegisterRequest | 201 RegisterUserResult | Public |
| POST | /auth/login | Đăng nhập và cấp token | LoginRequest | LoginUserResult | Public |
| GET | /auth/me | Lấy hồ sơ của phiên hiện tại | — | CurrentUserResult | Authenticated |
| PUT | /auth/forgotpassword | Gửi mật khẩu mới qua email | ForgotpasswordRequest | Void | Public |
| PUT | /auth/changepassword | Đổi mật khẩu | ChangePasswordRequest | Void | Authenticated |
| POST | /auth/refresh | Làm mới cặp token | RefreshTokenRequest | RefreshTokenResult | Public, refresh token |
| POST | /auth/logout | Thu hồi refresh token | RefreshTokenRequest | Void | Public, refresh token |
| POST | /auth/me/register-seller | Đăng ký vai trò người bán | — | Void | BUYER |
| POST | /auth/registerAdmin | Cấp vai trò quản trị | Query `userId` | Void | **Public privilege escalation. Exclude from UI.** |
| GET | /auth/users/{userId}/addresses | Liệt kê địa chỉ đã lưu | — | AddressResult[] | Authenticated, same user |
| POST | /auth/users/{userId}/addresses | Thêm địa chỉ | AddAddressRequest | 201 AddressResult | Authenticated, same user |
| PUT | /auth/users/{userId}/addresses/{addressId} | Sửa địa chỉ | UpdateAddressRequest | AddressResult | Authenticated, same user |
| DELETE | /auth/users/{userId}/addresses/{addressId} | Xóa địa chỉ | — | Void | Authenticated, same user |

`RegisterRequest` carries `email`, `password`, `phone`. `LoginRequest` carries `email`, `password`. Forgot password takes `email`, and change password takes `oldPassword` and `newPassword`. `LoginUserResult` contains `userId`, `accessToken`, `refreshToken`. `RefreshTokenRequest` contains `refreshToken` and `RefreshTokenResult` returns both tokens. `CurrentUserResult` contains `userId`, `email`, `phone`, `status`, `role`. Address write models contain `receiverName`, `phone`, `location`, `detail` and a default-address boolean declared as `isDefault` in Java. Verify the Lombok/Jackson JSON property with a runtime contract before binding that control.

## Shop and category · Routed

| Method | Path | Mục đích | Input | Response data | Access |
| --- | --- | --- | --- | --- | --- |
| GET | /shops | Liệt kê cửa hàng | Pageable | Page<PublicShopResult> | Public |
| GET | /shops/{shopId} | Xem cửa hàng | — | PublicShopResult | Public |
| GET | /categories | Liệt kê danh mục | — | CreateCategoryResult[] | Public |
| GET | /categories/{id} | Xem danh mục | — | CreateCategoryResult | Public |
| GET | /categories/{parentId}/children | Liệt kê danh mục con | — | CreateCategoryResult[] | Public |

## Product · Routed

| Method | Path | Mục đích | Input | Response data | Access |
| --- | --- | --- | --- | --- | --- |
| GET | /api/products | Liệt kê sản phẩm công khai | Optional `shopId`, `categoryId`, `includeChildren=false` | ProductDtos.Buyer[] | Public |
| GET | /api/products/{id} | Xem sản phẩm công khai | — | ProductDtos.Buyer | Public |
| GET | /api/products/search | Tìm kiếm và lọc sản phẩm | Optional `q`, `shopId`, `categoryId`, `includeChildren`, `minPrice`, `maxPrice`, `sort`, `page`, `size` | BuyerSearch.Page | Public |

## Checkout and buyer orders · Routed

| Method | Path | Mục đích | Input | Response data | Access |
| --- | --- | --- | --- | --- | --- |
| POST | /api/checkouts | Tạo phiên checkout và các đơn theo cửa hàng | CreateCheckoutRequest and required `Idempotency-Key` header | 201 CreateCheckoutResult | BUYER |
| GET | /api/checkouts/{id} | Xem trạng thái checkout | — | CheckoutResult | BUYER owner or ADMIN |
| GET | /api/orders | Liệt kê đơn của người mua | Optional `buyerId` matching current buyer | OrderResult[] | BUYER |
| GET | /api/orders?checkoutId={checkoutId} | Liệt kê đơn theo phiên checkout | `checkoutId` | OrderResult[] | BUYER |
| GET | /api/orders/{id} | Xem chi tiết đơn của người mua | — | OrderResult | BUYER owner |
| POST | /api/orders/{id}/cancel | Hủy đơn của người mua | — | OrderResult | BUYER owner |

`CreateCheckoutRequest` has `paymentMethod`, nonblank `shippingAddress` string and nonempty `items` of `{variantId, quantity}` with positive integer quantities. `CreateCheckoutResult` holds `checkout: CheckoutResult` and `orders: OrderResult[]`. [Checkout models](../../backend/multivendor/services/order_service/src/main/java/com/order_service/application/checkout/dto) · [Order model](../../backend/multivendor/services/order_service/src/main/java/com/order_service/application/order/dto/OrderResult.java)

Ví dụ `data` trả về từ API chi tiết đơn Buyer, Seller hoặc Admin (giá trị minh họa):

```json
{
  "id": "<order UUID>",
  "checkoutId": "<checkout UUID>",
  "buyerId": "<buyer UUID>",
  "shopId": "<shop UUID>",
  "shippingAddress": "Tên người nhận\n0901234567\nĐịa chỉ giao hàng",
  "items": [
    {
      "id": "<order item UUID>",
      "productId": "<product UUID>",
      "variantId": "<variant UUID>",
      "productName": "Áo thun",
      "unitPrice": 150000,
      "quantity": 2
    }
  ],
  "subtotal": 300000,
  "shippingFee": 20000,
  "totalAmount": 320000,
  "orderStatus": "CONFIRMED",
  "paymentStatus": "UNPAID",
  "paymentMethod": "COD",
  "createdAt": "2026-11-10T03:00:00Z",
  "updatedAt": "2026-11-10T03:01:00Z"
}
```

Tên và điện thoại người nhận có thể nằm trong chuỗi `shippingAddress` tùy cách đặt đơn. `OrderResult` không có trường riêng cho hai giá trị này hoặc mã vận đơn.

## Seller · Routed

All rows require SELLER and apply ownership checks.

| Method | Path | Mục đích | Input | Response data |
| --- | --- | --- | --- | --- |
| POST | /api/shops/seller | Tạo cửa hàng | CreateShopRequest | 201 ShopResult |
| GET | /api/shops/seller/my-shop | Xem cửa hàng của phiên Seller | — | ShopResult |
| PUT | /api/shops/seller/{shopId} | Sửa thông tin cửa hàng | UpdateShopRequest | ShopResult |
| DELETE | /api/shops/seller/{shopId} | Đóng cửa hàng | — | Void, closes shop |
| PATCH | /api/shops/seller/{shopId} | Mở lại cửa hàng | — | Void, reopens shop |
| GET | /api/products/seller/shops/{shopId} | Liệt kê sản phẩm của cửa hàng | — | ProductDtos.Seller[] |
| POST | /api/products/seller/shops/{shopId} | Tạo sản phẩm | ProductDtos.ProductInput | ProductDtos.Seller |
| GET | /api/products/seller/shops/{shopId}/{id} | Xem sản phẩm của cửa hàng | — | ProductDtos.Seller |
| PUT | /api/products/seller/shops/{shopId}/{id} | Sửa sản phẩm | ProductDtos.ProductInput | ProductDtos.Seller |
| DELETE | /api/products/seller/shops/{shopId}/{id} | Gỡ sản phẩm | — | Void |
| PATCH | /api/products/seller/shops/{shopId}/{id}/status | Đổi trạng thái bán | ProductDtos.StatusInput | ProductDtos.Seller |
| POST | /api/products/seller/shops/{shopId}/{id}/variants | Thêm sản phẩm con | ProductDtos.VariantInput | ProductDtos.Seller |
| PUT | /api/products/seller/shops/{shopId}/{id}/variants/{variantId} | Sửa sản phẩm con | ProductDtos.VariantInput | ProductDtos.Seller |
| DELETE | /api/products/seller/shops/{shopId}/{id}/variants/{variantId} | Gỡ sản phẩm con | — | ProductDtos.Seller |
| GET | /api/orders/seller/shops/{shopId} | Lọc và phân trang đơn của cửa hàng | `page=0`, `size=20`, optional ISO dates `from`, `to`, `status` (OrderStatus) | ListSellerOrdersUseCase.Result |
| GET | /api/orders/seller?checkoutId={checkoutId} | Liệt kê đơn theo phiên checkout | `checkoutId` | OrderResult[] |
| GET | /api/orders/seller?shopId={shopId} | Liệt kê đơn theo cửa hàng | `shopId` | OrderResult[] |
| GET | /api/orders/seller/{id} | Xem chi tiết đơn của cửa hàng | — | OrderResult |
| POST | /api/orders/seller/{id}/cancel | Hủy đơn của cửa hàng | — | OrderResult |
| POST | /api/orders/seller/{id}/ship | Ghi nhận gửi hàng | — | OrderResult |

[Product DTOs](../../backend/multivendor/services/product_service/src/main/java/com/product_service/application/product/dto/ProductDtos.java) · [Shop and category request DTOs](../../backend/multivendor/services/product_service/src/main/java/com/product_service/adapter/in/web/dto/request)

## Inventory · Routed

| Method | Path | Mục đích | Input | Response data | Access |
| --- | --- | --- | --- | --- | --- |
| GET | /api/inventories/variant/{variantId} | Xem số lượng khả dụng | — | AvailableStock (`variantId`, `availableQuantity`) | Public |
| GET | /api/inventories/seller | Liệt kê tồn kho của cửa hàng | Query `shopId` | InventoryResponse[] | SELLER owner |
| GET | /api/inventories/seller/{id} | Xem bản ghi tồn kho | — | InventoryResponse | SELLER owner |
| GET | /api/inventories/seller/variants/{variantId} | Tra tồn kho theo sản phẩm con | — | InventoryResponse | SELLER owner |
| PATCH | /api/inventories/seller/variants/{variantId}/stock | Đặt tổng số lượng tồn thực tế | AdjustStockRequest | InventoryResponse | SELLER owner |
| GET | /api/inventories/admin | Liệt kê tồn kho theo cửa hàng | Query `shopId` | InventoryResponse[] | ADMIN |
| GET | /api/inventories/admin/{id} | Xem bản ghi tồn kho | — | InventoryResponse | ADMIN |
| GET | /api/inventories/admin/variants/{variantId} | Tra tồn kho theo sản phẩm con | — | InventoryResponse | ADMIN |
| POST | /api/inventories/admin | Khởi tạo tồn kho | InitializeInventoryCommand | 201 InventoryResponse | ADMIN |
| PATCH | /api/inventories/admin/increase-stock | Tăng số lượng tồn | IncreaseStockCommand | InventoryResponse | ADMIN |
| GET | /api/reservations/admin | Liệt kê giữ chỗ theo đơn | Query `orderId` | ReservationResponse[] | ADMIN |
| GET | /api/reservations/admin/{id} | Xem bản ghi giữ chỗ | — | ReservationResponse | ADMIN |
| POST | /api/reservations/admin | Giữ tồn kho cho đơn | ReserveStockCommand | 201 ReservationResponse | ADMIN |
| POST | /api/reservations/admin/{id}/confirm | Xác nhận giữ chỗ | — | ReservationResponse | ADMIN |
| POST | /api/reservations/admin/{id}/release | Giải phóng giữ chỗ | — | ReservationResponse | ADMIN |

Seller có thể điều chỉnh tồn thực tế bằng `PATCH /api/inventories/seller/variants/{variantId}/stock` với `{ "newQuantity": number }`.

## Admin · Routed

All rows require ADMIN. Inventory and reservations are in the preceding table.

| Method | Path | Mục đích | Input | Response data |
| --- | --- | --- | --- | --- |
| GET | /api/users/admin | Tìm và phân trang tài khoản | Optional `keyword`, `page=0`, `size=20` | AdminUserListResult |
| GET | /api/users/admin/{userId} | Xem tài khoản | — | AdminUserResult |
| PUT | /api/users/admin/{userId} | Sửa thông tin liên hệ | `{email, phone}` | AdminUserResult |
| PATCH | /api/users/admin/{userId}/status | Cấm/bỏ cấm | `{status: "ACTIVE" hoặc "LOCKED"}` | AdminUserResult |
| DELETE | /api/users/admin/{userId} | Xóa tài khoản, không cho tự xóa | — | Void |
| GET | /api/shops/admin | Liệt kê cửa hàng | Pageable. Optional `sellerId` is accepted but ignored | Page<ShopResult> |
| GET | /api/shops/admin/{shopId} | Xem cửa hàng | — | ShopResult |
| PATCH | /api/shops/admin/{shopId}/status | Đổi trạng thái cửa hàng | Query `shopStatus` (ShopStatus) | ShopResult |
| DELETE | /api/shops/admin/{shopId} | Đóng cửa hàng | — | Void, owner check still applies |
| POST | /api/categories/admin | Tạo danh mục | CreateCategoryRequest | 201 CreateCategoryResult |
| PUT | /api/categories/admin/{id} | Sửa danh mục | UpdateCategoryRequest | CreateCategoryResult |
| PATCH | /api/categories/admin/{id}/status | Đổi trạng thái danh mục | ChangeStatusCategoryRequest | CreateCategoryResult |
| DELETE | /api/categories/admin/{id} | Vô hiệu hóa danh mục | — | Void |
| GET | /api/products/admin/pending | Liệt kê sản phẩm chờ duyệt | — | ProductDtos.Seller[] |
| GET | /api/products/admin/{id} | Xem sản phẩm để kiểm duyệt | — | ProductDtos.Seller |
| POST | /api/products/admin/{id}/approve | Duyệt sản phẩm | Optional ProductDtos.DecisionInput | ProductDtos.Seller |
| POST | /api/products/admin/{id}/reject | Từ chối sản phẩm | ProductDtos.DecisionInput | ProductDtos.Seller |
| GET | /api/orders/admin/{orderId} | Xem chi tiết đơn | — | OrderResult |
| GET | /api/orders/admin?checkoutId={checkoutId} | Liệt kê đơn theo checkout | `checkoutId` | OrderResult[] |
| GET | /api/orders/admin?shopId={shopId} | Liệt kê đơn theo cửa hàng | `shopId` | OrderResult[] |
| GET | /api/orders/admin?buyerId={buyerId} | Liệt kê đơn theo người mua | `buyerId` | OrderResult[] |
| POST | /api/orders/admin/{orderId}/cancel | Hủy đơn | — | OrderResult |
| POST | /api/orders/admin/{orderId}/ship | Ghi nhận gửi hàng | — | OrderResult |
| POST | /api/shipments/admin | Tạo vận đơn | CreateShipmentRequest | 201 raw ShipmentResponse |
| GET | /api/shipments/admin/{trackingCode} | Tra vận đơn theo mã | — | Raw ShipmentResponse |
| GET | /api/shipments/admin?orderId={orderId} | Tra vận đơn theo đơn hàng | `orderId` | Raw ShipmentResponse |

Use one required filter at a time for the admin and seller order query variants. There is no unfiltered global admin order list.

## Payment and promotion

| Feature | Implementation |
| --- | --- |
| Payment | Checkout accepts a PaymentMethod and orders expose PaymentStatus. COD has a reservation-to-confirmation flow. There are no browser payment initiation, verification, callback, refund, or transaction endpoints. |
| Promotion | No coupon, voucher, campaign, or promotion REST controller. Checkout accepts no promo code and has no discount field. |

## API hạ tầng và nội bộ

| Method | Path | Mục đích | Input | Response |
| --- | --- | --- | --- | --- |
| GET | /auth/hello | Kiểm tra Auth Service phản hồi | — | Chuỗi thô `hello` |
| GET | /oauth2/jwks | Công bố khóa công khai để xác minh chữ ký JWT | — | JSON thô `{ "keys": [...] }` |
| POST | /internal/products/variants/verify | Đối chiếu sản phẩm con khi tạo đơn | `VerifyRequest` với `variantIds` | Mảng JSON thô `VerifiedVariant[]` |

Endpoint xác minh sản phẩm con dành cho giao tiếp giữa các dịch vụ. Các bảng liệt kê endpoint theo controller. Endpoint do framework sinh và khả năng truy cập khi triển khai phụ thuộc cấu hình runtime.

## Các trường trong response data

Tra tên kiểu ở cột **Response data** trong các bảng endpoint, rồi đối chiếu bên dưới. Hậu tố `[]` nghĩa là mảng các phần tử cùng kiểu. UUID, timestamp ISO-8601 và enum được biểu diễn bằng chuỗi JSON. Các trường tham chiếu có thể vắng mặt hoặc là `null` tùy dữ liệu và cấu hình serialize.

| Kiểu trả về | Trường JSON |
| --- | --- |
| `RegisterUserResult` | `userId`, `email`, `phone`, `status` (chuỗi) |
| `LoginUserResult` | `userId`, `accessToken`, `refreshToken` (chuỗi) |
| `RefreshTokenResult` | `accessToken`, `refreshToken` (chuỗi) |
| `CurrentUserResult`, `AdminUserResult` | `userId`, `email`, `phone` (chuỗi), `status` (UserStatus), `role` (Role) |
| `AdminUserListResult` | `users` (AdminUserResult[]), `page`, `size`, `totalElements`, `totalPages` (số nguyên) |
| `AddressResult` | `addressId` (số nguyên), `receiverName`, `phone`, `location`, `detail` (chuỗi), `isDefault` (boolean) |
| `ShopResult` | `id`, `sellerId` (UUID), `name`, `slug`, `logoUrl`, `bannerUrl`, `description`, `address`, `bankDetails` (chuỗi), `status` (ShopStatus), `ratingAvg` (số), `ratingCount`, `productCount` (số nguyên), `createdAt`, `updatedAt` (timestamp) |
| `PublicShopResult` | Các trường của ShopResult trừ `bankDetails`. |
| `Page<ShopResult>`, `Page<PublicShopResult>` | `content` (mảng DTO cửa hàng tương ứng), `page` (object chứa `size`, `number`, `totalElements`, `totalPages` là số nguyên) |
| `CreateCategoryResult` | `id`, `parentId` (UUID), `parentName` (chuỗi), `name`, `slug`, `description`, `imageUrl`, `status` (chuỗi), `displayOrder` (số nguyên), `createdAt`, `updatedAt` (timestamp) |
| `ProductDtos.Buyer` | `id`, `shopId`, `categoryId` (UUID), `name`, `slug`, `description`, `mediaJson` (chuỗi), `minPrice` (số), `soldCount` (số nguyên), `variants` (ProductDtos.Variant[]) |
| `ProductDtos.Seller` | `id`, `shopId`, `categoryId` (UUID), `name`, `slug`, `description`, `mediaJson` (chuỗi), `status` (ProductStatus), `approvalStatus` (ApprovalStatus), `minPrice` (số), `soldCount` (số nguyên), `deletedAt`, `createdAt`, `updatedAt` (timestamp), `variants` (ProductDtos.Variant[]), `histories` (ProductApprovalHistory[]) |
| `ProductDtos.Variant` | `id` (UUID), `sku`, `variantName`, `attributesJson`, `imageUrl` (chuỗi), `price` (số), `active` (boolean), `soldCount` (số nguyên), `deletedAt` (timestamp) |
| `ProductApprovalHistory` | `id`, `actorId` (UUID), `decision` (ApprovalStatus), `reason` (chuỗi), `createdAt` (timestamp) |
| `BuyerSearch.Page` | `items` (BuyerSearch.Item[]), `page`, `size`, `totalElements`, `totalPages` (số nguyên) |
| `BuyerSearch.Item` | `id`, `shopId`, `categoryId` (UUID), `name`, `shopName`, `categoryName`, `mediaJson` (chuỗi), `minPrice` (số) |
| `CreateCheckoutResult` | `checkout` (CheckoutResult), `orders` (OrderResult[]) |
| `CheckoutResult` | `id`, `buyerId` (UUID), `idempotencyKey` (chuỗi), `paymentMethod` (PaymentMethod), `status` (CheckoutStatus), `expiresAt` (timestamp) |
| `OrderResult` | `id`, `checkoutId`, `buyerId`, `shopId` (UUID), `shippingAddress` (chuỗi), `items` (OrderResult.Item[]), `subtotal`, `shippingFee`, `totalAmount` (số nguyên), `orderStatus` (OrderStatus), `paymentStatus` (PaymentStatus), `paymentMethod` (PaymentMethod), `createdAt`, `updatedAt` (timestamp) |
| `OrderResult.Item` | `id`, `productId`, `variantId` (UUID), `productName` (chuỗi), `unitPrice`, `quantity` (số nguyên) |
| `ListSellerOrdersUseCase.Result` | `items` (ListSellerOrdersUseCase.Item[]), `page`, `size`, `totalElements`, `totalPages` (số nguyên) |
| `ListSellerOrdersUseCase.Item` | `order` (OrderResult), `createdAt`, `updatedAt` (timestamp) |
| `AvailableStock` | `variantId` (UUID), `availableQuantity` (số nguyên) |
| `InventoryResponse` | `id`, `productId`, `variantId`, `shopId` (UUID), `onHandQuantity`, `reservedQuantity`, `availableQuantity` (số nguyên), `createdAt`, `updatedAt` (timestamp) |
| `ReservationResponse` | `id`, `orderId`, `inventoryId` (UUID), `quantity`, `version` (số nguyên), `status` (chuỗi), `createdAt`, `updatedAt` (timestamp) |
| `ShipmentResponse` (JSON thô) | `id`, `orderId` (UUID), `trackingCode`, `shippingAddress` (chuỗi), `status` (ShipmentStatus), `createdAt`, `shippedAt`, `deliveredAt` (timestamp) |
| `VerifiedVariant` (phần tử trong mảng JSON thô) | `productId`, `variantId`, `shopId` (UUID), `productName` (chuỗi), `unitPrice` (số nguyên), `available` (boolean) |
| JWKS (JSON thô) | `keys` (mảng khóa công khai JWK) |
| `Void` | Không có trường trong `data`. |

## Audit chuẩn hóa response

Danh mục 89 endpoint ở trên là bảng đối chiếu controller. Bảng dưới nhóm những endpoint dùng cùng response hoặc có cùng khoảng trống dữ liệu.

| Service | Endpoint | DTO hiện tại | Thiếu field hoặc rủi ro | Nguồn dữ liệu | Hướng sửa và trạng thái | Ưu tiên |
| --- | --- | --- | --- | --- | --- | --- |
| Product | `GET /shops`, `GET /shops/{shopId}` | PublicShopResult | DTO cũ có `bankDetails`; thiếu `sellerName` | Shop DB; tên thuộc Auth | Đã loại dữ liệu ngân hàng khỏi hai route công khai. Giữ ShopResult trên route bảo vệ. | P0 |
| Product | `GET /api/products/search` | BuyerSearch.Item | Thiếu tên cửa hàng và danh mục | Bảng shops, categories cùng Product DB | Đã bổ sung hai tên trong truy vấn trang kết quả, không gọi HTTP theo từng bản ghi. | P1 |
| Product | Buyer detail, Seller product CRUD, Admin moderation | ProductDtos.Buyer, ProductDtos.Seller, Variant, ProductApprovalHistory | Thiếu tên cửa hàng, danh mục và người kiểm duyệt; tên sản phẩm con trước đây chỉ suy ra ở UI | Product DB và Auth | Đã bổ sung `variantName` từ giá trị thuộc tính dạng chuỗi, số hoặc boolean. JSON cũ không đọc được cho kết quả null. Cần thêm tra cứu theo lô cho tên cửa hàng/danh mục ngoài search. Chưa có nguồn tên người kiểm duyệt. | P1 |
| Product | `/categories` và `/api/categories/admin` | CreateCategoryResult | Thiếu tên danh mục cha | CategoryRepository | Đã bổ sung `parentName`. Danh sách tái sử dụng dữ liệu đã đọc để tra tên cha. | P1 |
| Order | Buyer/Seller/Admin order reads, checkout create, cancel/ship | OrderResult, OrderResult.Item, CreateCheckoutResult, ListSellerOrdersUseCase.Result | Thiếu ngày đặt trên detail, tên người mua/cửa hàng, snapshot tên sản phẩm con, SKU, ảnh và vận đơn | Order DB, Product, Auth, Shipping | Đã trả `createdAt`, `updatedAt` và ánh xạ dòng hàng từ snapshot đã lưu. Các trường lịch sử mới cần lưu lúc checkout. Đơn cũ không có nguồn đáng tin để backfill SKU/ảnh. | P1 |
| Order | `GET /api/checkouts/{id}` | CheckoutResult | Thiếu tên người mua | Auth | Chờ nguồn tên người dùng. Giữ trạng thái checkout và response hiện có. | P2 |
| Inventory | `/api/inventories/seller`, `/api/inventories/admin` và stock writes | InventoryResponse | Thiếu tên sản phẩm, sản phẩm con, SKU, tên cửa hàng | Product Service | Đề xuất đọc metadata theo lô qua application port hoặc read model. Khi nguồn không khả dụng, giữ UUID và tồn kho thực tế, để trống phần hiển thị. | P1 |
| Inventory | `GET /api/inventories/variant/{variantId}`, `/api/reservations/admin` | AvailableStock, ReservationResponse | Public chỉ cần tồn khả dụng. Reservation không có tên nghiệp vụ. | Inventory DB | Giữ payload nhỏ và giữ cách tính tồn khả dụng. | P2 |
| Auth | `/auth/register`, `/auth/me`, `/api/users/admin` | RegisterUserResult, CurrentUserResult, AdminUserResult | Thiếu trường tên hồ sơ | Auth aggregate và DB hiện có email, phone, role/status; Address có receiverName riêng | Tách tên hồ sơ khỏi tên người nhận. Chỉ bổ sung khi có nguồn hồ sơ được xác định và quyền công bố phù hợp. | P1 |
| Shipping | `/api/shipments/admin` | ShipmentResponse | Thiếu tên cửa hàng, người nhận và điện thoại riêng | Order/Checkout | Đề xuất lưu dữ liệu giao hàng có cấu trúc tại checkout. Không suy diễn danh tính từ chuỗi địa chỉ của đơn cũ. | P1 |

### Response mẫu cho các trường bổ sung

Các đoạn dưới là phần trích trong `data`. UUID và giá trị chỉ dùng minh họa.

```json
{
  "items": [
    {
      "shopId": "<shop UUID>",
      "shopName": "Demo Home Studio",
      "categoryId": "<category UUID>",
      "categoryName": "Nội thất"
    }
  ]
}
```

```json
{
  "id": "<variant UUID>",
  "sku": "TS-DEN-XL",
  "attributesJson": "{\"color\":\"Đen\",\"size\":\"XL\"}",
  "variantName": "Đen - XL"
}
```

```json
{
  "parentId": "<category UUID>",
  "parentName": "Nội thất"
}
```

## Error handling and OpenAPI

Common Spring MVC advice emits `{timestamp, status, error, message, details?, path}`. Validation details are an array of messages. The enum name for a domain failure is absent from the body. Read HTTP status first, then parse the body defensively.

| HTTP | Observed contract |
| --- | --- |
| 400 | Invalid JSON, validated body, missing required query parameter, illegal argument, invalid inventory quantity, stock below reserved quantity, or invalid pending-order cancellation. |
| 401 | Missing or invalid bearer on protected routes, invalid login credentials or refresh token, or unknown email in forgot password. |
| 403 | Insufficient role or resource access, including address ownership, or locked account on login. |
| 404 | Missing route or resource. Shared domain codes cover missing user, address, shop, category, inventory, reservation and order. Kong's unmatched-route response has its own format. |
| 409 | Duplicate email or slug, insufficient stock, invalid reservation transition, or an ineligible order cancellation. Inventory's optimistic-lock advice returns plain text. Checkout reservation failure can instead arrive asynchronously as FAILED. |
| 500 | Shared advice catches unexpected exceptions. Its broad fallback means a binding or domain failure without a specific handler may surface as 500. Buyer product detail throws a plain RuntimeException when a visible product is missing. |

Filter-level security and gateway errors can use separate response formats. Use [CommonExceptionHandler](../../backend/multivendor/common/src/main/java/com/common/exception/CommonExceptionHandler.java) and [ErrorCode](../../backend/multivendor/common/src/main/java/com/common/exception/ErrorCode.java) for exact shared mappings.

Auth, product and order configure Springdoc. Auth's OpenApiConfig declares a global bearer scheme, while SecurityConfig permits the public auth operations in the table. Generated specs are service-side and have no checked-in Kong route. This audit used source OpenAPI configuration and controller annotations. No checked-in generated OpenAPI document was found and deployed schemas were not runtime-verified.

## React page mapping

| Frontend page or feature | Backend mapping | Readiness through checked-in Kong |
| --- | --- | --- |
| Sign in, sign up, session, password and address book | Auth and address table | Routed. Public admin grant must be remediated before deployment. |
| Shop directory, shop details, category navigation | Public shop and category reads | Routed |
| Storefront catalog, product detail, variants and availability | Product and public inventory reads | Routed |
| Cart | Client state | Local cart supported once catalog data is available |
| Checkout, confirmation and buyer order history | Checkout and buyer order table | Routed for COD |
| Seller onboarding and shop settings | Seller role grant, seller shop table | Routed |
| Seller catalog, stock view and fulfillment | Seller product, inventory reads and seller order tables | Routed |
| Admin operations | Admin table and inventory/reservation reads | Routed |
| Online payment and promotions | No browser contracts | Unsupported |

## DTO source index

These Java declarations define the request and response fields, enum types and validation annotations. The inventory controllers also define their nested wire records.

- [AddAddressRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/AddAddressRequest.java)
- [AddressResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/AddressResult.java)
- [AdminUserListResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/AdminUserListResult.java)
- [AdminUserResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/AdminUserResult.java)
- [ChangePasswordRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/ChangePasswordRequest.java)
- [ChangeStatusCategoryRequest](../../backend/multivendor/services/product_service/src/main/java/com/product_service/adapter/in/web/dto/request/ChangeStatusCategoryRequest.java)
- [CheckoutResult](../../backend/multivendor/services/order_service/src/main/java/com/order_service/application/checkout/dto/CheckoutResult.java)
- [CreateCategoryRequest](../../backend/multivendor/services/product_service/src/main/java/com/product_service/adapter/in/web/dto/request/CreateCategoryRequest.java)
- [CreateCategoryResult](../../backend/multivendor/services/product_service/src/main/java/com/product_service/application/category/result/CreateCategoryResult.java)
- [CreateCheckoutRequest](../../backend/multivendor/services/order_service/src/main/java/com/order_service/adapter/in/web/request/CreateCheckoutRequest.java)
- [CreateCheckoutResult](../../backend/multivendor/services/order_service/src/main/java/com/order_service/application/checkout/dto/CreateCheckoutResult.java)
- [CreateShipmentRequest](../../backend/multivendor/services/shipping-service/src/main/java/com/multivendor/shipping/dto/CreateShipmentRequest.java)
- [CreateShopRequest](../../backend/multivendor/services/product_service/src/main/java/com/product_service/adapter/in/web/dto/request/CreateShopRequest.java)
- [CurrentUserResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/CurrentUserResult.java)
- [ForgotpasswordRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/ForgotpasswordRequest.java)
- [IncreaseStockCommand](../../backend/multivendor/services/inventory-service/src/main/java/com/inventory_service/application/inventory/command/IncreaseStockCommand.java)
- [InitializeInventoryCommand](../../backend/multivendor/services/inventory-service/src/main/java/com/inventory_service/application/inventory/command/InitializeInventoryCommand.java)
- [InventoryController](../../backend/multivendor/services/inventory-service/src/main/java/com/inventory_service/adapter/in/web/controller/InventoryController.java)
- [InventoryResponse](../../backend/multivendor/services/inventory-service/src/main/java/com/inventory_service/application/inventory/dto/InventoryResponse.java)
- [ListSellerOrdersUseCase](../../backend/multivendor/services/order_service/src/main/java/com/order_service/application/order/usecase/ListSellerOrdersUseCase.java)
- [LoginRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/LoginRequest.java)
- [LoginUserResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/LoginUserResult.java)
- [OrderResult](../../backend/multivendor/services/order_service/src/main/java/com/order_service/application/order/dto/OrderResult.java)
- [ProductDtos](../../backend/multivendor/services/product_service/src/main/java/com/product_service/application/product/dto/ProductDtos.java)
- [RefreshTokenRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/RefreshTokenRequest.java)
- [RefreshTokenResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/RefreshTokenResult.java)
- [RegisterRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/RegisterRequest.java)
- [RegisterUserResult](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/application/port/in/RegisterUserResult.java)
- [ReserveStockCommand](../../backend/multivendor/services/inventory-service/src/main/java/com/inventory_service/application/stockreservation/command/ReserveStockCommand.java)
- [ShipmentResponse](../../backend/multivendor/services/shipping-service/src/main/java/com/multivendor/shipping/dto/ShipmentResponse.java)
- [ShopResult](../../backend/multivendor/services/product_service/src/main/java/com/product_service/application/shop/result/ShopResult.java)
- [StockReservationController](../../backend/multivendor/services/inventory-service/src/main/java/com/inventory_service/adapter/in/web/controller/StockReservationController.java)
- [UpdateAddressRequest](../../backend/multivendor/services/auth_service/src/main/java/com/auth_service/adapters/in/web/dto/request/UpdateAddressRequest.java)
- [UpdateCategoryRequest](../../backend/multivendor/services/product_service/src/main/java/com/product_service/adapter/in/web/dto/request/UpdateCategoryRequest.java)
- [UpdateShopRequest](../../backend/multivendor/services/product_service/src/main/java/com/product_service/adapter/in/web/dto/request/UpdateShopRequest.java)

## Buyer catalog integration

The buyer storefront uses `GET /api/products/search` for product cards. It sends the keyword and selected filters, and receives `{items, page, size, totalElements, totalPages}`. Cards carry `id`, `shopId`, `categoryId`, `name`, `mediaJson`, and `minPrice`. Results require active, approved, nondeleted products in active shops. The old product list retains its array response.

Search supports exact active SKU, phrase matches in names, and full-text matching on names and descriptions with Vietnamese accent folding. Sorts are `relevance`, `newest`, `price_asc` and `price_desc`. Defaults are relevance for nonblank queries and newest otherwise. Page numbers are one-based (1–200), size is 1–48 (default 12), and keywords are capped at 120 characters. Price bounds are nonnegative whole VND. Invalid combinations return 400. Search requires Product Flyway migration V3.

The shop directory uses backend pagination with `{content, page: {size, number, totalElements, totalPages}}`. Shop rating averages and counts can be shown when supplied, and no public review listing exists. The storefront reads only public display fields from `ShopResult` and discards bank details. Media and attributes are opaque strings on the wire. The gallery accepts JSON arrays of HTTP(S) URLs, and variant labels use scalar values from a JSON object or fall back to the SKU. Other payloads fall back to an image-unavailable state or the SKU.


## Buyer order management

The buyer order list reads the full array from `GET /api/orders` using the
authenticated buyer. The frontend never supplies a buyer ID. The endpoint does
not expose pagination, sort, status or date filters. The UI groups the returned
orders by checkout and preserves the server's order within each group.
`GET /api/orders?checkoutId=...` continues to serve checkout results.

Buyer order detail exposes order, checkout and shop IDs, subtotal, shipping fee,
total, order status, payment status and payment method. Shop names are resolved
from `GET /shops/{shopId}` when available. Items, quantities, per-item prices,
the current DTO exposes shippingAddress and items, and order timestamps. Shipment and
tracking reads are admin-only, so the buyer UI presents current order status
without carrier tracking or a shipment timeline.

Buyer cancellation sends `POST /api/orders/{id}/cancel` for the buyer's own
confirmed COD order. It updates or refreshes the list, detail and related
checkout order cache. The backend determines eligibility and performs the
inventory release.

## Seller module audit

All rows below use the existing Kong paths and require SELLER authority. The
backend checks shop ownership. Step 8.6.1 only consumes the four overview reads.

| Step | Verified endpoints and scope |
| --- | --- |
| 8.6.1 Overview | GET /api/shops/seller/my-shop, GET /api/products/seller/shops/{shopId}, GET /api/inventories/seller?shopId=..., GET /api/orders/seller/shops/{shopId}?page=0&size=5 |
| 8.6.2 Shop | POST /api/shops/seller, GET /api/shops/seller/my-shop, PUT /api/shops/seller/{shopId}, DELETE to close, PATCH to reopen |
| 8.6.3 Products | GET and POST /api/products/seller/shops/{shopId}; GET, PUT and DELETE /{id}; PATCH /{id}/status. Approval stays with ADMIN. |
| 8.6.4 Variants | POST /{id}/variants, PUT and DELETE /{id}/variants/{variantId} under the seller shop product prefix. |
| 8.6.5 Inventory | GET /api/inventories/seller?shopId=..., GET /api/inventories/seller/{id}, GET /api/inventories/seller/variants/{variantId}. The seller stock adjustment endpoint exists, but inventory writes are outside the current frontend scope. Reservation actions are ADMIN-only. |
| 8.6.6 Orders | GET /api/orders/seller/shops/{shopId} with page, size, from, to and status. GET /api/orders/seller/{id}, GET /api/orders/seller with checkoutId or shopId, and POST /{id}/cancel or /{id}/ship under the seller order prefix. The workspace uses the paged shop list, detail, cancel and ship routes. Results are ordered newest first by the server. The date range is inclusive in Vietnam time. Cancel requires a confirmed COD order; ship requires a confirmed order. Both require shop ownership and run backend workflows. Seller detail includes line items and shippingAddress. Both detail and list expose creation and update timestamps. Carrier tracking is not embedded. |
| 8.6.7 Reports | No dedicated seller reporting, revenue, payout or analytics API found. The order list returns totals per order and separate order/payment statuses. The Reports page reuses the protected shop order list with page=0 and size=1 and reads totalElements for the selected creation date range and status. This is an order count. Revenue, payouts and reconciliation are unavailable. |
| 8.6.8 Promotions | No public seller promotion management API found. |

The order overview uses the order service's newest-first page, including
`createdAt` from the seller list wrapper. Product and inventory metrics are
counts over those endpoints' returned records. An out-of-stock count describes
only tracked inventory records with zero available quantity.

### Seller shop management

The Seller UI uses the verified My shop, create, update, close and reopen routes.
A My shop 404 opens the create form. Close and reopen require confirmation.
The frontend offers close for ACTIVE shops and reopen for CLOSED shops only.
Logo and banner use HTTP(S) URL fields. There is no verified upload endpoint.
Bank details are preserved on update and omitted from editing because the
backend includes them in public shop responses.

### Seller product management

The seller product list and detail are scoped to the shop returned by My shop.
The list is an array without paging, search or sort parameters. Create sends
product metadata with no initial variants. New products start INACTIVE and
PENDING. Saving metadata returns the product to PENDING. Selling status and
approval are displayed separately. Seller may set ACTIVE or INACTIVE, and
DELETE soft-deletes the product. Admin remains responsible for approval.
Catalog image fields use JSON with HTTP(S) URLs. No image upload is wired.
Variant CRUD is available on the Seller product detail page.


### Seller variant management

The Seller detail response includes variants. The UI displays nondeleted entries
and sends only SKU, price, attributesJson and active on POST/PUT. DELETE soft-deletes
the variant. All three mutations return the updated Seller product, including its
recalculated minimum active price and PENDING review status. New and changed
prices use whole VND to remain compatible with the Order Service. Existing opaque
attributes or fractional prices can be retained when editing other fields.
No stock writes are made in this module.

### Seller inventory management

The Seller inventory list uses the authenticated My shop ID and displays the
backend-provided on-hand, reserved and available quantities. Record and variant
detail use their corresponding Seller GET endpoints. Product names and SKUs
come from the Seller product list when available, with ID fallbacks. A missing
record is shown as unavailable rather than counted as zero stock. This module
is read-only and has no stock adjustment or reservation actions.

## Step 8.7 Admin module audit

The Admin operations below are routed through Kong and require ADMIN at the service. Public category reads are identified separately. Overview, User Management, Seller and Shop Management, Category Management, and Product Moderation are implemented.

| Step | Verified contract | Limits |
| --- | --- | --- |
| 8.7.1 Overview | GET /api/users/admin and GET /api/shops/admin with page=0 and size=1 for totalElements. GET /api/products/admin/pending returns the moderation queue. | Pending queue is unpaginated. Metrics load independently. |
| 8.7.2 Users | GET /api/users/admin with keyword, page and size. GET/PUT/DELETE /api/users/admin/{userId}; PATCH /api/users/admin/{userId}/status (ACTIVE/LOCKED). | Keyword matches email or phone. No role/status filters or sort parameter. DELETE hard-deletes the account. UI và backend chặn tự xóa/tự cấm; sửa chỉ email/phone, giữ role/password. Admin phải còn ACTIVE trong DB. LOCKED chặn login/refresh; access JWT cũ ở service khác còn hiệu lực tới expiry. No Admin password or general role management workflow. Exclude the public admin grant. |
| 8.7.3 Sellers and shops | GET /api/shops/admin and GET /api/shops/admin/{shopId}. PATCH /api/shops/admin/{shopId}/status with shopStatus. PATCH with CLOSED closes the shop in the UI. DELETE delegates to an owner-only use case even on the Admin route. | The list controller accepts sellerId but does not apply it. No dedicated seller approval queue. |
| 8.7.4 Categories | POST /api/categories/admin, PUT and DELETE /api/categories/admin/{id}, PATCH /api/categories/admin/{id}/status. | Public GET /categories and GET /categories/{id} provide list and detail. The list repository has no status filter. The UI loads the public list for editing. DELETE sets INACTIVE and PATCH can reactivate. No dedicated Admin list or pagination. |
| 8.7.5 Moderation | GET /api/products/admin/pending, GET /api/products/admin/{id}, POST /api/products/admin/{id}/approve and POST /api/products/admin/{id}/reject. | The pending queue has no pagination. Only live PENDING products can be decided. Approval accepts an optional reason. The Admin UI requires a rejection reason. The result contains approval status and history. |
| 8.7.6 Promotions | No verified browser route. | No campaign, voucher or discount management contract. |
| 8.7.7 System reports | Admin order detail and filtered reads by checkoutId, shopId or buyerId. | No global order list, reporting aggregates, revenue or reconciliation contract. |


## Seller image upload

`POST /api/media/upload` through Kong uses multipart/form-data: `domain` (products or sellers), `entityId` (UUID), optional `variantId` (UUID), and `file`. The current backend permits this media route publicly; the seller UI uses the shared authenticated Axios client. Response is the raw body `{ "url": "http://localhost:8000/media/..." }`, not a data envelope.

The seller forms accept JPEG, PNG, WebP files up to 10 MiB each, matching the current Product multipart limit. The browser supplies the multipart boundary. Product photos support multiple selections; variant photo, shop logo and banner each store one URL. Successful uploads are previewed immediately. Save is blocked while a field is uploading. A failed upload retains the previous URL and any earlier successful uploads so the seller can retry. Removing a photo removes its association on the next save; it does not delete the physical file.

After upload, save the returned URL in the existing request: product `mediaJson` is a JSON-encoded URL array; variant `imageUrl`, shop `logoUrl` and `bannerUrl` are strings or null. No user-entered URL fields are needed on these seller forms. Existing URLs remain if untouched.

For create forms, the backend assigns entity IDs only when saving. The UI generates stable draft UUIDs to group uploads before creation; these storage folder IDs can differ from the eventual database IDs. Existing records use their actual IDs. The current upload API does not relocate files after creation. Uploading then leaving the form can leave unreferenced files; the backend currently has no active orphan cleanup job.


## Product reviews · Routed

Đối chiếu ReviewController, ReviewResult và ReviewPageResult của order_service.

| Method | Path | Input | Response data | Access |
| --- | --- | --- | --- | --- |
| GET | /api/reviews/products/{productId} | page=0, size=20 mặc định; page >= 0, size 1..100 | ReviewPageResult | Public |

ReviewPageResult: items (ReviewResult[]), page, size, totalElements, totalPages, averageRating (điểm trung bình toàn bộ đánh giá của sản phẩm, không phải riêng trang hiện tại).

ReviewResult: id, productId, buyerName, rating (1..5), comment, mediaUrls (mảng URL ảnh/video), createdAt, updatedAt. Không công khai buyerId hoặc orderId. Danh sách mới nhất trước theo createdAt và id. ProductReviewCard dùng size=5 và page bắt đầu từ 0.


### Admin user management — 14/11/2026

PUT contact validates email (max 100), phone (0 plus 9 digits). PATCH status is idempotent for the requested target status. GET/list and all mutations also verify the current admin's ACTIVE status from Auth DB. Self-lock/self-delete return 409. No role grants or password mutations in this workflow. Java source/tests added; backend build/tests have not been run for this change.


### UI quản lý shop Admin — 14/11/2026

Danh sách dùng GET /api/shops/admin với page/size/sort, response Spring Page<ShopResult> (`content`, `number`, `size`, `totalElements`, `totalPages`). Search/status UI chỉ lọc trang hiện tại; không coi sellerId là bộ lọc vì controller chưa sử dụng. GET chi tiết và PATCH /status?shopStatus=ACTIVE|SUSPENDED|CLOSED|BANNED đã nối UI, mỗi mutation cần xác nhận. Đóng shop dùng PATCH CLOSED; không dùng DELETE vì implementation hiện yêu cầu ownership của actor. Chưa có Admin PUT sửa hồ sơ shop. Chỉ sửa code, không chạy build/test theo yêu cầu.


### Sửa phân trang shop VIA_DTO — 14/11/2026

WebConfig của product_service bật PageSerializationMode.VIA_DTO. GET /shops và GET /api/shops/admin trả ApiResponse.data dạng `{content: [...], page: {number, size, totalElements, totalPages}}`, không phải PageImpl có metadata ở ngoài. shopPage.js chuẩn hóa hai API về shape flat cho UI hiện có; vẫn nhận response flat cũ, không tự điền dữ liệu phân trang bị thiếu hoặc bỏ validation. GET chi tiết/PATCH status không qua normalizer.


### UI danh mục Admin — 14/11/2026

Read list/detail dùng GET /categories và /categories/{id}, list là mảng đầy đủ gồm ACTIVE/INACTIVE (không Spring Page). Writes: POST /api/categories/admin, PUT /api/categories/admin/{id}, DELETE /api/categories/admin/{id}, PATCH /api/categories/admin/{id}/status {status: ACTIVE|INACTIVE}. Create/update nhận parentId nullable/name/slug/description/imageUrl/displayOrder. name/slug <=100, description <=1000, imageUrl <=255, displayOrder nguyên không âm. DELETE chỉ đặt INACTIVE, không xóa cứng/cascade. UI ẩn/kích hoạt và chặn chọn parent self/descendant; backend UpdateCategoryUseCase vẫn quyết định chu kỳ. Upload ảnh qua Media domain=categories sau khi có UUID thật. Chỉ sửa code, không chạy build/test/lint.


### UI duyệt sản phẩm Admin — 14/11/2026

- GET `/api/products/admin/pending`: ApiResponse.data là mảng ProductDtos.Seller, không phải Page. UI chỉ hiển thị PENDING chưa xóa, tìm kiếm và phân trang phía frontend.
- GET `/api/products/admin/{id}`: chi tiết Seller gồm variants (id, sku, price, attributesJson, active, soldCount, deletedAt, variantName, imageUrl) và histories; dùng endpoint admin để xem được sản phẩm chờ duyệt.
- DTO không có shopName: GET `/api/shops/admin/{shopId}` để lấy tên cửa hàng, gộp shopId duy nhất trong hàng đợi, tối đa 4 request đồng thời. Dialog có request shop riêng và retry; lỗi metadata không chặn xem sản phẩm.
- POST `/api/products/admin/{id}/approve` hoặc `/reject`, body `{reason}`, trả Seller. UI xác nhận trước POST; từ chối cần reason.trim() không rỗng, tối đa 2000 ký tự ở UI. Không tự gửi lại POST khi lỗi; đọc lại trạng thái.
- Các thao tác nằm trong guard ADMIN và backend hiện có. Không sửa backend, không upload file trong trang duyệt. Không chạy build/test/lint.


### UI đơn hàng Admin — 14/11/2026

- GET `/api/orders/admin/{orderId}` trả `ApiResponse.data: OrderResult`.
- GET `/api/orders/admin?checkoutId={UUID}` hoặc `?shopId={UUID}` hoặc `?buyerId={UUID}` trả mảng OrderResult. UI chỉ gửi một selector; chưa có GET danh sách toàn hệ thống, search tên toàn hệ thống hoặc phân trang server.
- OrderResult: id/checkoutId/buyerId/shopId/shippingAddress/items/subtotal/shippingFee/totalAmount/orderStatus/paymentStatus/paymentMethod/createdAt/updatedAt. items có productId/variantId/productName/variantName/variantImageUrl/unitPrice/quantity. Thông tin người nhận dùng shippingAddress nguyên bản; không giả định có trường tên/số điện thoại riêng.
- Filter ngày/trạng thái/thanh toán/từ khóa, sort và phân trang là phía frontend trên mảng tra cứu, ngày theo Asia/Ho_Chi_Minh và bao gồm ngày kết thúc. Tên shop trong dialog từ GET `/api/shops/admin/{shopId}`; chưa fetch tên buyer từ auth.
- POST `/api/orders/admin/{orderId}/cancel` không body, trả OrderResult. UI giới hạn COD/CONFIRMED/chưa PAID, yêu cầu xác nhận; kiểm tra CANCELLED trong response, toast và tải lại danh sách. Không có lưu lý do hủy ở API này.
- Lịch sử trạng thái ghi **Chưa triển khai**. Không có API history/shipment được nối vào trang này; chưa gọi POST ship từ UI Admin.
- Route dùng guard ADMIN hiện có. Không sửa backend, không chạy build/test/lint.


### Đối chiếu doanh thu Admin — 14/11/2026

UI `/admin/reports` dùng GET `/api/shops/admin?page={page}&size=10&sort=createdAt,desc` (qua normalizeShopPage) và GET `/api/orders/admin?shopId={UUID}` cho mỗi shop trong trang. Không gọi API seller dưới quyền Admin. Tối đa 4 request đơn đồng thời; lỗi shop được giữ riêng, không quy thành doanh thu 0.

Frontend lọc orderStatus=DELIVERED và ngày updatedAt trong from/to inclusive theo Asia/Ho_Chi_Minh. Cộng subtotal/shippingFee/totalAmount; tách totalAmount theo paymentStatus=PAID và khác PAID. Tổng thẻ chỉ gồm shop tải thành công trên trang hiện tại; không phải tổng toàn hệ thống hoặc tổng tìm kiếm. Click xem đơn hiển thị các OrderResult cấu thành và mở GET chi tiết admin. Ngày thiếu/không hợp lệ ở đơn DELIVERED hoặc tổng vượt Number.MAX_SAFE_INTEGER làm báo lỗi, không bỏ qua âm thầm.

Chưa có API ledger/payout/settlement/refund/commission; không suy ra doanh thu thực thu, số tiền phải trả shop hoặc trạng thái đã đối soát. PAID là trạng thái đơn hiện tại. updatedAt có thể thay đổi và làm dịch chuyển kỳ; chưa có deliveredAt hoặc kỳ kế toán bất biến. Không có thao tác chốt/chuyển tiền. Không chạy build/test/lint theo yêu cầu.


### Dialog đơn hàng theo shop — 14/11/2026

- Trang `/admin/reports` thêm nút **Xem đơn hàng của shop**, sử dụng được cả khi tổng hợp doanh thu shop lỗi.
- Dialog độc lập tải GET `/api/orders/admin?shopId={UUID}`; kiểm tra đúng shop/không trùng mã. Có lọc từ ngày–đến ngày, theo createdAt hoặc updatedAt, trạng thái đơn và phân trang frontend 20 đơn/trang.
- Mặc định dùng khoảng ngày báo cáo, DELIVERED và updatedAt để đối chiếu; có thể chọn tất cả trạng thái và ngày đặt để xem các đơn khác. Khoảng ngày inclusive theo Asia/Ho_Chi_Minh, chỉ áp dụng khi bấm bộ lọc, không đổi kỳ báo cáo bên ngoài.
- Bảng có mã đơn/người nhận/ngày đặt/cập nhật/trạng thái/thanh toán/tổng tiền. Mở dialog chi tiết từng đơn và quay lại vẫn giữ bộ lọc. Sau thao tác hủy cập nhật lại danh sách và báo cáo. Đơn thiếu ngày hợp lệ được thông báo thay vì âm thầm bỏ qua.
- Lịch sử trạng thái trong chi tiết đơn vẫn ghi Chưa triển khai. Không thêm API backend; danh sách shop-order hiện chưa có lọc ngày hoặc phân trang server.
- Thêm components/admin/AdminShopOrdersDialog.jsx, nối AdminRevenuePage. Không chạy build/test/lint theo yêu cầu.


### Chuyển Admin sang API phân trang/tổng hợp server — 14/11/2026 (thay thế ghi chú triển khai cũ)

- `getAdminOrderPage(params, options)` → GET `/api/orders/admin/page`; ApiResponse.data `{items: OrderResult[],page,size,totalElements,totalPages}`. Khác Seller page: items là OrderResult trực tiếp. Filter server hỗ trợ orderId/checkoutId/shopId/buyerId/status/paymentStatus/paymentMethod/from/to/dateBasis/direction/q. page=0,size=20 (1–100), dateBasis=CREATED_AT|UPDATED_AT, direction=ASC|DESC. from/to inclusive Việt Nam. q tìm chuỗi con không phân biệt hoa/thường trong mã, địa chỉ nhận, tên product/variant snapshot, <=150 ký tự; chưa tìm bỏ dấu/SKU/tên shop/buyer.
- `/admin/orders` tự tải toàn hệ thống, tìm kiếm debounce 300ms, các filter và phân trang gửi server, giữ UI bảng hiện có. Có thể thu hẹp một UUID bằng form, để trống hoặc bỏ lọc để quay lại toàn hệ thống. Không lọc/slice mảng toàn bộ đơn ở frontend.
- AdminShopOrdersDialog dùng cùng `/page`, truyền shopId/from/to/dateBasis/status/page/size=20; giữ chọn ngày độc lập và mở chi tiết từng đơn.
- `getAdminRevenueReport` → GET `/api/orders/admin/reports/revenue?from=...&to=...&page=0&size=10[&shopId=...]` trả `{from,to,timezone,dateBasis,summary,items:[{shopId,summary}],page,size,totalElements,totalPages}`. summary gồm deliveredOrders/paidOrders/revenue/shippingFee/totalOrderAmount/paidOrderAmount/unpaidOrderAmount.
- Báo cáo server chỉ DELIVERED theo updatedAt; summary toàn phạm vi lọc, không phụ thuộc phân trang. Bảng chỉ gồm shop có đơn khớp kỳ, sort tiền hàng giảm dần; không có dòng shop doanh thu 0 nếu không phát sinh. UI bỏ tổng hợp/tải toàn bộ đơn từng shop và bỏ tìm shop-name cục bộ cũ; có lọc UUID shop trên server. Chỉ GET admin shop để bổ sung tên tối đa 10 shop trong trang, 4 request đồng thời; lỗi tên không chặn số liệu.
- Số tiền JSON number phải là số nguyên an toàn trong UI; số lớn vượt giới hạn báo lỗi, không âm thầm làm tròn. PAID chưa đồng nghĩa đối soát thực thu; chưa có chốt/chuyển tiền. API cũ giữ tương thích.
- Backend contract chi tiết tại backend/multivendor/docs/API-FRONTEND.md. Không chạy build/test/lint hoặc API/migration thật trong bước này.

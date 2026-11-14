# Báo cáo thống kê Seller — thiết kế và code đã dựng

Trạng thái: đã dựng code; theo yêu cầu chưa chạy build, test hoặc SQL. Frontend my-website đã nối /seller/reports; chưa nghiệm thu tích hợp API thật.

## Thành phần

| Thành phần | File/package trong order_service/src/main/java/com/order_service | Trách nhiệm |
|---|---|---|
| Controller | adapter/in/web/controller/SellerReportController.java | Hai API GET cho Seller |
| Exception handler | adapter/in/web/exception/SellerReportExceptionHandler.java | Lỗi tham số 400, sai quyền 403, không xác minh được quyền 503 |
| Model | application/report/model/ReportGroupBy.java, ReportRange.java | Ngày/tuần/tháng/năm, khoảng ngày và timezone |
| DTO | application/report/dto/SellerDemandReport.java, SellerSalesReport.java | Summary, periods, mặt hàng bán chạy |
| Use case nhu cầu | application/report/usecase/GetSellerDemandReportUseCase.java | Nhu cầu, hủy đơn, tỷ lệ hủy, điền kỳ trống |
| Use case doanh thu | application/report/usecase/GetSellerSalesReportUseCase.java | Doanh thu và mặt hàng DELIVERED |
| Guard quyền shop | application/report/usecase/SellerReportAccess.java | Tái sử dụng ShopOwnershipPort hiện có |
| Query port | application/report/port/SellerReportQueryPort.java | Hợp đồng truy vấn aggregate |
| JDBC adapter | adapter/out/persistence/report/SellerReportJdbcAdapter.java | SQL PostgreSQL trên orders/order_items |
| Index Flyway | src/main/resources/db/migration/V5__create_order_indexes.sql | Index lọc shop/ngày/trạng thái, chạy cùng schema migration |

Không thêm bảng, không thay DTO đơn hàng cũ, không sửa checkout. Không lấy tên/giá sản phẩm hiện tại. Chỉ kiểm tra quyền shop qua ShopOwnershipPort hiện có; phần báo cáo chỉ truy vấn order_db. Không thêm internal API.

SecurityConfig hiện đã bảo vệ `/api/orders/seller/**` bằng ROLE_SELLER. Controller báo cáo nằm trong prefix này. Kong đã chuyển tiếp `/api/orders/**`, nên không cần sửa route gateway.

## Tham số chung

- `from`, `to`: bắt buộc, YYYY-MM-DD, tính theo Asia/Ho_Chi_Minh; bao gồm cả ngày to. SQL dùng `[from đầu ngày, to + 1 ngày đầu ngày)`.
- from không được sau to; tối đa 3661 ngày, năm từ 1 đến 9998 để chặn biên tràn của LocalDate khi tính cuối kỳ.
- groupBy nhận chữ hoa. Tuần bắt đầu thứ Hai, kết thúc Chủ nhật; tháng và năm là kỳ lịch.
- periods tăng dần và có đủ kỳ, kể cả kỳ không có đơn (toàn bộ chỉ số bằng 0).
- periodStart/periodEnd là biên kỳ lịch. Nếu from/to cắt giữa kỳ, số liệu chỉ lấy phần nằm trong from/to; không mở rộng phạm vi lọc.
- Query aggregate không tải toàn bộ Order/OrderItem vào Java. Adapter dùng transaction read-only REPEATABLE_READ; báo cáo sales đọc các tổng và breakdown trong cùng snapshot DB. Kiểm tra quyền hoàn tất trước transaction này.

## API 1 — Nhu cầu và hủy đơn

`GET /api/orders/seller/shops/{shopId}/reports/demand?from=2026-11-01&to=2026-11-30&groupBy=WEEK`

groupBy hỗ trợ DAY/WEEK/MONTH, mặc định DAY. Lọc bằng orders.created_at; lấy tất cả trạng thái và tách CANCELLED / khác CANCELLED. Không loại PENDING, PENDING_PAYMENT hoặc UNPAID ra khỏi nhóm chưa hủy.

Summary và mỗi phần tử periods có các chỉ số:

| Trường | Công thức |
|---|---|
| totalOrders | COUNT orders trong phạm vi |
| cancelledOrders | COUNT orders có trạng thái hiện tại CANCELLED |
| nonCancelledOrders | totalOrders - cancelledOrders |
| totalQuantity | Tổng OrderItem.quantity thuộc các đơn này |
| cancelledQuantity | Tổng quantity của các đơn CANCELLED |
| nonCancelledQuantity | totalQuantity - cancelledQuantity |
| cancellationRate | cancelledOrders / totalOrders × 100, làm tròn 2 chữ số; không có đơn trả 0.00 |

Đây là số đơn và số đơn vị sản phẩm, không phải số buyer duy nhất. Một đơn nhiều dòng hàng vẫn chỉ đếm một đơn; SQL cộng quantity theo order trước khi join.

Đơn tạo trong kỳ nhưng hủy sau kỳ vẫn xuất hiện là đã hủy khi xem báo cáo sau đó. Đây là báo cáo trạng thái hiện tại của nhóm đơn theo ngày tạo, không phải lịch sử các thao tác hủy.

Ví dụ data (ngoài data còn có message và timestamp theo ApiResponse):

```json
{
  "shopId": "<shopId>",
  "from": "2026-11-02",
  "to": "2026-11-08",
  "groupBy": "WEEK",
  "timezone": "Asia/Ho_Chi_Minh",
  "summary": {
    "totalOrders": 20,
    "nonCancelledOrders": 15,
    "cancelledOrders": 5,
    "totalQuantity": 40,
    "nonCancelledQuantity": 30,
    "cancelledQuantity": 10,
    "cancellationRate": 25.00
  },
  "periods": [
    {
      "periodStart": "2026-11-02",
      "periodEnd": "2026-11-08",
      "totalOrders": 20,
      "nonCancelledOrders": 15,
      "cancelledOrders": 5,
      "totalQuantity": 40,
      "nonCancelledQuantity": 30,
      "cancelledQuantity": 10,
      "cancellationRate": 25.00
    }
  ]
}
```

## API 2 — Doanh thu và mặt hàng bán được

`GET /api/orders/seller/shops/{shopId}/reports/sales?from=2026-01-01&to=2026-12-31&groupBy=MONTH&variantPage=0&variantSize=20`

groupBy hỗ trợ DAY/WEEK/MONTH/YEAR, mặc định MONTH. Để xem doanh thu hôm nay, truyền from=to=ngày hiện tại theo giờ Việt Nam và groupBy=DAY; ví dụ `/api/orders/seller/shops/{shopId}/reports/sales?from=2026-11-12&to=2026-11-12&groupBy=DAY`. Với DAY, periodStart và periodEnd cùng là ngày đó. Chỉ lấy orders.order_status=DELIVERED và lọc/nhóm theo orders.updated_at (tạm dùng theo yêu cầu). Response có dateBasis=updatedAt. Không thêm điều kiện paymentStatus.

| Trường | Công thức |
|---|---|
| deliveredOrders | Số đơn DELIVERED trong khoảng updatedAt |
| soldQuantity | Tổng quantity trong các đơn này |
| revenue | SUM orders.subtotal — tiền hàng chưa gồm vận chuyển |
| shippingFee | SUM orders.shipping_fee |
| totalOrderAmount | SUM orders.total_amount |

Tiền dùng BigDecimal ở DTO/aggregate để không tràn khi cộng nhiều đơn. Response JSON vẫn là số. Không join trực tiếp các dòng hàng rồi SUM order.subtotal vì sẽ nhân sai doanh thu; SQL tổng hợp quantity theo order trước.

bestSellingVariants là breakdown **toàn bộ khoảng from/to**, không riêng từng period. Nhóm theo productId + variantId, gồm productName, variantName, soldQuantity, revenue. Không có attributesJson. Tên lấy từ dòng hàng có updatedAt của order mới nhất trong phạm vi (hòa thì theo orderId/itemId) để variant đổi tên không bị tách thành nhiều nhóm. Tên variant có thể null cho đơn cũ.

revenue mỗi variant = SUM(unitPrice × quantity) đã lưu ở order_items, không gồm phí vận chuyển. Xếp soldQuantity giảm dần, revenue giảm dần rồi productId/variantId để phân trang ổn định. variantPage từ 0, variantSize từ 1 đến 100 (mặc định 20). Summary/periods luôn tính trên toàn bộ phạm vi, không phụ thuộc trang variant. Response có totalVariants và totalVariantPages. Trang vượt cuối trả bestSellingVariants=[] nhưng vẫn trả summary/periods.

Ví dụ data cho một tháng có một variant:

```json
{
  "shopId": "<shopId>",
  "from": "2026-11-01",
  "to": "2026-11-30",
  "groupBy": "MONTH",
  "timezone": "Asia/Ho_Chi_Minh",
  "dateBasis": "updatedAt",
  "summary": {
    "deliveredOrders": 2,
    "soldQuantity": 3,
    "revenue": 375000,
    "shippingFee": 30000,
    "totalOrderAmount": 405000
  },
  "periods": [
    {
      "periodStart": "2026-11-01",
      "periodEnd": "2026-11-30",
      "deliveredOrders": 2,
      "soldQuantity": 3,
      "revenue": 375000,
      "shippingFee": 30000,
      "totalOrderAmount": 405000
    }
  ],
  "bestSellingVariants": [
    {
      "productId": "<productId>",
      "productName": "Áo thun",
      "variantId": "<variantId>",
      "variantName": "Blue - M",
      "soldQuantity": 3,
      "revenue": 375000
    }
  ],
  "variantPage": 0,
  "variantSize": 20,
  "totalVariants": 1,
  "totalVariantPages": 1
}
```

Do dùng updatedAt tạm thời, sửa một đơn DELIVERED sau này có thể chuyển doanh thu sang kỳ cập nhật mới. Báo cáo không phải sổ kế toán hay lịch sử giao hàng bất biến. Có thể thay bộ lọc sang deliveredAt khi bổ sung trường đó sau này.

## Kiểm chứng để làm sau

Chưa chạy build/test theo yêu cầu. Cần kiểm tra cú pháp/kiểu khi build; SQL PostgreSQL, biên timezone/tuần/năm; đơn nhiều item không nhân doanh thu; ngày không có dữ liệu; cancelled cohort; tên variant đổi; phân trang và quyền sở hữu. Index nằm trong Flyway V5; trạng thái áp dụng thực tế cần kiểm tra trên DB.


Báo cáo Admin là API riêng `/api/orders/admin/reports/revenue`, xem [API contract](API-FRONTEND.md). Không gọi API Seller bằng token Admin.

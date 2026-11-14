# Cấu trúc frontend my-website

Giữ cấu trúc đơn giản, chia thư mục theo nghiệp vụ. Chưa tạo component hoặc page giả; .gitkeep chỉ giữ các thư mục trống trong Git.

```text
src/
  App.jsx
  main.jsx
  index.css
  api/                  # API hiện có, chia theo nghiệp vụ; dùng axiosClient chung
  docs/                 # API contract và hướng dẫn cấu trúc
  components/
    common/             # Component dùng chung: Header, Footer, Button, Loading...
    auth/               # LoginForm, RegisterForm...
    product/            # ProductCard, ProductList, ProductForm, VariantForm...
    category/
    shop/               # ShopInfo, ShopForm, ShopLogo...
    inventory/
    order/
    cart/
    checkout/
    shipping/
    seller/             # Component riêng của khu vực người bán
    admin/              # Component riêng của khu vực quản trị
  pages/
    home/
    auth/
    product/
      ProductListPage.jsx
    category/
    shop/
    inventory/
    order/
    cart/
    checkout/
    shipping/
    seller/
    admin/
```

- Page ghép các component thành màn hình và gắn với route. Ví dụ pages/product/ProductListPage.jsx dùng components/product/ProductList.jsx.
- Component chỉ dùng trong một nghiệp vụ đặt ở thư mục nghiệp vụ đó; nhiều nghiệp vụ cùng dùng thì đặt trong components/common.
- Header/Footer dùng chung đặt trực tiếp trong components/common, không cần thêm lớp thư mục.
- Form variant nằm trong components/product vì variant thuộc sản phẩm.
- Màn hình riêng theo vai trò đặt trong pages/seller hoặc pages/admin; component nghiệp vụ có thể được dùng lại từ components/product, order, inventory, shop.
- Chỉ tách tiếp thư mục con khi số file thực tế cần đến; chưa thêm features, providers hay lớp service mới.
- Giữ nguyên API trong src/api và contract trong src/docs/api-contracts.md. Không tạo thêm Axios client.

Header.jsx và Footer.jsx nằm trong components/common và được ghép tại App.jsx. Header tìm kiếm bằng query q trên route /; ProductListPage đọc query này. ProductCard cơ bản đã được bổ sung trong components/product để trang render được. Các route Auth hiện có: /login và /register; chưa xây dựng giỏ hàng.


## Đăng nhập và đăng ký

- pages/auth/LoginPage.jsx, RegisterPage.jsx: màn hình đăng nhập/đăng ký.
- components/auth/AuthLayout.jsx, AuthForm.jsx: giao diện và validation dùng chung.
- components/auth/useAuth.js: xác minh hồ sơ trước khi lưu token, khôi phục phiên khi reload, refresh sau lỗi 401 và đăng xuất.
- components/auth/AccountMenu.jsx: liên kết hoặc trạng thái tài khoản trên Header.
- components/auth/authHelpers.js: chuẩn hóa input, kiểm tra token/profile và thông báo lỗi.

Đăng ký gửi email/password/phone; passwordConfirm chỉ được kiểm tra ở frontend. Đăng ký thành công chuyển sang /login và điền email vừa đăng ký. Đăng nhập xác minh /auth/me với token mới, chỉ chấp nhận tài khoản ACTIVE khớp userId. Token lưu trong sessionStorage của tab qua authSession hiện có. Đăng xuất xóa phiên local ngay; nếu API thu hồi refresh token thất bại, UI báo rõ.

Chạy npm run lint, npm run build và npm test. Các test dùng API adapter giả lập, không xác minh backend/Kong thực tế.


## Danh mục trên trang chủ

CategoryList dùng CategoryCard trong components/category, tải GET /categories, hiển thị danh mục ACTIVE theo displayOrder rồi tên. Chọn thẻ cập nhật categoryId trên URL, giữ từ khóa q và bỏ page cũ. Danh sách sản phẩm gửi categoryId và includeChildren=true; tìm kiếm Header giữ bộ lọc đang chọn. Nút Tất cả sản phẩm bỏ bộ lọc danh mục. Có trạng thái tải/lỗi/rỗng, thử lại và ảnh thay thế khi thiếu hoặc lỗi ảnh.


## Chi tiết sản phẩm

- Route /products/:id nằm trong MainLayout, dùng Header/Footer hiện có. ProductCard liên kết đến route bằng product.id.
- pages/product/ProductDetailPage.jsx tải GET /api/products/{id}; có loading, lỗi/404, thử lại và hủy request khi chuyển trang.
- components/product/ProductInfo.jsx ghép thông tin sản phẩm, variant, giá, SKU, thuộc tính, mô tả và shop.
- ProductGallery.jsx hiển thị ảnh/video, thumbnail, ảnh theo variant và trạng thái media lỗi. Video chỉ phát khi người dùng bật controls.
- VariantStock.jsx đọc GET /api/inventories/variant/{variantId} cho variant đang chọn; phân biệt loading/lỗi/hết hàng/còn hàng. Đây là số khả dụng lúc xem, không phải giữ hàng.
- ProductShop.jsx đọc GET /shops/{shopId}, hiển thị tên/logo/đánh giá nếu có. Link Xem sản phẩm của shop dùng shopId trên route /; danh sách sản phẩm đã hỗ trợ lọc này.
- productHelpers.js xử lý JSON media/thuộc tính và tên variant dự phòng. Không hiển thị HTML thô từ description.

Giỏ hàng và hiển thị đánh giá đã được bổ sung ở các bước sau; chưa triển khai thanh toán hoặc gửi đánh giá ở trang chi tiết. Test product dùng dữ liệu và API adapter giả lập, chưa kiểm thử với backend thực tế.


## Bình luận và đánh giá sản phẩm

ProductDetailPage gọi `<ProductReviewCard productId={result.product.id} />` sau ProductInfo. Component nhận productId, tự tải API công khai, hiển thị điểm trung bình/tổng số đánh giá, loading/lỗi/rỗng và phân trang 5 đánh giá. Khi productId đổi, trang đánh giá reset về 0 và request cũ bị hủy. ReviewItem hiển thị tên người mua, số sao, thời gian, nội dung và media; ảnh mở tab mới, video có controls.

API đọc nằm ở src/api/order/reviewApi.js, dùng Axios client chung. Bước này hiển thị danh sách; gửi đánh giá cần orderId chứng minh đã mua và được thực hiện từ luồng đơn hàng khi xây dựng phần đó.


## Giỏ hàng

- /cart: pages/cart/CartPage.jsx ghép CartItem và khung tạm tính. Header có link và số lượng sản phẩm trong giỏ.
- ProductInfo dùng VariantStock render AddToCart cho variant đang chọn. Người dùng chọn số lượng; nút thêm bị khóa nếu chưa xác minh phiên, chưa có tồn kho hoặc đã đạt lượng khả dụng.
- components/cart/useCart.js quản lý state và localStorage; MainLayout cung cấp {auth, cart} qua Outlet context. Không thêm Redux/Context provider hay API giỏ hàng giả.
- Giỏ khách dùng owner guest; giỏ đăng nhập dùng userId. Dữ liệu dùng khóa market-lane-cart-v1:<owner>. Không tự gộp giỏ khách vào tài khoản, không xóa giỏ tài khoản khi đăng xuất. Giỏ khách được khôi phục khi quay lại chế độ khách. State được remount theo owner và chặn mutation khi phiên đã đổi.
- Mỗi variant là một dòng; thêm lại cộng số lượng và cập nhật snapshot tên/ảnh/giá. Tối đa 100 dòng, quantity nguyên dương trong int32. Số lượng tổng của variant không được vượt tồn kho vừa đọc.
- Trước khi thêm/sửa, cartRequests.js đọc product detail và public inventory để xác nhận variant còn bán, giá hiện tại và số khả dụng. Request cũ bị hủy khi rời trang/đổi variant; async update không tái tạo dòng đã bị xóa.
- useCartAvailability.js kiểm tra giỏ khi mở/thay đổi các mặt hàng và khi bấm Làm mới; các variant cùng sản phẩm dùng chung một lần đọc product. Cart hiển thị giá hiện tại khi kiểm tra được, báo mặt hàng lỗi/hết hàng/vượt tồn kho và vẫn cho xóa.
- Cho phép sửa quantity, xóa từng dòng và xóa toàn bộ (có xác nhận). Tạm tính chưa bao gồm phí vận chuyển. Chưa nối checkout ở bước này; server sẽ xác nhận giá và giữ hàng khi đặt hàng.
- Storage input được chuẩn hóa, bỏ dòng lỗi/trùng và chỉ lưu trường hiển thị, không lưu token. Khi storage bị chặn/hỏng, UI báo rõ và giữ giỏ trong bộ nhớ. Storage event đồng bộ thay đổi giữa các tab cùng tài khoản; đây không phải cơ chế khóa giao dịch nhiều tab hoặc đồng bộ server.

Test cart kiểm tra gộp variant, giới hạn tồn kho/quantity/100 dòng, ảnh/giá variant, dữ liệu storage và public API. Chưa kiểm thử thao tác trình duyệt hoặc backend thực tế.


## Toast dùng chung

Chỉ mount một Toaster tại src/main.jsx, nằm cùng cấp với App để không mất thông báo khi chuyển route hoặc remount layout/tài khoản. Không thêm thư viện hoặc Axios interceptor cho Toast.

```js
import { toast } from "../../components/common/toast.js"; // chỉnh relative path theo file

toast.success("Đã lưu thay đổi.");
toast.error("Không thể hoàn tất. Vui lòng thử lại.");
```

- src/components/common/toast.js quản lý một thông báo tại một thời điểm; thông báo thao tác mới thay thế thông báo trước. Cấu hình successDurationMs=4000 và errorDurationMs=6500 tại toastConfig.
- Toaster.jsx hiển thị ở góc trên phải (co giãn trên mobile), có nút đóng, live region cho screen reader và tạm dừng khi hover/focus. Timer của toast cũ không được đóng toast mới.
- Đăng nhập/đăng xuất phát kết quả tại useAuth; đăng ký/validation/API error tại AuthForm. Không phát success trước khi xác minh profile hoặc hoàn thành request.
- useCart phát đúng một thông báo kết quả cho thêm/sửa/xóa/xóa toàn bộ. Nếu chỉ cập nhật trong bộ nhớ do localStorage bị chặn, phát error cảnh báo lưu trữ thay vì success gây hiểu nhầm. Các request kiểm tra sản phẩm/tồn kho thất bại được thông báo từ component thao tác.
- Hủy request hoặc đóng confirm không phát lỗi. Loading, lỗi đọc dữ liệu, cảnh báo tồn kho và lưu trữ cần theo dõi vẫn hiển thị tại phần nội dung; không tạo success Toast cho các lần tải nền.
- Các banner thông báo kết quả thao tác cũ đã được bỏ để tránh lặp thông báo. Form validation HTML mặc định vẫn giữ nguyên.


## Checkout COD

Thanh toán tại /cart mở CheckoutDialog bằng query checkout=1. Component hiển thị CheckoutAddressFields (default/saved/new), CheckoutOrderSummary (quantity/unit price/line totals/total), COD mặc định, VNPay disabled và nút xác nhận. CheckoutAddressFields tự tải địa chỉ theo buyerId và đổ controlled values vào form; useCheckout quản lý POST idempotent, polling, recovery; checkoutModel chứa validation và sessionStorage helpers.

Giữ nguyên key/payload trước POST để retry; xác minh GET COMPLETED mới dọn các dòng giỏ còn khớp revision. Kết quả/tổng tiền sau gửi đọc từ backend. Login/Register hỗ trợ quay về checkout bằng đích cố định; giỏ khách vẫn riêng giỏ tài khoản. Xem trạng thái chi tiết trong Progress.md. Chưa kiểm thử backend hoặc native dialog trên trình duyệt thật.


### Địa chỉ checkout và Toast trong native dialog

CheckoutDialog truyền buyerId và onReady cho CheckoutAddressFields. Component này gọi getAddresses(buyerId, {signal}), chọn default và đổ tên/điện thoại/địa chỉ vào form; khi lỗi cho nhập tay, retry và hủy request cũ được hỗ trợ. Không gọi trùng địa chỉ trong useCheckout.

Toaster vẫn chỉ mount một lần tại main.jsx. registerToastHost(dialog) định tuyến portal vào native dialog đang mở để thông báo COMPLETED không bị modal che; cleanup trả Toast về ứng dụng. Success chỉ phát một lần cho mỗi yêu cầu hoàn tất, không phát thêm từ JSX condition.

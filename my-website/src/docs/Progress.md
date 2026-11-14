# Tiến độ Frontend — my-website

Cập nhật: **14/11/2026**, múi giờ Asia/Saigon.

## 1. Phạm vi và trạng thái tổng quan

Tài liệu ghi nhận frontend mới tại `my-website`, theo source hiện tại và công việc đã thực hiện trong phiên phát triển. Thư mục `frontend` cũ và các thay đổi backend/Docker không thuộc tiến độ của frontend mới này.

Luồng đã xây dựng bằng code: **Trang chủ → tìm kiếm/lọc → chi tiết sản phẩm → chọn variant → giỏ hàng → xác nhận checkout COD → đơn hàng → xem chi tiết/hủy đơn đủ điều kiện**. Đã có Auth, hồ sơ cá nhân, quản lý địa chỉ, đổi mật khẩu, trang shop công khai và xem đánh giá. Các phần mới chưa được kiểm tra tích hợp với backend đang chạy; không coi có code là đã nghiệm thu.

| Nhóm | Trạng thái | Nội dung |
| --- | --- | --- |
| Cấu trúc dự án | Đã có | Chia API, component và page theo nghiệp vụ; giữ cấu trúc đơn giản |
| Layout/Header/Footer | Đã triển khai | MainLayout, logo, tìm kiếm, điều hướng, tài khoản, bộ đếm giỏ, Footer |
| Trang chủ | Đã triển khai | Banner, danh mục và danh sách sản phẩm |
| Auth | Đã triển khai | Đăng nhập/đăng ký/đăng xuất, quên mật khẩu trong login, khôi phục/refresh phiên |
| Danh mục | Đã triển khai | CategoryCard → CategoryList, chọn danh mục để lọc |
| Tìm kiếm sản phẩm | Đã có trang riêng | /search, từ khóa/SKU, danh mục-con/shop/khoảng giá, sort và phân trang API; homepage vẫn có danh sách giới thiệu |
| Chi tiết sản phẩm | Đã triển khai | Gallery ảnh/video, variant, giá, SKU, thuộc tính, tồn kho, mô tả, thông tin shop |
| Bình luận/đánh giá | Đã có xem/tạo kèm media | Xem public/phân trang; gửi sao/bình luận/ảnh-video từ đơn DELIVERED, xử lý 409 trùng |
| Giỏ hàng | Đã triển khai | Thêm/cộng variant, sửa số lượng, xóa, tạm tính, lưu localStorage |
| Toast | Đã triển khai | Một Toaster toàn ứng dụng; Auth, giỏ, checkout, hồ sơ/địa chỉ và hủy đơn; hỗ trợ native dialog |
| Checkout/COD | Đã triển khai code | Mở form trước, xác nhận mới POST; polling, retry cùng key; thành công dọn mặt hàng đã mua và chuyển /orders |
| Đơn hàng người mua | Đã triển khai code | /orders, lọc trạng thái, dialog chi tiết, xác nhận hủy đơn đủ điều kiện và Toast |
| Hồ sơ/địa chỉ/mật khẩu | Đã triển khai code | /profile, xem tài khoản, CRUD địa chỉ, mặc định/đổi mật khẩu; quên mật khẩu ở login |
| Shop công khai | Đã triển khai code | /shops/:id, banner/logo tròn chồng banner, thông tin cơ bản và sản phẩm shop |
| Seller | Đã có 4 mục quản lý bằng code | Shop, sản phẩm/variant/tồn kho, đơn hàng và báo cáo demand/sales; đã có tạo product/thêm variant/bật-ngừng bán; còn nghiệm thu tích hợp |
| Admin | User, cửa hàng, danh mục và duyệt sản phẩm | Role ADMIN; quản lý user/shop/category; hàng đợi duyệt, chi tiết đủ variant/tên shop, duyệt/từ chối; tra cứu đơn và hủy đủ điều kiện |
| Kiểm thử tích hợp thực tế | Chưa xác minh | Chưa chạy xuyên suốt trên trình duyệt với backend/Kong thật |

> Có file API hoặc thư mục không có nghĩa là chức năng UI đã hoàn thành.

## 2. Công nghệ và nguyên tắc tổ chức

- React, JavaScript JSX, Vite, React Router, Tailwind CSS và Axios.
- Dùng `useState`, `useEffect`, các hook nhỏ phục vụ chức năng thực tế; chưa thêm Redux, Zustand hoặc React Query.
- Một Axios client dùng chung tại `src/api/axiosClient.js`, gọi Kong tại `http://localhost:8000`.
- Client mặc định timeout 10 giây; module Media có timeout riêng cho upload.
- Bearer token được gắn cho các request qua client chung; request public/Auth có thể dùng `skipAuth`.
- HTTP implementation nằm trong `src/api`; page và component gọi các hàm API đã có.
- `MainLayout` chia sẻ `{ auth, cart }` qua Outlet context; không thêm global provider cho giỏ hàng.
- Component dùng chung ở `components/common`; component nghiệp vụ ở `components/<nghiệp vụ>`; page ở `pages/<nghiệp vụ>`.
- Không tổ chức lại theo kiến trúc nhiều tầng phức tạp; chỉ tách thêm khi có trách nhiệm thực tế cần tách.

## 3. Routing hiện tại

| Route | Page | Layout | Chức năng |
| --- | --- | --- | --- |
| `/` | HomePage | MainLayout | Banner, danh mục, danh sách giới thiệu sản phẩm |
| `/search` | SearchPage | MainLayout | Tìm kiếm/lọc/sắp xếp/phân trang công khai; categoryId cũ được chuyển sang slug |
| `/search/danh-muc/:categorySlug` | SearchPage | MainLayout | Giải slug danh mục thành UUID để gọi API; không hiển thị UUID danh mục trên URL |
| `/products/:productSlug/:id` | ProductDetailPage | MainLayout | URL chuẩn có slug và UUID, xem chi tiết/đánh giá |
| `/products/:id` | ProductDetailPage | MainLayout | Tương thích URL cũ, đọc sản phẩm rồi replace sang URL chuẩn |
| `/shops` | ShopListPage | MainLayout | Danh sách shop công khai có phân trang, banner/logo/trạng thái |
| `/shops/:id` | ShopPage | MainLayout | Shop công khai, banner/logo/thông tin/sản phẩm |
| `/profile` | ProfilePage | MainLayout | Hồ sơ, địa chỉ và đổi mật khẩu; cần đăng nhập |
| `/orders` | OrdersPage | MainLayout | Đơn người mua, dialog chi tiết và hủy; cần đăng nhập |
| `/admin` | AdminHomePage | MainLayout + AdminLayout | Tổng quan và kiểm tra quyền ADMIN |
| `/admin/users` | AdminUsersPage | MainLayout + AdminLayout | Bảng, tìm email/điện thoại, sửa/cấm-bỏ cấm/xóa |
| `/admin/shops` | AdminShopsPage | MainLayout + AdminLayout | Bảng shop/phân trang/lọc cục bộ, chi tiết và đổi trạng thái |
| `/admin/categories` | AdminCategoriesPage | MainLayout + AdminLayout | Bảng/tìm kiếm/lọc, thêm/sửa/ảnh/phân cấp/ẩn-kích hoạt danh mục |
| `/admin/products` | AdminProductsPage | MainLayout + AdminLayout | Hàng đợi chờ duyệt, chi tiết sản phẩm/variant/tên shop, duyệt hoặc từ chối |
| `/admin/reports` | AdminRevenuePage | MainLayout + AdminLayout | Doanh thu DELIVERED tổng hợp server toàn phạm vi lọc, bảng shop phân trang và dialog đơn |
| `/admin/orders` | AdminOrdersPage | MainLayout + AdminLayout | Danh sách toàn hệ thống, lọc/phân trang server, chi tiết và hủy đủ điều kiện |
| `/seller` | Chuyển /seller/shop | MainLayout + SellerLayout | Kiểm tra phiên và role SELLER |
| `/seller/shop` | SellerShopPage | MainLayout + SellerLayout | Xem/tạo/sửa thông tin shop, upload logo/banner |
| `/seller/products` | SellerProductsPage | MainLayout + SellerLayout | Tìm kiếm/bảng, chi tiết, sửa/xóa product và variant |
| `/seller/orders` | SellerOrdersPage | MainLayout + SellerLayout | Lọc/phân trang đơn hàng, dialog chi tiết, giao hàng và hủy |
| `/seller/reports` | SellerReportsPage | MainLayout + SellerLayout | Demand/sales, lọc thời gian, biểu đồ, bảng kỳ và sản phẩm bán có phân trang |
| `/cart` | CartPage | MainLayout | Quản lý giỏ hàng |
| `/cart?checkout=1` | CheckoutDialog trong CartPage | MainLayout + native dialog | Xác nhận và theo dõi checkout COD; cần đăng nhập |
| `/login` | LoginPage | AuthLayout | Đăng nhập, không dùng Header/Footer |
| `/register` | RegisterPage | AuthLayout | Đăng ký, không dùng Header/Footer |

- Chưa có route /products cho danh sách; homepage vẫn ghép danh sách giới thiệu, /search là trang khám phá/tìm kiếm có phân trang riêng.
- Tìm kiếm dùng /search với q/categoryId/shopId/includeChildren/minPrice/maxPrice/sort/page/size. Header, CategoryList và liên kết cùng danh mục đã nối route mới; các URL lọc homepage cũ vẫn được ProductListPage hỗ trợ cơ bản.
- Checkout mở bằng query trên `/cart`, không tạo route `/checkout` riêng. Đã có /orders và /profile; đã có route Seller với 4 mục chờ; đã có route Admin với trang tổng quan/5 mục chờ; chưa có trang 404 chung. ScrollToTop đang được mount trong BrowserRouter: đổi pathname cuộn lên đầu; đổi query mở checkout không cuộn lại.

## 4. Chi tiết phần đã phát triển

### 4.1. Layout, Header và Footer

File chính: `components/common/MainLayout.jsx`, `Header.jsx`, `Footer.jsx`.

- Header có logo Market Lane, ô tìm kiếm và điều hướng trang chủ/danh mục/sản phẩm.
- Header tìm kiếm trim từ khóa và chuyển /search. Khi đang /search giữ bộ lọc hợp lệ đã có và reset page; từ trang khác bắt đầu tìm kiếm toàn catalog, không mang query checkout/Seller sang.
- Hiển thị liên kết đăng nhập/đăng ký hoặc email/nút đăng xuất theo trạng thái Auth.
- Link `/cart` có bộ đếm tổng số lượng, rút gọn badge khi vượt 99.
- Footer có giới thiệu, liên kết khám phá và quay về nội dung chính.
- MainLayout dùng flex để Footer nằm cuối màn hình khi nội dung ngắn.
- Có nhãn truy cập, skip link, focus và các utility responsive. Chưa xác minh trực quan đầy đủ ở các kích thước màn hình.

### 4.2. Đăng nhập, đăng ký và phiên tài khoản

File chính: `pages/auth/{LoginPage,RegisterPage}.jsx`, `components/auth/{AuthLayout,AuthForm,AccountMenu}.jsx`, `useAuth.js`, `authHelpers.js`.

**Đăng ký:**

- Nhập email, điện thoại, mật khẩu và xác nhận mật khẩu.
- Email tối đa 100 ký tự; điện thoại 10 số bắt đầu bằng 0; mật khẩu đăng ký 8–20 ký tự.
- Kiểm tra hai mật khẩu khớp nhau; chỉ gửi email/password/phone tới backend.
- Thành công chuyển về `/login` và điền email vừa đăng ký, không tự đăng nhập.

**Đăng nhập:**

- Gọi `/auth/login`, kiểm tra đủ userId/accessToken/refreshToken.
- Dùng token mới gọi `/auth/me` trước khi lưu phiên.
- Chỉ chấp nhận hồ sơ ACTIVE, userId khớp và role BUYER/SELLER/ADMIN hợp lệ.
- Mật khẩu giữ nguyên, không trim; không áp giới hạn tối thiểu 8 ký tự cho login để hỗ trợ mật khẩu reset hiện có.
- Có hiện/ẩn mật khẩu, trạng thái pending và chặn submit trùng.
- Đăng nhập thành công quay về trang chủ hoặc đích nội bộ `/cart?checkout=1`, `/profile`, `/orders` theo thao tác trước đó. Chỉ nhận đích quay lại nội bộ cố định, chưa có khu vực riêng theo role.

**Khôi phục/đăng xuất:**

- Token lưu qua `api/auth/authSession.js` trong sessionStorage của tab.
- Reload xác minh lại `/auth/me`; lỗi 401 dùng cơ chế refresh chung rồi đọc lại profile. Đổi URL (pathname/query/hash và lịch sử điều hướng) kiểm tra exp của access token; nếu còn tối đa 60 giây hoặc đã hết hạn thì chủ động refresh với refresh token hiện tại.
- Phiên/tài khoản không hợp lệ bị xóa; lỗi mạng có trạng thái để thử lại.
- Đăng xuất xóa phiên local ngay, sau đó yêu cầu thu hồi refresh token; báo rõ nếu thu hồi server thất bại.
- Request bị hủy khi rời form; không phát Toast lỗi cho thao tác bị hủy.
- SellerLayout đã kiểm tra phiên và role SELLER trước khi render nội dung quản lý; AdminLayout cũng kiểm tra phiên và role ADMIN trước khi render khu vực quản trị.

### 4.3. Danh mục

File chính: `components/category/CategoryCard.jsx`, `CategoryList.jsx`.

- Đọc `/categories`; hiển thị danh mục ACTIVE, sắp theo displayOrder rồi tên.
- Card gồm ảnh/tên, có ảnh dự phòng và trạng thái đang chọn.
- Chọn card mở /search/danh-muc/:categorySlug, giữ q nếu có và về trang đầu. includeChildren=true mặc định nên ẩn khỏi URL, false vẫn giữ.
- Danh sách sản phẩm gửi `includeChildren=true` khi lọc danh mục.
- Nút Tất cả sản phẩm bỏ bộ lọc danh mục.
- Có loading/error/empty, thử lại và hủy request cũ.

**Thông tin danh mục trong API sản phẩm đã đối chiếu:**

| API | Dữ liệu danh mục |
| --- | --- |
| `GET /api/products/{id}` | Có categoryId, chưa có categoryName hoặc object category đầy đủ |
| `GET /api/products` | Có categoryId trên mỗi sản phẩm |
| `GET /api/products/search` | Có categoryId và categoryName |
| `GET /categories/{id}` | Lấy chi tiết danh mục bằng categoryId |

Frontend đã có `getCategory(id)`. Nếu cần tên danh mục trên breadcrumb chi tiết, có thể dùng hàm này; hiện UI chi tiết có link khám phá cùng danh mục bằng categoryId.

### 4.4. Trang chủ và danh sách sản phẩm

File chính: `pages/home/HomePage.jsx`, `pages/product/ProductListPage.jsx`, `components/product/ProductCard.jsx`.

- Trang chủ ghép banner, CategoryList và ProductListPage theo bố cục hiện tại.
- Dùng `/api/products` khi không có từ khóa; dùng `/api/products/search` khi tìm kiếm.
- Kết hợp keyword/categoryId/shopId; đổi bộ lọc sẽ tải lại và bỏ dữ liệu cũ.
- ProductCard hiển thị ảnh, tên, giá và tên shop khi response có dữ liệu.
- Thumbnail chọn ảnh thay vì cố hiển thị video như ảnh; có fallback khi thiếu/lỗi ảnh.
- Bấm card dùng productPath: /products/:slug/:id. Ưu tiên slug backend; search DTO hoặc dữ liệu giỏ/đơn thiếu slug thì tạo phần tên từ tên sản phẩm, detail sẽ canonical về slug backend sau khi đọc bằng ID.
- Có loading/error/empty, thử lại và AbortController.
- Trang chủ ProductListPage vẫn là danh sách giới thiệu cơ bản. SearchPage đã có phân trang/lọc giá/sort trực tiếp theo API, không lọc riêng trên một trang dữ liệu.

### 4.5. Chi tiết sản phẩm

File chính: `pages/product/ProductDetailPage.jsx`, `components/product/{ProductInfo,ProductGallery,ProductShop,VariantStock}.jsx`, `productHelpers.js`.

- Đọc `/api/products/{id}`; kiểm tra response khớp ID; xử lý loading/404/error và thử lại.
- Đổi sản phẩm reset state và cuộn lên đầu trang.
- Gallery gồm ảnh/video từ mediaJson và ảnh các variant; chọn thumbnail và xử lý media lỗi.
- Video có controls, không tự phát.
- Chỉ hiển thị variant active, chưa bị xóa; mặc định chọn variant đầu tiên.
- Chọn variant đổi giá, ảnh, SKU và thuộc tính; tên ưu tiên variantName rồi attributesJson/SKU.
- Đọc tồn kho công khai của variant đang chọn; phân biệt đang tải/lỗi/hết hàng/còn hàng và cho làm mới.
- Hiển thị soldCount, mô tả text an toàn, không render HTML thô.
- ProductShop tải tên/logo/điểm đánh giá của shop nếu có; lỗi shop không chặn chi tiết sản phẩm.
- Link Xem cửa hàng mở /shops/:id; CategorySearchLink lấy danh mục bằng categoryId rồi mở /search/danh-muc/:slug.
- Giá/tồn kho lúc xem không có nghĩa là đã giữ hàng.

### 4.6. Bình luận và đánh giá

File chính: `components/product/ProductReviewCard.jsx`, `ReviewItem.jsx`, `api/order/reviewApi.js`.

Trang chi tiết gọi:

```jsx
<ProductReviewCard productId={result.product.id} />
```

- Component nhận productId và tự đọc `/api/reviews/products/{productId}`; ai cũng xem được.
- Hiển thị buyerName, rating, comment, thời gian và mediaUrls.
- Tên dự phòng khi thiếu; thời gian hiển thị theo giờ Việt Nam.
- Ảnh mở tab mới; video có controls; có fallback media lỗi.
- Hiển thị totalElements và averageRating do backend trả, không tính trung bình riêng các review trên trang hiện tại.
- Phân trang 5 review/trang, page bắt đầu từ 0; có loading/error/empty và thử lại.
- Đổi productId reset page và hủy request cũ.
- Response public không dùng buyerId/orderId.
- Đã nối tạo review từ OrderDetailDialog của đơn DELIVERED bằng OrderReviews/OrderReviewForm: chọn 1–5 sao, bình luận bắt buộc tối đa 2000 ký tự, upload media trước, rồi gửi orderId/productId/rating/comment/mediaUrls (URL, không phải file).
- Gom các variant cùng product trong một đơn thành một lượt đánh giá. Khóa submit trùng và đóng/cập nhật dialog trong khi gửi; giữ nội dung khi lỗi.
- POST thành công: Toast, ẩn form, đánh dấu đã gửi và có link về sản phẩm để xem review. HTTP 409: đánh dấu đã đánh giá, không cho gửi lại trong dialog hiện tại; HTTP 403 thông báo điều kiện mua/giao hàng chưa đạt.
- API public không có orderId/buyerId và chưa có API đọc trạng thái review của từng đơn. Không suy đoán từ tên người mua/public reviews; mở lại dialog có thể vẫn thấy nút, backend unique constraint là nguồn quyết định cuối cùng.
- Đã thêm ReviewMediaFields: chọn tối đa 6 tệp JPG/PNG/WebP/MP4, mỗi tệp tối đa 10 MB theo cấu hình Media hiện tại; xem trước, video controls, bỏ tệp trước khi gửi. Blob URL được thu hồi khi đổi/bỏ tệp hoặc unmount.
- Bấm Gửi đánh giá mới upload tuần tự qua /api/media/upload (domain=reviews, entityId=orderId); đủ URL mới POST review. Upload lỗi không POST review. URL của tệp upload thành công được giữ trong form để retry phần còn lại; chọn/bỏ tệp bị khóa trong khi gửi.
- Trạng thái đã gửi/trùng giữ trong OrderDetailDialog, không lưu local để giả lập trạng thái backend. Backend chưa có API xóa upload nháp: bỏ tệp đã upload hoặc rời form sau lỗi không xóa file vật lý; cần cơ chế dọn file mồ côi ở bước backend sau này.

### 4.7. Giỏ hàng và thêm hàng

File chính: `pages/cart/CartPage.jsx`, `components/cart/{AddToCart,CartItem}.jsx`, `useCart.js`, `useCartAvailability.js`, `cartModel.js`, `cartRequests.js`.

**Thêm và cập nhật:**

- ProductInfo ghép AddToCart vào VariantStock cho variant đang chọn.
- Có ô quantity và nút Thêm vào giỏ; khóa khi pending, phiên chưa xác minh hoặc đã đạt tồn kho quan sát được.
- Trước khi thêm/sửa, đọc lại product detail và public inventory để xác nhận còn bán, đúng variant, giá và lượng khả dụng.
- Mỗi variant là một dòng; thêm lại cộng quantity, cập nhật snapshot tên/ảnh/giá mới.
- Snapshot dùng ảnh của variant, nếu thiếu dùng ảnh sản phẩm; không dùng video làm thumbnail.
- Quantity nguyên dương trong int32; tối đa 100 variant khác nhau; tổng quantity của variant không vượt tồn kho vừa kiểm tra.
- Chặn thao tác khi tài khoản thay đổi; async update không tái tạo dòng đã xóa.

**Trang `/cart`:**

- Xem tên, variant, ảnh, đơn giá, số lượng và thành tiền.
- Sửa quantity, xóa từng dòng và xóa toàn bộ; thao tác xóa có xác nhận.
- Kiểm tra lại dữ liệu khi mở/thay đổi mặt hàng và khi bấm Làm mới.
- Những variant cùng product dùng chung một lần đọc product trong đợt kiểm tra giỏ.
- Hiển thị giá hiện tại khi đọc được, cảnh báo giá đổi, lỗi sản phẩm, hết hàng hoặc quantity vượt tồn kho.
- Có tạm tính và nút Thanh toán mở checkout COD. Tổng tiền/phí vận chuyển sau khi tạo đơn lấy từ backend.

**Lưu trữ và tài khoản:**

- Backend hiện chưa có giỏ hàng server; không tạo endpoint giả.
- Lưu localStorage với khóa `market-lane-cart-v1:<owner>`; owner là guest hoặc userId.
- Tách giỏ khách/từng tài khoản; không tự gộp giỏ khách sau login; logout không xóa giỏ của tài khoản.
- Chuẩn hóa dữ liệu đọc vào, bỏ dòng lỗi/trùng, chỉ lưu trường hiển thị, không lưu token trong giỏ.
- Storage bị chặn/lỗi: báo rõ, giữ giỏ trong bộ nhớ của layout đang mở.
- Storage event đồng bộ thay đổi giữa các tab cùng giỏ, không phải khóa giao dịch nhiều tab hay đồng bộ đa thiết bị.
- Thêm vào giỏ không giữ hàng; giá và reservation phải được backend xác nhận lúc checkout.

### 4.8. Toast dùng chung

File chính: `components/common/toast.js`, `Toaster.jsx`; mount một lần tại `src/main.jsx` cùng cấp App.

```js
import { toast } from "../../components/common/toast.js"; // sửa relative path theo vị trí file

toast.success("Đã lưu thay đổi.");
toast.error("Không thể hoàn tất. Vui lòng thử lại.");
```

- Một thông báo tại một thời điểm; thông báo mới thay thông báo trước.
- Hiện góc trên phải, co giãn trên mobile, có nút đóng và live region cho screen reader.
- Tạm dừng khi hover/focus; callback đóng của Toast cũ không đóng Toast mới.
- **Cấu hình source hiện tại:** successDurationMs = 2000 và errorDurationMs = 2000. Sửa thời lượng tập trung tại toastConfig.
- Dùng cho login/register/logout, validation, giỏ, checkout, đổi mật khẩu, CRUD địa chỉ và hủy đơn. Toast nổi trên ConfirmDialog và được portal vào native dialog đang mở.
- Không phát thành công trước khi API/xác minh/commit hoàn tất; lỗi lưu localStorage phát cảnh báo error thay vì success gây hiểu nhầm.
- Đã bỏ banner kết quả thao tác trùng lặp. Loading, lỗi đọc dữ liệu và cảnh báo cần theo dõi tiếp tục hiển thị tại nội dung.
- Hủy request hoặc hủy confirm không phát lỗi; không tự bật Toast từ Axios interceptor.

### 4.9. Checkout COD

File chính: `components/checkout/CheckoutDialog.jsx`, `CheckoutAddressFields.jsx`, `CheckoutOrderSummary.jsx`, `useCheckout.js`, `checkoutModel.js`.

- Bấm Thanh toán tại `/cart` chỉ mở form trong native dialog bằng query `checkout=1`, tải địa chỉ và kiểm tra giá/tồn; không tự POST. Chỉ bấm Xác nhận đặt hàng mới gửi yêu cầu.
- Cần đăng nhập, bảo toàn đích quay lại qua Login/Register. Giỏ khách và giỏ tài khoản tiếp tục tách riêng, không tự gộp; sau login dùng giỏ tài khoản.
- Đọc địa chỉ của user; ưu tiên isDefault/default, nếu không có thì chọn địa chỉ đầu tiên. Có thể chọn địa chỉ khác hoặc nhập mới. Lỗi tải địa chỉ vẫn cho nhập tay.
- Hiển thị người nhận, số điện thoại và địa chỉ; địa chỉ mới chỉ dùng cho đơn này, không tự lưu vào danh sách địa chỉ.
- Bảng mặt hàng có tên/variant, quantity, đơn giá, thành tiền; tiền hàng, phí vận chuyển và tổng. Trước gửi là tạm tính, sau gửi đọc OrderResult để dùng số tiền server.
- COD chọn mặc định, VNPay hiển thị disabled. Payload luôn COD, chỉ gửi variantId/quantity và shippingAddress; không dùng giá client làm giá đặt hàng.
- Trước POST lưu nguyên payload/key/lines trong sessionStorage theo buyer. Nếu không lưu được thì không gửi. Request timeout/5xx giữ nguyên intent để người dùng retry cùng key.
- HTTP 400 từ backend hiện tại được coi là từ chối trước khi lưu, cho sửa/gửi yêu cầu mới; lỗi chưa rõ kết quả không tự tạo key khác.
- Khi POST được chấp nhận, kiểm tra checkoutId/buyerId/key/COD và đơn hàng tương ứng. Poll GET checkout mỗi 2,5 giây đến COMPLETED/FAILED/EXPIRED; đọc đơn để xác minh tổng tiền.
- Chỉ sau GET xác nhận COMPLETED mới Toast thành công một lần, dọn giỏ theo snapshot/revision, xóa intent và điều hướng /orders bằng replace (dialog đóng theo route). COD hoàn tất không có nghĩa đã thanh toán.
- Dòng giỏ có revision; chỉ xóa dòng khớp productId/variantId/quantity/revision đã gửi. Dòng đã sửa hoặc xóa rồi thêm lại được giữ.
- Đóng hoặc reload khi chưa rõ kết quả vẫn giữ yêu cầu. Mở lại không tự tiếp tục đơn cũ: người dùng bấm Kiểm tra yêu cầu trước đó; nếu chưa rõ kết quả thì chặn tạo yêu cầu mới. Có nút khôi phục ngay cả khi giỏ trống. FAILED/EXPIRED giữ giỏ; chỉ xóa intent sau khi xác nhận kết quả kết thúc.
- Dialog khóa nút xác nhận khi gửi, thiếu địa chỉ, dữ liệu/tồn kho đang kiểm tra hoặc có mặt hàng lỗi. Toast dùng chung báo kết quả.
- Kiểm tra SSR đã xác nhận default address được chọn, COD checked, VNPay disabled và phần xác nhận render được. Chưa kiểm tra mở dialog/focus/tương tác trên trình duyệt hoặc đặt đơn với backend thật.

### 4.10. Hồ sơ, địa chỉ và mật khẩu

File chính: `pages/profile/ProfilePage.jsx`, `components/profile/{ProfileInfo,ProfileAddresses,AddressForm,ChangePasswordForm}.jsx`.

- /profile cần phiên xác minh; dùng dữ liệu /auth/me từ useAuth: email, phone, role, status. Chưa có API chỉnh sửa thông tin cơ bản được nối UI.
- Đọc danh sách địa chỉ, ưu tiên mặc định; thêm/sửa người nhận, điện thoại, khu vực, chi tiết và isDefault; xóa có ConfirmDialog.
- Tải lại danh sách từ backend sau mutation, khóa gửi trùng, hủy request khi unmount; Toast thành công/lỗi. Địa chỉ mới nhập riêng ở checkout vẫn không tự lưu vào hồ sơ.
- Đổi mật khẩu gửi oldPassword/newPassword; kiểm tra 8–20 ký tự, xác nhận khớp, mới khác cũ. Giữ nguyên ký tự mật khẩu; thành công reset form.
- Đã có UI quên mật khẩu ngay trong LoginPage, nhập email và gọi PUT /auth/forgotpassword; sau thành công quay lại login. Backend ChangePasswordRequest hiện yêu cầu cả mật khẩu cũ tối thiểu 8 ký tự, còn luồng forgot password sinh 6 chữ số: cần thống nhất backend trước khi hoàn thiện trường hợp đổi mật khẩu sau reset.

### 4.11. Đơn hàng người mua

File chính: `pages/order/OrdersPage.jsx`, `components/order/{OrderCard,OrderDetailDialog}.jsx`, `orderHelpers.js`.

- GET /api/orders, sắp xếp mới nhất và lọc trạng thái ở frontend; chưa có phân trang API/UI cho danh sách này.
- Card: mã đơn, thời gian, tên sản phẩm/variant/số lượng, trạng thái đơn/thanh toán, tổng tiền.
- Bấm card mở native dialog, gọi GET chi tiết mới; hiển thị shippingAddress, ngày đặt/cập nhật, ảnh biến thể, đơn giá/số lượng/thành tiền, subtotal/shippingFee/totalAmount.
- Hủy sau xác nhận, chỉ hiện với COD + CONFIRMED + chưa PAID, backend quyết định cuối cùng; POST không có body lý do hủy.
- Thành công cập nhật dialog và tải lại danh sách; lỗi Toast rồi đọc lại trạng thái. Không tự gửi lại thao tác hủy.
- Có nút cập nhật trạng thái; chưa tự polling đơn đang giao, chưa có timeline vận đơn. Đã có phần tạo review cho đơn DELIVERED.

### 4.12. Shop công khai

File chính: `pages/shop/ShopPage.jsx`, `components/shop/{ShopHeader,ShopProducts}.jsx`.

- /shops/:id dùng GET /shops/{id} public; kiểm tra ID response, xử lý 404/loading/error/thử lại.
- Banner lớn, logo tròn nằm chồng cạnh dưới banner; tên, mô tả, địa chỉ, trạng thái, số sản phẩm, điểm/số đánh giá và ngày tham gia. Thiếu/lỗi ảnh có fallback.
- GET /api/products?shopId={id} cho danh sách shop hiện tại; thêm link Tìm kiếm sản phẩm trong shop mở /search?shopId=... để tìm kiếm/lọc giá/sort/phân trang theo shop.
- ProductShop ở detail đã dẫn đến trang shop; chưa có trang danh sách tất cả shop.

## 5. API đã được nối vào giao diện

| API | Vị trí sử dụng |
| --- | --- |
| `POST /auth/register` | Đăng ký |
| `POST /auth/login` | Đăng nhập |
| `PUT /auth/forgotpassword` | Quên mật khẩu trong LoginPage |
| `GET /shops` | Danh sách shop công khai phân trang |
| `GET /auth/me` | Xác minh user sau login/khôi phục phiên |
| `POST /auth/refresh` | Refresh khi khôi phục gặp 401 |
| `POST /auth/logout` | Thu hồi phiên khi logout |
| `GET /categories` | CategoryList |
| `GET /api/products` | Danh sách và lọc không có từ khóa |
| `GET /api/products/seller/shops/{shopId}` | Bảng sản phẩm Seller |
| `POST /api/products/seller/shops/{shopId}` | Tạo sản phẩm nháp |
| `POST /api/products/seller/shops/{shopId}/{id}/variants` | Thêm variant |
| `PATCH /api/products/seller/shops/{shopId}/{id}/status` | Bật/ngừng bán sản phẩm |
| `GET /api/products/seller/shops/{shopId}/{id}` | Dialog chi tiết Seller |
| `PUT /api/products/seller/shops/{shopId}/{id}` | Sửa thông tin sản phẩm |
| `DELETE /api/products/seller/shops/{shopId}/{id}` | Xóa sản phẩm sau xác nhận |
| `PUT /api/products/seller/shops/{shopId}/{id}/variants/{variantId}` | Sửa variant, thuộc tính/trạng thái/ảnh |
| `DELETE /api/products/seller/shops/{shopId}/{id}/variants/{variantId}` | Xóa variant sau xác nhận |
| `GET /api/products/search` | SearchPage: từ khóa/SKU/danh mục/shop/giá/sort/page/size; ProductListPage cũ vẫn dùng khi có q |
| `GET /api/products/{id}` | Detail và kiểm tra mặt hàng trong giỏ |
| `GET /shops/{shopId}` | ProductShop và ShopPage |
| `GET /api/shops/seller/my-shop` | Thông tin cửa hàng Seller |
| `POST /api/shops/seller` | Seller tạo shop nếu chưa có |
| `PUT /api/shops/seller/{shopId}` | Seller cập nhật hồ sơ shop |
| `GET /api/inventories/variant/{variantId}` | VariantStock và kiểm tra giỏ |
| `GET /api/inventories/seller?shopId={shopId}` | Tồn kho toàn bộ variant trong dialog sản phẩm Seller |
| `PATCH /api/inventories/seller/variants/{variantId}/stock` | Đặt tổng tồn thực tế sau xác nhận |
| `GET /api/reviews/products/{productId}` | ProductReviewCard |
| `POST /api/reviews` | OrderReviewForm trong chi tiết đơn DELIVERED |
| `POST /api/media/upload` | Upload ảnh/video đánh giá trước POST review |
| `GET /auth/users/{userId}/addresses` | Checkout và hồ sơ |
| `POST /auth/users/{userId}/addresses` | Thêm địa chỉ |
| `PUT /auth/users/{userId}/addresses/{addressId}` | Sửa địa chỉ/mặc định |
| `DELETE /auth/users/{userId}/addresses/{addressId}` | Xóa địa chỉ |
| `PUT /auth/changepassword` | Đổi mật khẩu |
| `GET /api/orders` | Danh sách đơn người mua |
| `GET /api/orders/{id}` | Dialog chi tiết đơn |
| `POST /api/orders/{id}/cancel` | Hủy đơn người mua |
| `GET /api/orders/seller/shops/{shopId}` | Đơn Seller phân trang/lọc ngày-trạng thái |
| `GET /api/orders/seller/{id}` | Chi tiết đơn Seller |
| `POST /api/orders/seller/{id}/ship` | Seller giao đơn cho vận chuyển |
| `POST /api/orders/seller/{id}/cancel` | Seller hủy đơn COD đủ điều kiện |
| `GET /api/orders/seller/shops/{shopId}/reports/demand` | Thống kê đặt/hủy theo createdAt |
| `GET /api/orders/seller/shops/{shopId}/reports/sales` | Doanh thu DELIVERED theo updatedAt, phân trang variant |
| `POST /api/checkouts` | Xác nhận checkout COD với Idempotency-Key |
| `GET /api/checkouts/{id}` | Theo dõi trạng thái checkout |
| `GET /api/orders?checkoutId={id}` | Xác minh đơn/tổng tiền thuộc checkout hiện tại |

Các màn hình Seller Shop/Product/Orders/Reports và quên mật khẩu đã nối API. Admin, quản trị Category, trang Inventory/Reservation riêng và Shipping vẫn chưa có UI tương ứng. reviewApi.js đã có getProductReviews và createReview; POST /api/reviews được nối từ đơn đã giao. Media API có uploadMedia và alias uploadImage cho tương thích. Review đã có UI upload JPG/PNG/WebP/MP4, tối đa 10 MB/tệp; SellerShopForm đã có UI upload logo/banner ảnh; dialog Seller đã có upload ảnh khi sửa variant; media sản phẩm đã có chọn/upload/đổi thứ tự/bỏ trong form sửa; Seller tạo product bắt buộc kèm ít nhất một variant trong cùng POST; có media và tiếp tục thêm variant riêng trong chi tiết. Slug tạo mới tự sinh từ tên sản phẩm + shopId. MediaController hiện chấp nhận domain reviews và trả trực tiếp {url}.

Xem chi tiết contract tại [api-contracts.md](api-contracts.md).

API mới nối từ AdminUsersPage/AdminUserDialog:

- GET /api/users/admin?keyword=&page=&size= và GET /api/users/admin/{userId}.
- PUT /api/users/admin/{userId} với email/phone; PATCH /status với ACTIVE/LOCKED; DELETE xóa sau xác nhận.
- Hai API sửa/status mới được bổ sung trong auth_service; cần khởi động lại backend. Contract cập nhật tại API-FRONTEND.md và api-contracts.md.

## 6. Kiểm tra đã ghi nhận

- Sau quản trị user: **53/53 test frontend đạt**, build thành công (cảnh báo bundle >500 KB vẫn còn). Lint đạt. Chưa kiểm tra E2E với backend thật. Build thành công; cảnh báo bundle lớn hơn 500 KB vẫn còn.
- Lint đã đạt sau khi bỏ hai initializer thừa gây no-useless-assignment ở ShopProducts/ShopPage.
- Test mới bao phủ productPath ưu tiên productId thay order-item id, slug trùng giữa shop vẫn phân biệt bằng UUID, chuyển URL danh mục cũ, bảo toàn query/bộ lọc, includeChildren mặc định/false, slug không tồn tại/inactive/trùng và mâu thuẫn ID-slug.
- Kết quả test là Node/SSR/adapter giả lập; chưa xác minh chuyển URL, reload/back/forward trên trình duyệt với backend/Kong thật và chưa chạy E2E toàn bộ nghiệp vụ Seller.
- Các ghi chú “không chạy test/build” ở lịch sử từng bước là trạng thái tại thời điểm đó, không phải kết quả mới nhất.

## 7. Phần còn thiếu và điểm cần xử lý

- Tạo review sao/bình luận/media và xử lý trùng đã có code; còn API trạng thái đã đánh giá theo từng đơn nếu muốn biết trước khi gửi. Cần thiết kế dọn upload nháp/mồ côi, tránh xóa nhầm media review được lưu ở DB order_service.
- Seller đã có khung route/nav và kiểm tra role, AccountMenu đã sửa thành SELLER và hiển thị Quản lý cửa hàng. Thông tin cửa hàng đã có nghiệp vụ xem/tạo/sửa và upload logo/banner; Quản lý sản phẩm đã có tìm kiếm/bảng/chi tiết/sửa/xóa; Orders đã có danh sách/chi tiết/giao hàng/hủy, Reports đã nối demand/sales; Admin đã có khung/guard và quản lý user/shop/category; product/order Admin còn chờ.
- UI quên mật khẩu đã có; cần thống nhất giới hạn mật khẩu cũ với mật khẩu reset 6 chữ số của backend.
- /search có phân trang/sort/lọc giá/danh mục/shop, Header có gợi ý sản phẩm từ API search. /shops đã có danh sách shop riêng. Còn lịch sử tìm kiếm; chưa bổ sung tìm kiếm từ khóa backend cho Seller Orders.
- Chưa có route 404 chung, breadcrumb đầy đủ hoặc trạng thái vận đơn cho người mua. Danh sách shop riêng đã có tại /shops.
- Đã có refresh chủ động khi đổi URL với token sắp hết hạn (60 giây) và refresh chung khi restore 401. Request nghiệp vụ chờ refresh đang chạy trước khi gắn Bearer; chưa tự refresh/replay request nghiệp vụ khi nhận 401 nếu không có chuyển URL. Không tự gửi lại lệnh ghi.
- Axios baseURL đang hardcode localhost:8000; cần cấu hình bằng biến môi trường khi đổi môi trường chạy.
- Giỏ cục bộ theo account/trình duyệt, không đồng bộ server/thiết bị, không tự gộp guest; tồn kho chỉ được giữ khi backend reservation thành công.
- COD là phương thức đang phát triển; VNPay bị disabled. Thanh toán trực tuyến chưa thuộc kế hoạch hiện tại.

## 8. Chiến lược phát triển tiếp theo

### Giai đoạn 1 — Hoàn thiện vòng đời người mua

**Đã có code tạo đánh giá sao/bình luận và ảnh/video từ đơn đã giao; còn kiểm tra tích hợp và quản lý file nháp.**

- Đã có nút Đánh giá cho từng product trong đơn DELIVERED ở dialog chi tiết; nhiều variant cùng product được gom theo orderId + productId.
- Đã có form sao 1–5, bình luận bắt buộc tối đa 2000 ký tự và createReview gọi POST /api/reviews theo contract thực tế; chưa chạy kiểm tra tích hợp.
- Đã nối upload ảnh/video qua Media hiện có, chỉ gửi URL vào review; không gửi file vào order_service. Cần xác minh thực tế URL qua Kong, retry khi upload dở và chính sách dọn file nháp trước nghiệm thu.
- Đã xử lý 403/409, giữ nội dung ở lỗi có thể sửa/thử lại, Toast sau thành công. Không giả định frontend biết đã đánh giá nếu API không trả dữ liệu đó.
- Hoàn thiện UI quên mật khẩu sau khi thống nhất ràng buộc reset/change password; bổ sung trang 404 và sửa điều hướng role.

Điều kiện hoàn tất: người mua xem được đơn vừa đặt, hủy đúng điều kiện, đánh giá đúng sản phẩm đã giao và không tạo trùng review.

### Giai đoạn 2 — Xây khu vực Seller theo từng lát chức năng

| Thứ tự | Phần xây dựng | Kết quả cần đạt |
| --- | --- | --- |
| 1 | SellerLayout + kiểm tra role (đã có code) | /seller giữ Header/Footer; nav 4 mục chờ, chỉ SELLER xem; chưa chạy kiểm tra tích hợp |
| 2 | Shop của tôi (đã có code) | Tải/tạo/cập nhật shop, upload logo/banner, xem trước và mở trang shop công khai; chưa kiểm tra tích hợp |
| 3 | Product/Variant (đã có code các thao tác chính) | Bảng/tìm kiếm/dialog/tạo/sửa/xóa, thêm variant và bật/ngừng bán sản phẩm; kiểm tra duyệt và tồn kho trước khi bán |
| 4 | Inventory (đã nối trong dialog product) | Xem onHand/reserved/available, điều chỉnh tổng tồn thực tế sau xác nhận; chưa có trang Inventory riêng hoặc quyền khởi tạo kho cho Seller |
| 5 | Seller Orders (đã có code) | Phân trang/lọc ngày-trạng thái, dialog chi tiết, giao vận/hủy có xác nhận; chưa kiểm tra tích hợp |
| 6 | Reports (đã có code) | Demand DAY/WEEK/MONTH; sales DAY/WEEK/MONTH/YEAR, thẻ/biểu đồ/bảng và phân trang sản phẩm bán; chưa kiểm tra tích hợp |

Trước mỗi phần, đối chiếu controller/contract và API module; bổ sung hàm API còn thiếu, không coi module API chuẩn bị là UI hoàn thành. Giữ cấu trúc đơn giản đang dùng.

### Giai đoạn 3 — Admin

- AdminLayout/guard, quản lý user/shop/category và duyệt sản phẩm đã có. Đã có API danh sách đơn Admin toàn hệ thống và tổng hợp doanh thu; tiếp theo nghiệm thu tích hợp.
- Tiếp theo đơn hàng/tồn kho theo nhu cầu quản trị thực tế. Không đưa chức năng cấp quyền Admin công khai vào UI.

### Giai đoạn 4 — Khả năng tìm kiếm và trải nghiệm

- Phân trang/sort/lọc giá và tìm kiếm trong shop đã có qua /search. Gợi ý search đã có; tiếp theo lịch sử tìm kiếm nếu cần, breadcrumb và kiểm tra chất lượng kết quả thực tế.
- Cấu hình API qua biến môi trường; refresh khi chuyển URL đã có khóa dùng chung. Nếu bổ sung xử lý 401 cho request nghiệp vụ sau này, cần tránh replay lệnh ghi không an toàn.
- Rà focus/keyboard cho menu/dialog, responsive, media lỗi và thông báo dài; mở rộng component dùng chung khi thật sự lặp lại.

### Kiểm tra tích hợp — Cổng nghiệm thu, chưa thực hiện

Khi có yêu cầu cho phép kiểm tra trở lại, ưu tiên xác minh luồng hiện có trước khi mở rộng quá nhiều:

- Auth → địa chỉ → giỏ → checkout COD nhiều shop → /orders → dialog → hủy; tải lại/mất mạng và cùng Idempotency-Key không tạo trùng.
- Sửa/xóa/thêm lại mặt hàng khi checkout đang chờ; chỉ dọn dòng khớp snapshot sau COMPLETED.
- Địa chỉ mặc định thay đổi, mật khẩu sai, đơn đổi trạng thái trong lúc xác nhận hủy, tài khoản đổi/đăng xuất giữa request.
- Product/Shop public không cần token; ảnh/video từ URL backend mở đúng qua Kong.
- Mobile/desktop, cuộn khi đổi route, focus và Toast trên native dialog.

Hiện tiếp tục giữ yêu cầu không chạy test/build; bảng chiến lược không phải xác nhận các bước kiểm tra đã chạy.

## 9. Quy tắc cập nhật tiến độ

- Sau mỗi nhóm chức năng, cập nhật trạng thái, route, file chính, API được nối và kết quả kiểm tra thực tế.
- Phân biệt rõ: đã có thư mục/API; đã có UI; đã qua lint/build/unit test; đã chạy thật với backend.
- Không sửa backend hoặc thêm endpoint chỉ để khớp giả định frontend; kiểm tra contract/controller trước.
- Giữ cấu trúc hiện tại, không cài thêm state library hoặc tách nhiều lớp khi chưa có nhu cầu.
- Tài liệu cấu trúc bổ sung: [structure.md](structure.md). Khi thông số khác giữa ghi chú cũ và source, đối chiếu source hiện tại và cập nhật cùng thay đổi.

## 10. Danh mục file hiện tại

Danh sách lấy từ workspace khi cập nhật tài liệu; chỉ liệt kê source JS/JSX/CSS và test. Thư mục giữ chỗ không được tính là chức năng hoàn thành.

### Source frontend

```text
src/App.jsx
src/api/auth/addressApi.js
src/api/auth/adminUserApi.js
src/api/auth/authApi.js
src/api/auth/authSession.js
src/api/auth/testApi.js
src/api/axiosClient.js
src/api/category/adminCategoryApi.js
src/api/category/categoryApi.js
src/api/inventory/inventoryApi.js
src/api/inventory/reservationApi.js
src/api/media/mediaApi.js
src/api/order/adminOrderApi.js
src/api/order/checkoutApi.js
src/api/order/orderApi.js
src/api/order/reviewApi.js
src/api/order/sellerOrderApi.js
src/api/order/sellerReportApi.js
src/api/product/adminProductApi.js
src/api/product/productApi.js
src/api/product/sellerProductApi.js
src/api/shipping/shipmentApi.js
src/api/shop/adminShopApi.js
src/api/shop/sellerShopApi.js
src/api/shop/shopApi.js
src/api/shop/shopPage.js
src/components/admin/AdminCategoriesTable.jsx
src/components/admin/AdminCategoryDialog.jsx
src/components/admin/AdminCategoryForm.jsx
src/components/admin/AdminLayout.jsx
src/components/admin/AdminShopDialog.jsx
src/components/admin/AdminShopsTable.jsx
src/components/admin/AdminSidebar.jsx
src/components/admin/AdminUserDialog.jsx
src/components/admin/AdminUsersTable.jsx
src/components/admin/adminCategoryModel.js
src/components/admin/adminShopModel.js
src/components/admin/adminUserModel.js
src/components/auth/AccountMenu.jsx
src/components/auth/AuthForm.jsx
src/components/auth/AuthLayout.jsx
src/components/auth/ForgotPasswordForm.jsx
src/components/auth/RefreshSessionOnNavigation.jsx
src/components/auth/authHelpers.js
src/components/auth/useAuth.js
src/components/cart/AddToCart.jsx
src/components/cart/CartItem.jsx
src/components/cart/cartModel.js
src/components/cart/cartRequests.js
src/components/cart/useCart.js
src/components/cart/useCartAvailability.js
src/components/category/CategoryCard.jsx
src/components/category/CategoryList.jsx
src/components/category/CategorySearchLink.jsx
src/components/checkout/CheckoutAddressFields.jsx
src/components/checkout/CheckoutDialog.jsx
src/components/checkout/CheckoutOrderSummary.jsx
src/components/checkout/ScrollToTop.jsx
src/components/checkout/checkoutModel.js
src/components/checkout/useCheckout.js
src/components/common/ConfirmDialog.jsx
src/components/common/Footer.jsx
src/components/common/Header.jsx
src/components/common/MainLayout.jsx
src/components/common/Toaster.jsx
src/components/common/catalogPaths.js
src/components/common/toast.js
src/components/order/OrderCard.jsx
src/components/order/OrderDetailDialog.jsx
src/components/order/OrderReviewForm.jsx
src/components/order/OrderReviews.jsx
src/components/order/ReviewMediaFields.jsx
src/components/order/orderHelpers.js
src/components/order/reviewMediaModel.js
src/components/product/ProductCard.jsx
src/components/product/ProductGallery.jsx
src/components/product/ProductInfo.jsx
src/components/product/ProductReviewCard.jsx
src/components/product/ProductShop.jsx
src/components/product/ReviewItem.jsx
src/components/product/VariantStock.jsx
src/components/product/productHelpers.js
src/components/profile/AddressForm.jsx
src/components/profile/ChangePasswordForm.jsx
src/components/profile/ProfileAddresses.jsx
src/components/profile/ProfileInfo.jsx
src/components/search/HeaderSearch.jsx
src/components/search/SearchFilters.jsx
src/components/search/SearchResults.jsx
src/components/search/searchModel.js
src/components/seller/SellerCreateProductDialog.jsx
src/components/seller/SellerInitialVariants.jsx
src/components/seller/SellerLayout.jsx
src/components/seller/SellerOrderDialog.jsx
src/components/seller/SellerOrdersTable.jsx
src/components/seller/SellerProductDialog.jsx
src/components/seller/SellerProductEditForm.jsx
src/components/seller/SellerProductMediaField.jsx
src/components/seller/SellerProductsTable.jsx
src/components/seller/SellerReportChart.jsx
src/components/seller/SellerReportResults.jsx
src/components/seller/SellerShopForm.jsx
src/components/seller/SellerShopImageField.jsx
src/components/seller/SellerSidebar.jsx
src/components/seller/SellerVariantEditForm.jsx
src/components/seller/SellerVariantStock.jsx
src/components/seller/productCreationModel.js
src/components/seller/sellerInventoryModel.js
src/components/seller/sellerOrderHelpers.js
src/components/seller/sellerProductHelpers.js
src/components/seller/sellerReportHelpers.js
src/components/shop/ShopCard.jsx
src/components/shop/ShopHeader.jsx
src/components/shop/ShopProducts.jsx
src/index.css
src/main.jsx
src/pages/admin/AdminCategoriesPage.jsx
src/pages/admin/AdminHomePage.jsx
src/pages/admin/AdminSectionPage.jsx
src/pages/admin/AdminShopsPage.jsx
src/pages/admin/AdminUsersPage.jsx
src/pages/auth/LoginPage.jsx
src/pages/auth/RegisterPage.jsx
src/pages/cart/CartPage.jsx
src/pages/home/HomePage.jsx
src/pages/order/OrdersPage.jsx
src/pages/product/ProductDetailPage.jsx
src/pages/product/ProductListPage.jsx
src/pages/profile/ProfilePage.jsx
src/pages/search/SearchPage.jsx
src/pages/seller/SellerOrdersPage.jsx
src/pages/seller/SellerProductsPage.jsx
src/pages/seller/SellerReportsPage.jsx
src/pages/seller/SellerSectionPage.jsx
src/pages/seller/SellerShopPage.jsx
src/pages/shop/ShopListPage.jsx
src/pages/shop/ShopPage.jsx
```

### Test có trong workspace

```text
tests/admin-shell.test.js
tests/admin-users.test.js
tests/auth.test.js
tests/buyer-seller-ui.test.js
tests/cart-ui.test.js
tests/cart.test.js
tests/catalog-urls.test.js
tests/checkout-address.test.js
tests/checkout.test.js
tests/product-creation.test.js
tests/product.test.js
tests/reviews.test.js
tests/toast.test.js
```

## 11. Các thay đổi đã hợp nhất vào tiến độ

- Checkout: sửa tải địa chỉ, chỉ POST sau xác nhận, khôi phục yêu cầu cũ bằng thao tác riêng, Toast/cleanup/điều hướng /orders khi COMPLETED.
- ScrollToTop: bật khi pathname thay đổi để không giữ vị trí cuộn từ homepage sang cart.
- Hồ sơ: /profile, CRUD địa chỉ và đổi mật khẩu.
- Đơn hàng: /orders, dialog chi tiết, xác nhận hủy và cập nhật trạng thái.
- Shop: /shops/:id, banner/logo tròn/thông tin/sản phẩm; nối từ ProductShop.
- Bỏ các ghi chú cũ mâu thuẫn như chưa có Orders/Profile/Shop, nút Thanh toán ở tiêu đề giỏ và kết quả 35 test là mới nhất. Hiện nút Thanh toán nằm trong khung tóm tắt giỏ; kết quả kiểm tra lịch sử gần nhất là 36 test ở bản checkout, chưa bao phủ các phần mới.


### Bổ sung tạo đánh giá sao/bình luận — 13/11/2026

- Nối POST /api/reviews trong reviewApi.js; thêm OrderReviews và OrderReviewForm vào OrderDetailDialog.
- Chỉ đơn DELIVERED; một product một form bất kể số variant. Khóa gửi trùng, chặn đóng/cập nhật dialog khi gửi; Toast thành công/lỗi và xử lý duplicate 409.
- Ảnh/video chưa triển khai trong bước này, payload mediaUrls là mảng rỗng. Backend unique constraint tiếp tục quyết định tính duy nhất.
- Chỉ sửa code và đọc đối chiếu controller/use case/DTO, không chạy test/build/lint hoặc gửi review thực tế.


### Bổ sung ảnh/video cho đánh giá — 13/11/2026

- Hoàn thành code chọn/xem trước/bỏ tệp, giới hạn 6 tệp × 10 MB, định dạng JPG/PNG/WebP/MP4.
- Upload trước khi POST review; giữ URL đã upload khi thử lại trong cùng form, trạng thái tải từng tệp, Toast lỗi và abort khi unmount. Không coi lỗi 409 từ Media là trùng review.
- Media lưu dưới media/reviews/{orderId}/images hoặc videos bằng tên ngẫu nhiên; order_service chỉ nhận mediaUrls. Chưa có API xóa tệp nháp nên không giả lập xóa vật lý khi bỏ tệp khỏi form.
- Chỉ sửa code, không chạy test/build/lint và chưa upload file thật.


### Khung quản lý Seller — 13/11/2026

- AccountMenu chỉ hiện Quản lý cửa hàng cho role SELLER (đã sửa so sánh role viết thường).
- /seller chuyển /seller/shop; SellerLayout nằm dưới MainLayout nên giữ Header/Footer tổng. Kiểm tra loading/lỗi phiên/chưa đăng nhập/sai role trước khi hiển thị nội dung; login từ Seller quay về /seller.
- SellerSidebar có 4 NavLink dọc: Thông tin cửa hàng, Quản lý sản phẩm, Quản lý đơn hàng, Thống kê. Desktop menu bên trái, mobile menu dọc nằm trên nội dung. Có trạng thái active và focus bàn phím.
- 4 trang hiện dùng SellerSectionPage làm khung chờ, không gọi API nghiệp vụ và chưa triển khai tính năng quản lý.
- File chính: components/seller/SellerLayout.jsx, SellerSidebar.jsx, pages/seller/SellerSectionPage.jsx; cập nhật App.jsx, AccountMenu.jsx, LoginPage.jsx.
- Chỉ sửa code và tài liệu, không chạy test/build/lint theo yêu cầu trước đó.


### Thông tin cửa hàng Seller — 13/11/2026

- Thay trang chờ /seller/shop bằng SellerShopPage. GET my-shop, kiểm tra sellerId, có tải/lỗi/thử lại. Chỉ lỗi shop không tồn tại theo thông báo backend mới hiển thị form tạo; lỗi route 404 không tự coi là chưa có shop.
- Tái sử dụng ShopHeader để xem banner/logo tròn/tên/mô tả/địa chỉ/trạng thái/thống kê; có link mở shop công khai. bankDetails chỉ nằm trong form quản lý.
- SellerShopForm tạo/cập nhật tên, slug đúng quy tắc domain, địa chỉ, mô tả (1000), bankDetails (500). Chưa có shop thì tạo thông tin trước; sau khi có mã shop mới thêm logo/banner.
- SellerShopImageField nhận file JPG/PNG/WebP tối đa 10 MB, xem trước và bỏ lựa chọn mới để giữ ảnh cũ. Upload lúc lưu qua Media domain=shops, entityId=shop.id, kiểm tra URL tối đa 255 ký tự trước PUT. Giữ URL upload thành công để retry trong cùng form; blob URL được thu hồi.
- Chặn gửi trùng/tải lại trong khi lưu, hủy request khi unmount, Toast kết quả. Create lỗi chưa rõ kết quả yêu cầu đọc lại my-shop trước khi tạo lại. Chưa thêm đóng/mở shop hoặc xóa ảnh vật lý.
- File mới: pages/seller/SellerShopPage.jsx; components/seller/SellerShopForm.jsx và SellerShopImageField.jsx. 3 mục Sản phẩm/Đơn hàng/Thống kê vẫn là khung chờ.
- Chỉ sửa code/tài liệu; không chạy test/build/lint, chưa upload hoặc ghi shop thật.


### Quản lý sản phẩm Seller — 13/11/2026

- /seller/products đã thay khung chờ bằng SellerProductsPage. Tải my-shop rồi danh sách Seller theo shopId, kiểm tra ownership response. Chưa có shop thì dẫn về tạo shop; có loading/error/thử lại và danh sách rỗng.
- Ô search lọc cục bộ tên/ID/slug/SKU, không phân biệt dấu/chữ hoa; nút tìm kiếm/bỏ lọc. API hiện trả danh sách chưa phân trang.
- SellerProductsTable dạng bảng cuộn ngang: ảnh, tên/ID/slug, minPrice, soldCount, số variant chưa xóa, trạng thái bán/duyệt và nút Chi tiết. soldCount là đã bán, không phải tồn kho.
- SellerProductDialog native modal đọc chi tiết Seller mới, ảnh/video, mô tả, trạng thái duyệt và danh sách variant gồm ảnh/tên/SKU/ID/giá/đã bán/trạng thái.
- Sửa product: tên/slug/danh mục/mô tả, quản lý ảnh/video; khi không thay đổi media thì bảo toàn mediaJson; variants được sửa riêng, backend ProductInput update không cập nhật variants. Category tải lỗi vẫn giữ được category hiện tại. Chỉnh product đưa approvalStatus về PENDING theo backend.
- Sửa variant: SKU/giá/active, thuộc tính bằng cặp tên-giá trị (không bắt người dùng nhập JSON), ảnh qua Media domain=products/entityId=productId/variantId. Giá trị nguyên thủy không đổi được bảo toàn kiểu; cấu trúc thuộc tính phức tạp giữ nguyên. JPG/PNG/WebP tối đa 10 MB.
- Lưu product/variant và xóa đều cần xác nhận trong cùng native dialog, không portal ConfirmDialog phía dưới browser top layer. Nội dung form được giữ khi quay lại/hủy xác nhận. Chỉ gửi mutation sau xác nhận, khóa gửi trùng/đóng khi đang xử lý; Toast và tải lại danh sách sau kết quả. Xóa theo soft-delete của backend, không tự xóa ảnh vật lý.
- Chưa triển khai tạo product/variant hoặc trạng thái sản phẩm bằng nút riêng. Đã bổ sung xem/điều chỉnh tồn kho variant trong dialog. Đã bổ sung thay đổi media sản phẩm sau bước quản lý ban đầu. Orders/Reports Seller vẫn chờ.
- File chính: SellerProductsPage, SellerProductsTable, SellerProductDialog, SellerProductEditForm, SellerVariantEditForm và sellerProductHelpers.
- Chỉ sửa code/tài liệu, đối chiếu controller/DTO/use case; không chạy test/build/lint hoặc ghi dữ liệu thật.


### Chỉnh sửa ảnh/video sản phẩm — 13/11/2026

- SellerProductEditForm thêm SellerProductMediaField: xem media hiện tại, chọn nhiều JPG/PNG/WebP/MP4, tối đa 10 mục trong danh sách, 10 MB/tệp mới. Có xem trước ảnh/video, đổi thứ tự, bỏ media và hủy chỉnh sửa.
- Sau xác nhận lưu trong SellerProductDialog, upload tuần tự tệp mới qua Media domain=products/entityId=productId, giữ URL tệp cũ và thứ tự đã chọn, serialize mảng URL vào mediaJson rồi PUT sản phẩm. Nếu không chỉnh media thì giữ nguyên mediaJson gốc, tránh thay dữ liệu chỉ vì mở form.
- Upload lỗi không PUT; URL upload thành công được cache trong dialog để retry không upload lại. Khóa chọn/đóng/gửi trùng trong khi xử lý; có progress và Toast. Blob URL xem trước được thu hồi khi bỏ tệp/unmount.
- Bỏ media chỉ gỡ URL khỏi sản phẩm khi lưu, không xóa file vật lý vì API chưa hỗ trợ xóa upload nháp. Hủy form không PUT hoặc tự upload.
- Chỉ sửa code/tài liệu, không chạy test/build/lint theo yêu cầu trước đó.


### Tồn kho biến thể trong dialog Seller — 13/11/2026

- SellerProductDialog đọc GET /api/inventories/seller?shopId={shopId} một lần cho các variant của sản phẩm, không gọi từng variant. Kiểm tra shopId/productId/variantId và số lượng an toàn, không âm; available = onHand - reserved.
- SellerVariantStock hiển thị tồn thực tế, đang giữ chỗ và có thể bán cho từng biến thể. Có trạng thái tải/lỗi, nút tải lại tồn kho; thiếu bản ghi kho được thông báo riêng, không giả định số lượng 0 hoặc gọi API Admin để khởi tạo.
- Điều chỉnh nhập tổng tồn thực tế mới (không phải delta), số nguyên từ reservedQuantity đến Number.MAX_SAFE_INTEGER. Không cho sửa trực tiếp reserved/available.
- Lưu mở bước xác nhận trong native dialog hiện có; chỉ xác nhận mới PATCH {newQuantity}. Backend kiểm tra quyền/giữ chỗ lần cuối. Khóa đóng/gửi trùng trong khi xử lý.
- Thành công dùng InventoryResponse cập nhật cả 3 số liệu và Toast. Lỗi tải lại tồn kho, đóng bước xác nhận để người dùng kiểm tra số liệu mới, không tự gửi lại PATCH.
- File mới: components/seller/SellerVariantStock.jsx và sellerInventoryModel.js; cập nhật SellerProductDialog.jsx. Chỉ sửa code/tài liệu, không chạy test/build/lint hoặc sửa tồn kho thực tế.


### Quản lý đơn hàng Seller — 13/11/2026

- Thay trang chờ /seller/orders bằng SellerOrdersPage, vẫn trong MainLayout/SellerLayout và guard SELLER. Đọc my-shop, kiểm tra sellerId; thiếu shop dẫn về /seller/shop, có trạng thái tải/lỗi/thử lại.
- Dùng GET /api/orders/seller/shops/{shopId} với page/size/status/from/to; DTO items[].order được unwrap và kiểm tra shopId/tổng tiền. Lọc theo ngày đặt, bao gồm ngày kết thúc theo múi giờ backend Asia/Ho_Chi_Minh. Chọn 10/20/50 đơn mỗi trang, reset trang khi áp dụng lọc, tự lùi trang nếu tổng trang giảm sau mutation.
- Bảng SellerOrdersTable: mã đơn/ngày, shippingAddress, mặt hàng/số lượng, tổng tiền, phương thức và trạng thái thanh toán/đơn; nút Chi tiết.
- Tìm kiếm cục bộ trong trang hiện tại theo mã đơn/người nhận/điện thoại/tên sản phẩm/variant, có nhãn rõ; không giả lập tìm kiếm toàn bộ dữ liệu khi API chưa hỗ trợ từ khóa.
- SellerOrderDialog đọc GET chi tiết theo quyền Seller; hiển thị order/checkout/buyer IDs, người nhận, thời gian, trạng thái, ảnh/sản phẩm/variant/giá/số lượng, subtotal/shippingFee/totalAmount.
- Giao cho vận chuyển chỉ ở CONFIRMED, POST /ship; hủy chỉ COD + CONFIRMED + chưa PAID, POST /cancel. Cả hai có xác nhận trong native dialog, khóa gửi trùng/đóng trong lúc xử lý; kiểm tra response và Toast thành công sau API.
- Sau mutation tải lại bảng; lỗi/timeout đọc lại chi tiết để đối chiếu trạng thái, không tự POST lại. Có nút cập nhật trạng thái thủ công. Không thêm nút xác nhận DELIVERED hoặc API vận đơn Admin cho Seller.
- File mới: pages/seller/SellerOrdersPage.jsx; components/seller/SellerOrdersTable.jsx, SellerOrderDialog.jsx, sellerOrderHelpers.js. Reports là mục Seller duy nhất còn dùng khung chờ.
- Chỉ sửa code/tài liệu, đối chiếu controller/use case/DTO; không chạy test/build/lint hoặc giao/hủy đơn thực tế.


### Thống kê Seller — 13/11/2026

- /seller/reports thay trang chờ bằng SellerReportsPage, giữ guard SELLER và layout chung. Tải my-shop, kiểm tra sellerId; thiếu shop dẫn về tạo, lỗi/tải/thử lại riêng.
- Thêm sellerReportApi.js gọi demand và sales đúng contract. Hai chế độ độc lập; chọn khoảng from/to, groupBy, nút Xem báo cáo; mặc định tháng này đến hôm nay theo giờ Việt Nam. Preset hôm nay/7 ngày/tháng/năm chỉ điền form, cần áp dụng mới tải dữ liệu. Khoảng tối đa 3661 ngày và năm 1–9998 theo backend.
- Demand nhóm DAY/WEEK/MONTH: tổng đơn/không hủy/hủy, tổng lượng/không hủy/hủy và tỷ lệ hủy theo số đơn. Ghi rõ dựa createdAt và trạng thái hiện tại, không phải số khách duy nhất hay lượt hủy xảy ra trong kỳ.
- Sales nhóm DAY/WEEK/MONTH/YEAR: chỉ DELIVERED theo updatedAt (tạm thời), deliveredOrders/soldQuantity/revenue/subtotal, phí vận chuyển và tổng tiền đơn. Tiền hàng không gồm vận chuyển và không coi là lợi nhuận.
- SellerReportResults có thẻ tổng hợp, SellerReportChart biểu đồ cột cuộn ngang và bảng đầy đủ theo kỳ. Tuần bắt đầu thứ Hai; nhãn kỳ được cắt theo khoảng thực chọn, không gợi ý thống kê cả kỳ ngoài from/to.
- Bảng bestSellingVariants hiển thị productName/variantName/IDs/soldQuantity/revenue; 20 nhóm mỗi trang, tổng báo cáo luôn từ summary server, không tính lại từ trang variant đang xem. Reset trang khi áp dụng bộ lọc; lùi trang nếu tổng trang giảm.
- Kiểm tra response khớp shop/range/group/timezone, số lượng/tiền hợp lệ và pagination. Hủy request khi chuyển chế độ/đổi filter/unmount, không giữ số liệu cũ dưới filter mới. Loading/error/empty/thử lại/tải lại thủ công.
- File mới: pages/seller/SellerReportsPage.jsx; components/seller/SellerReportResults.jsx, SellerReportChart.jsx, sellerReportHelpers.js; api/order/sellerReportApi.js. App không còn route SellerSectionPage chờ cho 4 mục Seller; file placeholder cũ vẫn giữ, không tự xóa.
- Chỉ sửa code/tài liệu và đối chiếu controller/DTO/use case/SQL, không chạy test/build/lint hoặc gọi API thật. Chưa có export CSV/Excel hoặc so sánh hai kỳ.


### Làm mới token khi chuyển URL — 13/11/2026

- RefreshSessionOnNavigation nằm trong BrowserRouter, theo dõi location key/pathname/search/hash và thời điểm xác minh phiên hoàn tất. Không gọi refresh khi render hoặc liên tục theo timer.
- authSession.js đọc exp JWT base64url để lên lịch (không thay việc xác thực backend). Ngưỡng ACCESS_TOKEN_REFRESH_WINDOW_MS=60000; token còn nhiều thời gian không gọi API, exp không đọc được không tự suy đoán. Token hết hạn lúc chuyển route cũng thử refresh.
- refreshSessionTokens chia sẻ một promise cho cùng phiên, dùng POST /auth/refresh hiện có, kiểm tra đủ token và userId nếu response có; lưu accessToken/refreshToken mới cùng nhau. Không hủy refresh đang xoay token vì route đổi/StrictMode cleanup, không ghi đè nếu phiên đã logout/login khác.
- useAuth giữ hồ sơ đã xác minh khi token xoay thành công, cập nhật session/verification cùng nhau để không đổi owner giỏ hoặc remount trang bảo vệ. Restore 401 cũng dùng refresh chung và đồng bộ lại state với token mới.
- Axios request cần Auth tới gateway chờ refresh đang chạy rồi mới lấy Bearer mới; request skipAuth (đặc biệt /auth/refresh) không chờ để tránh deadlock. Header Authorization tường minh được giữ nguyên. Request chờ bị chặn nếu đã đổi user/logout.
- Refresh 400/401/403: xóa phiên và Toast yêu cầu đăng nhập lại. Lỗi mạng/server: không xóa token còn hiệu lực, Toast lỗi và cooldown 30 giây cho cùng phiên; token đã hết hạn có trạng thái lỗi xác minh để retry. Không tự phát Toast thành công mỗi lần chuyển trang.
- Chưa thêm response interceptor tự replay request 401 hoặc timer nền. Lần này chỉ sửa code/tài liệu, không chạy test/build/lint hoặc gọi refresh thật.


### Trang tìm kiếm sản phẩm — 13/11/2026

- Thêm /search công khai trong MainLayout. Header tìm kiếm dẫn route mới, không giữ query ngoài nghiệp vụ tìm kiếm. Chọn danh mục homepage và link cùng danh mục detail mở /search; ShopPage có link tìm trong shop.
- SearchPage/SearchFilters/SearchResults/searchModel chia trách nhiệm theo cấu trúc hiện tại; tái sử dụng ProductCard và API search. URL là nguồn dữ liệu cho q/categoryId/shopId/includeChildren/minPrice/maxPrice/sort/page/size; reload/back/forward giữ bộ lọc, đổi query cuộn lên đầu.
- Search endpoint được dùng cả khi q trống để khám phá catalog. Bộ lọc danh mục ACTIVE, tùy chọn gồm con, giá nguyên VND theo minPrice sản phẩm, scope shop từ link công khai. Bỏ từ khóa/danh mục/khoảng giá/shop riêng; lỗi tải danh mục không chặn tìm kiếm.
- Sort RELEVANCE/NEWEST/PRICE_ASC/PRICE_DESC; mặc định liên quan khi có từ khóa, mới nhất khi q trống. Mỗi trang chọn 12/24/48; page bắt đầu 1, tối đa 200 theo backend. Hiển thị tổng kết quả/range, nút số trang/trước/sau và cảnh báo cần thu hẹp bộ lọc khi vượt 200 trang.
- Validate q tối đa 120, UUID bộ lọc, khoảng giá không âm/min <= max/số nguyên an toàn, sort, size và page URL. URL sai có thông báo và link bắt đầu mới, không gửi API với tham số sai.
- Response kiểm tra page/size/tổng/items và shopId nếu có scope; AbortController chống kết quả cũ ghi đè khi chuyển query. Có loading/error/thử lại/empty và về trang đầu khi trang hiện tại không có kết quả.
- Không thêm thư viện; chưa có autocomplete, lịch sử tìm kiếm hoặc export. Chỉ sửa code/tài liệu, không chạy test/build/lint hoặc gọi API thật.


### Áp dụng URL sản phẩm/danh mục — 13/11/2026

- Danh mục dùng /search/danh-muc/:slug. SearchPage đợi catalog ACTIVE để giải slug thành categoryId; API giữ nguyên UUID. URL /search?categoryId=... và query lọc homepage cũ tự replace sang route chuẩn, giữ q/shop/giá/sort/page/size; includeChildren=true mặc định ẩn, false được giữ.
- categorySearchPath dùng chung khi chọn danh mục, đổi bộ lọc/sort/trang, Header và liên kết cùng danh mục. Slug trùng/không tồn tại/inactive hoặc mâu thuẫn với categoryId có lỗi rõ, không tự chọn sản phẩm/danh mục khác.
- Sản phẩm dùng /products/:slug/:UUID vì slug backend chỉ duy nhất theo shop. UUID vẫn định danh ổn định, không cần scan catalog hay đổi API. ProductDetail đọc đúng UUID rồi canonical replace nếu phần slug cũ/sai hoặc URL cũ chỉ UUID; query/hash giữ nguyên.
- productPath áp dụng ProductCard trên homepage/search/shop, CartItem, link xem đánh giá từ Orders; ưu tiên productId trong order item để không dùng nhầm item.id. SKU giữ vai trò tìm kiếm, không dùng làm định danh sản phẩm.
- Không đổi URL quản trị Seller hoặc request API sang slug. Không có lịch sử alias khi đổi slug danh mục: URL UUID cũ vẫn resolve theo slug mới, URL slug cũ không còn trong catalog sẽ báo không tìm thấy. File preview độc lập vẫn được giữ để đối chiếu.
- Bổ sung catalog-urls.test.js: 7 test, toàn bộ suite 43/43 đạt; lint đạt; build thành công, cảnh báo bundle >500 KB. Bỏ hai initializer content=null thừa tại ShopProducts/ShopPage, không đổi hành vi render. Chưa kiểm tra E2E trình duyệt/backend thật.


### Hoàn thiện UI Buyer/Seller — 13/11/2026

- Login có Quên mật khẩu: giữ email đã nhập, mở ForgotPasswordForm trong cùng /login, gọi PUT public /auth/forgotpassword. Pending/khóa gửi trùng/Toast; thành công hiển thị hướng dẫn kiểm tra email, không tự gửi lại. Không tạo luồng token reset khác với backend hiện có.
- HeaderSearch gợi ý toàn catalog sau 300 ms, tối thiểu 2 ký tự, GET search size=5 sort=RELEVANCE; AbortController loại request cũ. Hỗ trợ lên/xuống/Enter/Escape, chuột và focus; gợi ý lỗi không chặn tìm kiếm đầy đủ. Chọn sản phẩm dùng URL chuẩn productPath. Không tự ghi lịch sử tìm kiếm.
- /shops có ShopListPage/ShopCard, GET public /shops phân trang 12 shop/trang (server page 0, URL page 1). Card banner/logo tròn/tên/mô tả/địa chỉ/số sản phẩm/đánh giá/trạng thái; không tự giấu shop đóng và không coi mọi shop đều ACTIVE. Link vào shop chi tiết; Header/Footer có mục Cửa hàng.
- SellerProductsPage thêm Tạo sản phẩm. SellerCreateProductDialog tái sử dụng form tên/category/mô tả/media và danh sách variant bắt buộc; slug tự sinh tên + shopId, xác nhận rồi POST product kèm variants để lấy UUID; upload bằng UUID thật, sau đó PUT media. Thành công mở ngay dialog sản phẩm để xem các variant đã tạo và tiếp tục quản lý. Sản phẩm mới INACTIVE/PENDING theo backend.
- Tạo thành công nhưng media lỗi giữ UUID nháp và URL đã upload, retry PUT trên cùng nháp thay vì POST trùng. Có nút mở nháp; lỗi POST chưa rõ kết quả chặn retry tự động, yêu cầu đóng/tải lại bảng để kiểm tra. Không giả lập rollback/xóa sản phẩm vừa tạo.
- SellerVariantEditForm hỗ trợ tạo mới: SKU/giá/thuộc tính/active/ảnh, xác nhận rồi POST variant. Ảnh có thể upload vào thư mục product trước khi có UUID variant; chỉ gửi URL. Lỗi tạo đọc lại chi tiết trước khi thao tác tiếp; kiểm tra kho sau khi thêm, không gọi API Admin để tự khởi tạo kho.
- SellerProductDialog thêm Bật bán/Ngừng bán qua PATCH status có xác nhận, cập nhật trạng thái từ response và Toast. Bật ACTIVE không bỏ qua APPROVED hoặc shop ACTIVE; giao diện nhắc rõ điều kiện hiển thị công khai.
- Kiểm tra: 46/46 test đạt, lint đạt, build thành công (cảnh báo bundle >500 KB). Chưa kiểm tra trên trình duyệt với API/email/upload thật.


### Tạo product bắt buộc kèm variant, slug mặc định — 13/11/2026

- Form tạo mới có sẵn một variant, không được bỏ variant cuối cùng; có thể thêm nhiều variant trước khi xác nhận. Mỗi variant nhập SKU/giá/active, cặp thuộc tính và ảnh tùy chọn; chặn SKU trùng, giá lỗi và thuộc tính thiếu/trùng tên.
- Slug tạo mới read-only, tự sinh từ tên sản phẩm bỏ dấu/chuẩn hóa + dấu gạch ngang + UUID shop; thay đổi tên cập nhật preview slug. Giới hạn 220 ký tự để nằm trong giới hạn DB. Sửa sản phẩm hiện có vẫn giữ cơ chế slug hiện tại, không tự đổi các slug cũ.
- POST create gửi danh sách VariantInput đầy đủ cùng ProductInput (không còn variants=[] ở yêu cầu tạo). Backend hiện có transaction tạo product và variants cùng nhau; không gọi thêm POST variant để tạo các variant ban đầu.
- Sau response, upload media sản phẩm bằng UUID product, ảnh variant bằng UUID variant thật tra theo SKU; PUT media/ảnh sau upload. Order/inventory logic không đổi. Sản phẩm và variants đã tồn tại nếu upload lỗi, giữ ID/cache để thử tiếp hoặc mở chi tiết, không POST tạo lại.
- Khi đã tạo nháp mà media lỗi, form không quay về sửa danh sách variant ban đầu trong phiên tạo: mở chi tiết để quản lý variant đã lưu, hoặc thử lại đúng yêu cầu media. Tránh khiến thay đổi variant trong bản nháp lệch với DB.
- File mới: SellerInitialVariants.jsx, productCreationModel.js; cập nhật SellerProductEditForm/SellerCreateProductDialog và test. Không thay API/backend hoặc tự khởi tạo tồn kho bằng API Admin.

- Kết quả kiểm tra bước tạo product kèm variant: 49/49 test đạt, lint đạt, build thành công; chưa tạo sản phẩm/upload thật qua backend.


### Khởi tạo khu vực Admin — 13/11/2026

- AccountMenu thêm Quản trị -> /admin chỉ khi auth.user.role === ADMIN, dùng profile đã xác minh từ useAuth.
- AdminLayout nằm dưới MainLayout, giữ Header/Footer và context auth/cart; kiểm tra đang xác minh/đăng xuất, lỗi phiên, guest và sai role trước khi render menu hoặc nội dung bảo vệ.
- Guest vào /admin được dẫn login với returnToAdmin và quay lại /admin khi đăng nhập; tài khoản sai role nhận thông báo không có quyền, không tự nâng quyền hoặc gọi API Admin.
- AdminSidebar dọc gồm Tổng quan, Người dùng, Cửa hàng, Danh mục, Duyệt sản phẩm, Đơn hàng; active theo route và focus bàn phím. Mobile menu nằm trên nội dung, desktop ở bên trái.
- AdminHomePage có các liên kết tới 5 nhóm quản lý; AdminSectionPage hiện là trang chờ. Không bịa số liệu dashboard hoặc coi CRUD/duyệt đã hoàn thiện.
- File mới: components/admin/AdminLayout.jsx, AdminSidebar.jsx; pages/admin/AdminHomePage.jsx, AdminSectionPage.jsx. Cập nhật App, AccountMenu, LoginPage; thêm admin-shell.test.js kiểm tra role/menu/guard.

- Kiểm tra bước Admin: 50/50 test đạt; lint đạt; build thành công, vẫn cảnh báo bundle lớn hơn 500 KB. Chưa kiểm tra trình duyệt/API Admin thực tế; các mục quản trị hiện chỉ là khung chờ.


### Quản trị user — 14/11/2026

- /admin/users đã thay placeholder bằng AdminUsersPage: bảng userId/email/phone/role/status và Sửa/Cấm-Bỏ cấm/Xóa. Search email/phone phía server, page 0, size 10/20/50; loading/error/empty/retry, tự lùi trang khi xóa cuối trang.
- AdminUserDialog đọc user mới trước thao tác. Sửa email/phone, giữ role/password. Mọi mutation có xác nhận, pending/khóa gửi trùng/đóng, AbortController và Toast; cập nhật bảng sau kết quả. Sửa chính mình xác minh lại hồ sơ Auth để Header cập nhật.
- Trạng thái đích được giữ cố định khi retry (không tự toggle ngược nếu lần trước đã thành công). Cấm dùng LOCKED, bỏ cấm ACTIVE; backend và UI chặn tự cấm/tự xóa.
- Backend bổ sung ManageAdminUserUseCase, AdminUpdateUserRequest, AdminUserStatusRequest; PUT contact và PATCH status. Dùng domain updateContact/lock/activate, khóa dòng target qua repository hiện có; không đổi role/password hoặc thêm schema. Các Admin user API kiểm tra admin ACTIVE trong DB, email trùng được từ chối.
- RefreshTokenUseCase đã từ chối user LOCKED, login đã có kiểm tra trước đó. JWT access cũ không bị thu hồi tức thì ở các service khác: hiệu lực tới expiry. DELETE giữ semantics xóa thật user và địa chỉ Auth, không đụng DB orders.
- Bổ sung test Java cho self-ban/self-delete/locked-admin/idempotent ban/email trùng và refresh user bị cấm; chưa chạy build/test backend trong lần này. Frontend thêm 3 test contract/validation/self-protection; 53/53 test đạt, lint đạt và build thành công (cảnh báo bundle >500 KB). Chưa gọi API ghi dữ liệu thật.


### Quản lý cửa hàng Admin — 14/11/2026

- Theo yêu cầu mới: từ bước này chỉ sửa code/tài liệu, không chạy build/test/lint. Kết quả kiểm tra ở mục 6 thuộc bản trước, không xác nhận code Shop Admin mới đã được chạy kiểm tra.
- /admin/shops thay placeholder bằng AdminShopsPage. GET /api/shops/admin page 0, size 10/20/50, sort createdAt,desc. Bảng logo/name/slug/shopId/sellerId/productCount/rating/status và thao tác.
- Tìm theo tên/slug/shopId/sellerId/địa chỉ và lọc status cục bộ trong trang hiện tại, có nhãn rõ. Không gửi keyword/status/sellerId giả vì API list chưa hỗ trợ các bộ lọc này; tổngElements là tổng tất cả shop, không phải tổng khớp bộ lọc cục bộ.
- AdminShopDialog GET /api/shops/admin/{shopId}, hiển thị ShopHeader (banner/logo tròn/thông tin/thống kê), sellerId, slug, thời gian cập nhật và bankDetails riêng cho admin; link mở trang shop.
- Đổi trạng thái ACTIVE/SUSPENDED/CLOSED/BANNED qua PATCH /api/shops/admin/{shopId}/status?shopStatus=...; body rỗng đúng API. Có nút nhanh tạm ngưng/mở lại/cấm trên bảng và chọn đủ 4 trạng thái trong dialog.
- Thay đổi cần xác nhận, khóa đóng/gửi trùng khi pending, kiểm tra response đúng shopId/trạng thái rồi Toast/cập nhật chi tiết và tải lại bảng. Lỗi đọc lại chi tiết, giữ cố định trạng thái đích để retry không đảo ngược thao tác.
- Đóng shop dùng PATCH CLOSED, không dùng DELETE Admin hiện còn kiểm tra owner trong CloseShopUseCase. Không sửa backend trong bước này; không xóa shop/user/orders và không giả lập sửa thông tin shop khi API Admin chưa có PUT.
- File mới: AdminShopsPage, AdminShopsTable, AdminShopDialog, adminShopModel; cập nhật route App và trang tổng quan Admin. Chưa gọi API thật hoặc chạy kiểm tra tự động.


### Sửa lỗi “Danh sách cửa hàng không hợp lệ” — 14/11/2026

- Nguyên nhân xác định từ WebConfig: product_service serialize Page qua VIA_DTO, metadata nằm trong data.page; UI Admin đọc number/size/totalElements/totalPages ở ngoài nên validation thất bại.
- Thêm api/shop/shopPage.js chuẩn hóa metadata nested thành shape flat, dùng trong getAdminShops và getShops. Cả AdminShopsPage và ShopListPage công khai được sửa mà giữ nguyên validation, không sửa backend hay đoán mặc định khi response thiếu dữ liệu.
- Giữ tương thích PageImpl flat cũ. Không chạy build/test/lint theo yêu cầu; chưa gọi backend thực tế.


### Quản lý danh mục Admin — 14/11/2026

- /admin/categories thay placeholder bằng AdminCategoriesPage. Đọc GET /categories trả toàn bộ CreateCategoryResult[], gồm cả INACTIVE. Kiểm tra IDs/trạng thái/thứ tự; sort displayOrder/tên/ID.
- Bảng ảnh/tên/slug/ID/cha/số con trực tiếp/thứ tự/trạng thái/thao tác. Search toàn danh sách theo tên/slug/ID/tên cha; lọc trạng thái/cấp gốc/cha trực tiếp. Phân trang cục bộ 10/20/50 mục, tổng số theo đúng tập đã lọc.
- AdminCategoryDialog đọc GET /categories/{id} mới trước sửa/ẩn/kích hoạt. AdminCategoryForm nhập name/slug/description/parentId/displayOrder, ảnh file JPG/PNG/WebP <=10 MB và tùy chọn gỡ ảnh.
- Slug tạo mới tự gợi ý từ tên, cho sửa/tạo lại; giới hạn 100 ký tự, kiểm tra trùng và nhắc URL slug cũ không được alias khi đổi. Parent options loại chính mình/toàn bộ descendants bằng duyệt có tập visited, backend kiểm tra chu kỳ lần cuối.
- POST/PUT /api/categories/admin và /{id}; body đúng Create/UpdateCategoryRequest. Tạo lấy UUID thật trước upload Media domain=categories/entityId=category.id, rồi PUT imageUrl <=255 ký tự. Upload/lưu ảnh lỗi giữ ID/cache URL cho retry, không POST tạo lại. Lỗi create chưa rõ kết quả yêu cầu kiểm tra danh sách, không tự gửi lại.
- DELETE /api/categories/admin/{id} thực tế chỉ chuyển INACTIVE: UI ghi Ẩn, xác minh bằng GET lại; không xóa vĩnh viễn/cascade con/xóa sản phẩm. Kích hoạt lại PATCH /{id}/status {status: ACTIVE}. Trạng thái đích cố định khi retry.
- Mỗi mutation có xác nhận trong native dialog, khóa gửi trùng/đóng khi xử lý, Toast và tải lại bảng. Có lỗi/tải/rỗng/thử lại; giữ danh sách cha trong lúc refresh để không mất bản nháp của dialog.
- File mới: AdminCategoriesPage, AdminCategoriesTable, AdminCategoryDialog, AdminCategoryForm, adminCategoryModel; cập nhật App và tổng quan Admin. Không sửa backend, không chạy build/test/lint và chưa gọi API ghi dữ liệu thật theo yêu cầu.


### Duyệt sản phẩm Admin — 14/11/2026

- `/admin/products`: hàng đợi PENDING, bảng tên/ảnh/ID/giá/variant/tên cửa hàng; tìm kiếm tên, slug, SKU, ID và tên shop; phân trang frontend 10/20/50.
- Dialog luôn GET lại chi tiết; xem mô tả, ảnh/video, danh mục, giá, trạng thái, thời gian, tất cả variant (kể cả ngừng bán/đã xóa), SKU/ảnh/giá/thuộc tính và lịch sử xét duyệt.
- API product không có shopName: lấy từ GET admin shop theo shopId duy nhất, tối đa 4 request đồng thời trong hàng đợi. Lỗi tên shop không làm mất danh sách, vẫn hiển thị shopId; dialog có thử tải lại shop.
- Duyệt/từ chối có bước xác nhận; từ chối bắt buộc lý do, tối đa 2000 ký tự tại UI. Toast kết quả, chống nhấn lặp, tải lại hàng đợi; dialog giữ kết quả và lịch sử. Chỉ cho quyết định khi PENDING và chưa xóa.
- Nếu mutation lỗi, đọc lại chi tiết/hàng đợi, không tự gửi lại quyết định. Điều kiện hiển thị công khai vẫn phụ thuộc trạng thái bán và shop.
- File mới: AdminProductsPage, AdminPendingProductsTable, AdminProductModerationDialog, AdminModerationDetails, adminProductModel; nối App và AdminHomePage.
- Chỉ sửa code/tài liệu; không chạy build, test hay lint theo yêu cầu. Chưa kiểm tra tích hợp trình duyệt/backend.


### Quản lý đơn hàng Admin — 14/11/2026

- `/admin/orders` đã nối AdminOrdersPage thay trang chờ. Nhập UUID và chọn tra cứu theo mã đơn, checkout, shop hoặc buyer; kiểm tra dữ liệu trả về đúng phạm vi đã yêu cầu.
- Bảng gồm mã đơn/ngày tạo/ngày cập nhật, buyerId/shopId, thông tin người nhận, tổng tiền, phương thức/trạng thái thanh toán, trạng thái đơn và nút chi tiết.
- Lọc từ khóa (mã/người nhận/tên sản phẩm/variant), trạng thái đơn/thanh toán, khoảng ngày đặt theo Việt Nam, sắp xếp mới/cũ, phân trang frontend 10/20/50. Tất cả áp dụng trên kết quả tra cứu, chưa phải danh sách toàn hệ thống.
- Dialog GET lại chi tiết, lấy tên cửa hàng bằng adminShopApi; hiển thị địa chỉ nhận hàng nguyên bản, sản phẩm/variant/ảnh/giá/số lượng/thành tiền và tiền hàng/phí ship/tổng tiền. Lỗi tên shop có retry riêng.
- Mục **Lịch sử trạng thái: Chưa triển khai**. Chưa có dữ liệu vận đơn hoặc lịch sử để hiển thị; không suy diễn từ updatedAt.
- Hủy đơn COD CONFIRMED chưa PAID, có xác nhận, khóa nhấn lặp và toast. Backend quyết định điều kiện cuối cùng. Sau thao tác tải lại kết quả; khi lỗi đọc lại trạng thái, không tự gửi lại POST.
- API cancel không nhận/lưu lý do; UI chưa thêm ô lý do. Chưa thêm chuyển trạng thái tùy ý hoặc gọi API ship từ Admin.
- File mới: pages/admin/AdminOrdersPage.jsx, components/admin/AdminOrderDialog.jsx, components/admin/adminOrderModel.js. Nối App và cập nhật AdminHomePage.
- Chỉ sửa code/tài liệu; không chạy build/test/lint theo yêu cầu. Chưa nghiệm thu tích hợp backend/trình duyệt.


### Thống kê đối soát doanh thu Admin — 14/11/2026

- Thêm `/admin/reports`, menu Đối soát doanh thu và thẻ ở trang tổng quan Admin.
- Khoảng ngày mặc định đầu tháng đến hôm nay; áp dụng theo updatedAt của đơn DELIVERED, múi giờ Việt Nam, gồm ngày kết thúc. UI cho chọn khoảng ngày hợp lệ.
- Phân trang danh sách shop (10 shop/trang), tải đơn theo shop với tối đa 4 request đồng thời; kiểm tra dữ liệu đúng shop, không trùng đơn, số tiền nguyên an toàn. Tìm tên/ID shop chỉ trong trang hiện tại.
- Bảng từng shop: số đơn đã giao, tiền hàng subtotal, phí vận chuyển, tổng giá trị đơn, giá trị DELIVERED có PAID và DELIVERED chưa PAID. Các tổng thẻ tính shop tải thành công trên trang hiện tại, độc lập bộ tìm kiếm, không phải tổng toàn hệ thống. Có chỉ báo tiến độ/lỗi/chưa đầy đủ.
- Xem các đơn cấu thành số liệu (50 đơn/trang), thời điểm tải từng shop và mở AdminOrderDialog để đối chiếu chi tiết. Lịch sử trạng thái trong dialog vẫn ghi Chưa triển khai.
- Đây là đối chiếu tham khảo từ OrderResult: PAID không chứng minh tiền ngân hàng; chưa có phí nền tảng/hoàn tiền/sổ công nợ/chốt hoặc chuyển tiền. Không giả lập số tiền phải trả shop hay nút xác nhận đã chuyển tiền.
- Backend chưa có báo cáo đối soát riêng; frontend dùng API admin shop và orders hiện có, chưa sửa backend. Truy xuất toàn bộ đơn của từng shop có thể lớn; bước tiếp theo là API tổng hợp/phân trang chuyên biệt và sổ đối soát nếu cần nghiệp vụ thanh toán thực tế.
- File mới: pages/admin/AdminRevenuePage.jsx, components/admin/adminRevenueModel.js; cập nhật App/AdminSidebar/AdminHomePage.
- Chỉ sửa code/tài liệu, không chạy build/test/lint. Chưa nghiệm thu tích hợp với backend/trình duyệt.


### Dialog đơn hàng theo shop — 14/11/2026

- Trang `/admin/reports` thêm nút **Xem đơn hàng của shop**, sử dụng được cả khi tổng hợp doanh thu shop lỗi.
- Dialog độc lập tải GET `/api/orders/admin?shopId={UUID}`; kiểm tra đúng shop/không trùng mã. Có lọc từ ngày–đến ngày, theo createdAt hoặc updatedAt, trạng thái đơn và phân trang frontend 20 đơn/trang.
- Mặc định dùng khoảng ngày báo cáo, DELIVERED và updatedAt để đối chiếu; có thể chọn tất cả trạng thái và ngày đặt để xem các đơn khác. Khoảng ngày inclusive theo Asia/Ho_Chi_Minh, chỉ áp dụng khi bấm bộ lọc, không đổi kỳ báo cáo bên ngoài.
- Bảng có mã đơn/người nhận/ngày đặt/cập nhật/trạng thái/thanh toán/tổng tiền. Mở dialog chi tiết từng đơn và quay lại vẫn giữ bộ lọc. Sau thao tác hủy cập nhật lại danh sách và báo cáo. Đơn thiếu ngày hợp lệ được thông báo thay vì âm thầm bỏ qua.
- Lịch sử trạng thái trong chi tiết đơn vẫn ghi Chưa triển khai. Không thêm API backend; danh sách shop-order hiện chưa có lọc ngày hoặc phân trang server.
- Thêm components/admin/AdminShopOrdersDialog.jsx, nối AdminRevenuePage. Không chạy build/test/lint theo yêu cầu.


### API Admin server pagination / doanh thu — 14/11/2026

Cập nhật này thay thế các giới hạn “chỉ tra cứu đơn” và “tổng doanh thu theo shop trên trang hiện tại” trong ghi chép lịch sử phía trên.

- Thêm backend AdminOrderQueryPort/UseCase, AdminOrderJdbcAdapter, AdminOrderQueryController/ExceptionHandler. GET /api/orders/admin/page lọc UUID/trạng thái/thanh toán/khoảng ngày/keyword và phân trang/sort server. Mặc định không có filter trả toàn hệ thống. Batch đọc item đúng các order trong trang; sort ngày + UUID ổn định. READ_ONLY REPEATABLE_READ cho count/page.
- GET /api/orders/admin/reports/revenue tổng hợp SQL DELIVERED theo updatedAt, summary toàn phạm vi lọc, không phụ thuộc trang; bảng shop có đơn khớp kỳ được phân trang/sort doanh thu giảm dần. Hỗ trợ shopId tùy chọn. Không gọi service khác từ backend.
- Giữ API cũ, dùng guard ADMIN hiện có. Thêm V9 migration index, không sửa migration/seed đã có. Cập nhật backend docs/API-FRONTEND.md.
- Frontend AdminOrdersPage giữ giao diện hiện tại và chuyển filter/page sang API mới, tải tự động toàn hệ thống, debounce keyword 300ms. AdminShopOrdersDialog cũng phân trang/lọc server. AdminRevenuePage không tải toàn bộ đơn shop để tổng hợp nữa; chỉ bổ sung tên shop bằng GET admin shop.
- Báo cáo có tổng tiền hàng, phí ship, tổng đơn, PAID/chưa PAID và số đơn. Không đưa shop không phát sinh vào bảng; không suy diễn công nợ, hoa hồng, hoàn tiền hay chốt/chuyển tiền. updatedAt còn có thể dịch chuyển kỳ. Chưa có history trạng thái.
- Chỉ sửa code/tài liệu; theo yêu cầu không chạy build/test/lint, không gọi API hoặc chạy migration trên DB. Cần khởi động lại backend để Flyway áp dụng V9 khi nghiệm thu.


### Tối ưu tải trang theo route — 14/11/2026

- App chuyển các page Auth/Buyer/Seller/Admin sang React.lazy + dynamic import, khai báo ở module scope. Trang chủ giữ import trực tiếp để không tạo thêm vòng tải JS cho landing page. Layout, guard role và session/cart vẫn được giữ trong shell.
- Thêm components/common/RoutePage.jsx: Suspense/skeleton đặt quanh từng page, giữ Header/Footer và sidebar bên ngoài. Error boundary có tải lại thủ công hoặc về trang chủ khi tải chunk/render lỗi; không tự reload lặp. Boundary reset theo pathname, không theo query để tránh làm mất form khi đổi query checkout hoặc bộ lọc.
- Card product/category và thumbnail gallery dùng lazy loading sẵn có, bổ sung decoding=async. Banner trang chủ ưu tiên fetch cao, không lazy ảnh chính.
- Không thêm thư viện, không cache dữ liệu nghiệp vụ hoặc đổi logic API/checkout/phân quyền. Chưa tách lazy cho từng dialog. Tách route làm giảm số module cần tải ban đầu; chưa đo kích thước bundle hoặc mức tăng tốc.
- Chỉ sửa/đọc source; theo yêu cầu không chạy build/test/lint hoặc browser benchmark. Cần nghiệm thu deep link, back/forward, mạng chậm/lỗi chunk và kích thước build khi được phép kiểm tra.


### Nền tảng UI Buyer/Seller: shadcn + GSAP + Lenis — 14/11/2026

- Cài skill chính thức shadcn (shadcn-ui/ui) và frontend-design (anthropics/skills) vào Codex user skills; nhận diện từ lượt tiếp theo.
- GSAP ^3.15.1 và Lenis ^1.3.27 có sẵn ở trạng thái bắt đầu, giữ nguyên; thêm @gsap/react ^2.1.2. Thêm clsx/tailwind-merge/class-variance-authority/radix-ui/lucide-react/tw-animate-css và lockfile qua npm.
- components.json: Vite/JavaScript, tsx=false, radix/new-york, Tailwind v4, icon Lucide. Alias @/ qua vite.config.js và jsconfig.json; cn dùng src/lib/utils.js. Các component CLI sinh được chỉnh về cùng utility cn, gỡ package cn dư.
- CLI thêm 18 component: button/card/input/textarea/label/badge/separator/skeleton/table/tabs/select/dialog/alert-dialog/sheet/dropdown-menu/avatar/checkbox/field. Source ở components/ui; chưa thay toàn bộ UI hiện có.
- Chuyển class Tailwind literal của 16 file page Buyer/Auth/Seller (gồm file khung SellerSectionPage cũ) sang const object ở module scope, giữ chuỗi class đầy đủ và logic hiện có. ShopPage giữ STYLES có sẵn; SellerReportsPage đưa cả formControl/variant nút vào object. Component con chưa refactor đồng loạt.
- Thêm UI-GUIDE.md ghi nguyên tắc const styles độc lập từng page, shadcn semantic tokens, GSAP cleanup/reduced motion và Lenis một instance. Chưa bật Lenis toàn cục hoặc GSAP animation để không đổi cuộn/modal/checkout trước bước redesign.
- Đã chạy cài dependencies và CLI info/docs/add; không chạy build/test/lint hoặc nghiệm thu trình duyệt. npm install báo 3 moderate vulnerabilities; chưa chạy audit fix hoặc thay phiên bản tự động.
- Bước tiếp theo: thống nhất layout/theme, xây UI Buyer rồi Seller theo từng trang, giữ nghiệp vụ và quyền; không redesign Admin trong phạm vi này.


### Dựng lại giao diện Halloween Buyer/Seller — 14/11/2026

- Áp dụng skill frontend-design và shadcn: định hướng trắng/xám/đen + cam bí ngô, primary cam sẫm, typography và hệ semantic tokens thống nhất. Chi tiết HALLOWEEN-DESIGN.md, UI-GUIDE.md.
- Khung: ribbon Halloween, Header search/actions, dropdown tài khoản có bàn phím bằng shadcn, Footer mới; Home hero SVG local và lối vào danh mục/sản phẩm/shop. Không tạo fake discount hoặc số liệu marketing.
- Buyer: Auth layout/form dùng Card/Field, card product và detail có variant/stock/cart thật, gallery theo theme; category/shop card, banner/logo shop, profile, cart/COD, orders/review/media và search dùng control/theme thống nhất. API/handler/role/cart/checkout idempotency giữ nguyên.
- Seller: sidebar tối Seller Studio, khu nội dung sáng; shop/logo/banner, product/variant/media/stock, orders và reports chuyển Button/Input/Textarea shadcn và theme. Biểu đồ giữ số liệu thật và màu hủy có ý nghĩa. Các dialog nghiệp vụ native giữ showModal/cleanup/lock/toast host.
- ConfirmDialog dùng shadcn AlertDialog, giữ callback và chặn đóng khi busy. Không tạo Toaster thứ hai. ProfileInfo bổ sung nhãn LOCKED theo trạng thái backend.
- GSAP: SeasonalEntrance/useGSAP tạo một lần intro hero, scope/cleanup và prefers-reduced-motion. Lenis: StorefrontScroll dynamic import, một controller active, bỏ Admin/reduced motion, prevent cuộn form/overlay, ngừng khi dialog mở; cleanup theo route. Fallback native scroll khi chunk lỗi.
- Giữ const styles độc lập cho các page Buyer/Auth/Seller. Không redesign các page nghiệp vụ Admin; header/footer/theme/component chia sẻ có thể thay đổi giao diện chung.
- Chỉ sửa/đọc source và tra CLI info/docs. Không chạy build/test/lint hoặc kiểm tra browser/API theo yêu cầu; chưa có bằng chứng responsive/focus/animation/checkout hoạt động thực tế sau thay đổi này.


### Thống nhất UI quản trị Admin — 14/11/2026

- Đồng bộ theme Admin với Buyer/Seller trắng/xám/đen/cam. Dựng lại Admin Studio sidebar icon và layout; tổng quan dùng Card/Badge shadcn cho 6 nhóm chức năng, không thêm fake metrics.
- Đồng bộ các trang user/shop/category/duyệt product/orders/doanh thu và các bảng/form/dialog bằng semantic tokens, Button/Input/Textarea; giữ màu trạng thái/cảnh báo. Loại các mảng gradient/decorative glow nhiều màu của header cũ.
- Mỗi page Admin có const styles ở module scope. Header/Footer/toast dùng chung. Native dialog và quyền ADMIN/khóa ghi/xác nhận/API handler vẫn giữ nguyên. Lenis tiếp tục bỏ Admin, cuộn native.
- Lịch sử trạng thái đơn vẫn ghi Chưa triển khai. Không bổ sung backend hoặc thay contract trong đợt UI này.
- Chỉ sửa/đọc source và tra cấu hình shadcn. Không chạy build/test/lint hoặc nghiệm thu trình duyệt/API theo yêu cầu.


### Font tiếng Việt thống nhất — 14/11/2026

- Cài @fontsource/be-vietnam-pro và cập nhật package-lock. Import normal 400–800 có Vietnamese/Latin unicode-range, font-display swap, tự phục vụ font từ app.
- Tailwind --font-sans/body/form controls dùng Be Vietnam Pro cho cả Buyer/Seller/Admin và dialog portal; bỏ Trebuchet MS. Giữ font-mono ở mã đơn/UUID.
- Chỉnh line-height và tracking hero để hạn chế dấu tiếng Việt bị chật. Cập nhật UI-GUIDE và HALLOWEEN-DESIGN.
- Chỉ cài package/sửa code/tài liệu, không chạy build/test/lint hoặc kiểm tra trình duyệt.

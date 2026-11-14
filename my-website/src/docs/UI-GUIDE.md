# UI Buyer / Seller — shadcn, GSAP, Lenis

Cập nhật 14/11/2026. Chuẩn bị nền tảng cho việc thay UI từng trang, giữ API và nghiệp vụ hiện có. Chưa coi cài thư viện là hoàn tất redesign.

## Công cụ

- shadcn/ui: source component trong src/components/ui, cấu hình components.json, JavaScript (tsx=false), alias @/ trỏ src, Tailwind v4 và semantic CSS variables trong src/index.css.
- cn() tại src/lib/utils.js dùng clsx + tailwind-merge. Ưu tiên variant/size của component; className của page dùng để bố trí.
- GSAP và Lenis có sẵn trước bước này; bổ sung @gsap/react để quản lý scope/cleanup animation theo lifecycle React. Không tạo nhiều ticker hoặc global scroll controller ở từng page.
- Skill shadcn và frontend-design đã cài vào Codex user skills; được nhận diện từ lượt tiếp theo.

## Cấu hình Tailwind tại từng page

Mỗi file page Buyer/Seller có một const độc lập ở module scope, ví dụ:

```jsx
const sellerProductsPageStyles = {
  page: "grid min-w-0 gap-6",
  toolbar: "flex flex-wrap items-center justify-between gap-3",
  filters: "grid gap-3 sm:grid-cols-2",
};

// <section className={sellerProductsPageStyles.page}>...</section>
```

Class phải là chuỗi đầy đủ để Tailwind quét được; không ghép động bg-${color}. Trạng thái dùng map hoặc hàm trong object và cn(). Không khởi tạo object lớn mỗi lần render. ShopPage giữ const STYLES có sẵn. Các page khác đã chuyển class literal ra object riêng; chưa thay giao diện/nghiệp vụ trong bước chuẩn bị này. Component con có style riêng, không kéo toàn bộ style của tất cả page vào một object global.

## Nguyên tắc tích hợp tiếp theo

1. Thống nhất theme/tokens rồi chuyển từng page, bắt đầu layout/header/footer, Buyer catalog/product/cart/checkout, tiếp theo Seller shop/product/orders/reports.
2. Dùng component shadcn thật; kiểm tra docs/API trước khi compose. Dialog phải có title, form có label, điều khiển focus và thông báo lỗi.
3. GSAP dùng useGSAP scope theo container, cleanup khi unmount và tôn trọng prefers-reduced-motion. Không ẩn nội dung vĩnh viễn nếu animation không chạy.
4. Lenis chỉ có một instance cho trang khi được tích hợp, có cleanup/ticker remove, xử lý đổi route và ngừng smooth scroll khi người dùng yêu cầu giảm chuyển động. Không chặn cuộn bên trong dialog, select, sheet hoặc textarea. Hiện chưa mount toàn cục.
5. Giữ auth/role guard, cart/checkout idempotency và toast hiện có khi thay UI. Không tự cài Toaster thứ hai. Khi đổi native dialog sang shadcn cần rà cơ chế toast host và portal cùng lúc.
6. Không áp dụng redesign Admin trong đợt Buyer/Seller nếu chưa có yêu cầu.

Không chạy build/test/lint theo yêu cầu. Cần kiểm tra hiển thị/mobile/focus/modal/scroll sau khi được phép nghiệm thu.


## Giao diện Halloween đã áp dụng — 14/11/2026

Design direction chi tiết ở HALLOWEEN-DESIGN.md. Buyer/Seller hiện dùng semantic theme trắng/xám/đen với cam bí ngô, primary cam sẫm cho chữ trắng dễ đọc; tím và màu trạng thái chỉ làm điểm phụ. Hình hero là SVG local tự dựng tại public/halloween-market.svg.

- Header/Footer, Home, AuthLayout/AuthForm, AccountMenu, ProductCard/ProductInfo, CategoryCard, ShopCard/ShopHeader, ProfileInfo, OrderCard và SellerSidebar/SellerLayout được dựng lại theo composition shadcn.
- Những page/form/dialog còn lại trong Buyer/Seller được áp dụng theme và Button/Input/Textarea shadcn, giữ nghiệp vụ. Checkbox/radio/range giữ native để không làm đổi FormData hoặc kích thước control.
- ConfirmDialog dùng AlertDialog shadcn có focus trap; các native dialog nghiệp vụ hiện có vẫn giữ lifecycle/khóa request/toast host và được đổi style cùng theme.
- SeasonalEntrance dùng useGSAP cho hero; StorefrontScroll khởi tạo Lenis theo route Buyer/Seller, bỏ Admin/reduced motion, ngừng khi dialog mở, cleanup khi đổi route. Search query/list shop query khởi tạo lại controller; query checkout không làm mất form.
- Các page giữ const Tailwind riêng. Shared UI/layout thay đổi có thể xuất hiện ở Admin nhưng không thay nghiệp vụ/page Admin.
- Không chạy build/test/lint, không mở trình duyệt hay nghiệm thu API trong lần này. Cần nghiệm thu mobile, cuộn dialog, keyboard, checkout và upload trước khi coi giao diện đã được xác minh.


## Đồng bộ Admin Studio — 14/11/2026

- AdminSidebar/AdminLayout dùng cùng cấu trúc Seller Studio: nav đen, active cam bí ngô, nội dung trắng/xám, spacing/width/mobile nhất quán. Role guard và Outlet context giữ nguyên.
- AdminHomePage dùng Card/Badge/Lucide điều hướng tới 6 khu vực đã có; không hiển thị số liệu tổng quan giả. Bỏ các accent/gradient nhiều màu cũ.
- User/shop/category/product/order/revenue page, các bảng/form/dialog Admin dùng semantic palette, Button/Input/Textarea shadcn; status Badge giữ màu có ý nghĩa. Các page có const Tailwind độc lập, class đầy đủ để Tailwind quét.
- Native dialog vẫn giữ showModal, cleanup, khóa mutation/xác nhận/toast host; checkbox/radio/range giữ native. Không thay API, filter/pagination hoặc quy tắc nghiệp vụ. Lịch sử trạng thái đơn vẫn Chưa triển khai.
- MainLayout áp dụng style nền/bảng/dialog chung cho Admin. Lenis vẫn bỏ Admin để trang quản trị dùng cuộn native.
- Không chạy build/test/lint, không kiểm tra browser/API; cần nghiệm thu responsive/focus/modal và thao tác quản trị thực tế.


## Font tiếng Việt — 14/11/2026

Dùng Be Vietnam Pro tự host bằng @fontsource/be-vietnam-pro, normal weights 400/500/600/700/800; CSS có unicode-range Vietnamese/Latin/Latin-ext và font-display: swap. Vite phục vụ file font cùng app, không gọi Google Fonts khi mở trang. Tailwind --font-sans và body/form controls dùng cùng font, fallback Segoe UI/Arial; font-mono cho UUID/mã đơn giữ nguyên. Body line-height 1.65, hero 1.18 và tracking -.035em để dấu tiếng Việt có khoảng thở. Thay font tập trung ở index.css, không thêm font riêng trong page. Chưa kiểm tra browser/build/test/lint.

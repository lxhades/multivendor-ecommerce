import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getShop } from "../../api/shop/shopApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import ShopHeader from "../../components/shop/ShopHeader.jsx";
import ShopProducts from "../../components/shop/ShopProducts.jsx";


// 1. CẤU HÌNH CSS TAILWIND CHUNG 
const STYLES = {
  link: "inline-flex min-h-11 w-fit items-center rounded-lg border border-primary/25 bg-card px-4 text-sm font-semibold text-primary",
  labelText: "mx-2",

  container: "mx-auto grid w-full max-w-7xl gap-7 px-4 py-6 sm:px-6 lg:px-8",
  
  // Breadcrumb
  breadcrumbWrapper: "text-sm text-muted-foreground",
  breadcrumbLink: "inline-flex min-h-11 items-center font-semibold hover:text-primary",
  
  // Trạng thái Loading
  loading: "rounded-xl border border-border bg-card p-8 text-sm text-muted-foreground",
  
  // Trạng thái Lỗi
  errorBox: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-6 text-sm text-red-700",
  errorTitle: "text-2xl font-extrabold tracking-tight",
  retryButton: "min-h-11 rounded-lg border border-red-200 px-4 font-semibold hover:bg-red-50 transition-colors",
};

// ==========================================
// 2. COMPONENT CHÍNH
// ==========================================
export default function ShopPage() {
  const { id } = useParams();
  // Key giúp reset state hoàn toàn khi chuyển sang shop khác
  return <ShopContent key={id} shopId={id} />;
}

function ShopContent({ shopId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);

  // --- HÀM XỬ LÝ ---
  const handleRetry = () => {
    setResult(null);
    setAttempt((value) => value + 1);
  };

  // --- GỌI API ---
  useEffect(() => {
    const request = new AbortController();

    getShop(shopId, { signal: request.signal, skipAuth: true })
      .then((shop) => {
        if (request.signal.aborted) return;
        if (shop?.id !== shopId) throw new Error("Thông tin cửa hàng không khớp.");
        setResult({ shop });
      })
      .catch((failure) => {
        if (!request.signal.aborted) {
          setResult({ 
            error: failure.response?.status === 404 
              ? "Không tìm thấy cửa hàng này." 
              : authErrorMessage(failure, "Không tải được thông tin cửa hàng.") 
          });
        }
      });

    return () => request.abort();
  }, [shopId, attempt]);

  // --- XỬ LÝ RENDER (Tách logic để tránh Ternary lồng nhau) ---
  let content;

  if (!result) {
    content = (
      <p role="status" className={STYLES.loading}>
        Đang tải cửa hàng…
      </p>
    );
  } else if (result.error) {
    content = (
      <div role="alert" className={STYLES.errorBox}>
        <h1 className={STYLES.errorTitle}>
          Chưa thể hiển thị cửa hàng
        </h1>
        <p>{result.error}</p>
        <Button variant="outline" type="button" onClick={handleRetry} className={STYLES.retryButton}>
          Thử lại
        </Button>
      </div>
    );
  } else {
    content = (
      <>
        <ShopHeader shop={result.shop} />
        <Link to={`/search?${new URLSearchParams({ shopId })}`} className={STYLES.link}>Tìm kiếm sản phẩm trong shop</Link>
        <ShopProducts shopId={shopId} />
      </>
    );
  }

  // --- RENDER CHÍNH ---
  return (
    <div className={STYLES.container}>
      
      {/* BREADCRUMB */}
      <nav aria-label="Điều hướng cửa hàng" className={STYLES.breadcrumbWrapper}>
        <Link to="/" className={STYLES.breadcrumbLink}>
          Trang chủ
        </Link>
        <span aria-hidden="true" className={STYLES.labelText}>
          /
        </span>
        <span>Cửa hàng</span>
      </nav>

      {/* NỘI DUNG (Loading / Error / Success) */}
      {content}
      
    </div>
  );
}
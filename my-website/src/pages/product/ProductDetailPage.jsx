import { Button } from "@/components/ui/button";
import { productPath } from "../../components/common/catalogPaths.js";
import { useEffect, useState } from "react";
import { Link, Navigate, useLocation, useParams } from "react-router-dom";

import { getProduct } from "../../api/product/productApi.js";
import ProductInfo from "../../components/product/ProductInfo";
import ProductReviewCard from "../../components/product/ProductReviewCard";

const productDetailPageStyles = {
  page: "mx-auto grid max-w-6xl gap-6 px-4 py-6 sm:px-6 lg:px-8",
  navigation: "flex min-w-0 flex-wrap items-center gap-2 text-sm text-muted-foreground",
  link: "inline-flex min-h-11 items-center rounded hover:text-primary",
  labelText: "min-w-0 break-words",
  description: "rounded-2xl border border-border bg-card p-6 text-sm text-muted-foreground",
  section: "grid justify-items-start gap-4 rounded-2xl border border-red-200 bg-card p-6",
  heading: "text-lg font-bold text-foreground",
  errorDescription: "text-sm text-red-700",
  button: "min-h-11 rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary",
};

export default function ProductDetailPage() {
  const { id } = useParams();

  // Cuộn lên đầu trang mỗi khi ID sản phẩm thay đổi
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [id]);

  return <ProductDetail key={id} id={id} />;
}

function ProductDetail({ id }) {
  const location = useLocation();
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    getProduct(id, { signal: controller.signal, skipAuth: true })
      .then((product) => {
        if (controller.signal.aborted) return;
        
        // Validate dữ liệu trả về
        if (product?.id !== id || typeof product.name !== "string") {
          throw new Error("Phản hồi sản phẩm không hợp lệ.");
        }
        
        setResult({ product });
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setResult({
            error:
              error.response?.status === 404
                ? "Không tìm thấy sản phẩm hoặc sản phẩm đã ngừng bán."
                : "Không tải được sản phẩm. Vui lòng thử lại.",
          });
        }
      });

    return () => controller.abort();
  }, [id, attempt]);

  // Hàm xử lý thử lại khi có lỗi
  const handleRetry = () => {
    setResult(null);
    setAttempt((value) => value + 1);
  };

  if (result?.product && location.pathname !== productPath(result.product)) {
    return <Navigate to={`${productPath(result.product)}${location.search}${location.hash}`} replace />;
  }

  return (
    <div className={productDetailPageStyles.page}>
      
      {/* BREADCRUMB */}
      <nav
        aria-label="Đường dẫn trang"
        className={productDetailPageStyles.navigation}
      >
        <Link
          to="/"
          className={productDetailPageStyles.link}
        >
          Trang chủ
        </Link>
        <span aria-hidden="true">
          /
        </span>
        <span
          aria-current="page"
          className={productDetailPageStyles.labelText}
        >
          {result?.product?.name || "Chi tiết sản phẩm"}
        </span>
      </nav>

      {/* TRẠNG THÁI LOADING */}
      {!result && (
        <p
          role="status"
          className={productDetailPageStyles.description}
        >
          Đang tải sản phẩm…
        </p>
      )}

      {/* TRẠNG THÁI LỖI */}
      {result?.error && (
        <section
          role="alert"
          className={productDetailPageStyles.section}
        >
          <h1 className={productDetailPageStyles.heading}>
            Chưa xem được sản phẩm
          </h1>
          <p className={productDetailPageStyles.errorDescription}>
            {result.error}
          </p>
          <Button
            type="button"
            onClick={handleRetry}
            className={productDetailPageStyles.button}
          >
            Thử lại
          </Button>
        </section>
      )}

      {/* TRẠNG THÁI THÀNH CÔNG */}
      {result?.product && (
        <>
          <ProductInfo product={result.product} />
          <ProductReviewCard productId={result.product.id} />
        </>
      )}
      
    </div>
  );
}
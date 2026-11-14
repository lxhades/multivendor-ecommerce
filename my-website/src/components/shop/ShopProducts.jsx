import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { getProducts } from "../../api/product/productApi.js";
import ProductCard from "../product/ProductCard.jsx";
import { authErrorMessage } from "../auth/authHelpers.js";

export default function ShopProducts({ shopId }) {
  // --- KHU VỰC STATE ---
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

    getProducts({ shopId }, { signal: request.signal, skipAuth: true })
      .then((data) => {
        if (request.signal.aborted) return;
        
        const products = data?.items ?? data;
        // Validate dữ liệu trả về
        if (!Array.isArray(products) || products.some((product) => !product?.id || product.shopId !== shopId)) {
          throw new Error("Danh sách sản phẩm của cửa hàng không hợp lệ.");
        }
        
        setResult({ products });
      })
      .catch((failure) => {
        if (!request.signal.aborted) {
          setResult({ error: authErrorMessage(failure, "Không tải được sản phẩm của cửa hàng.") });
        }
      });

    return () => request.abort();
  }, [shopId, attempt]);

  // --- XỬ LÝ RENDER (Tránh Ternary lồng nhau) ---
  let content;

  if (!result) {
    // Trạng thái 1: Đang tải
    content = (
      <p role="status" className="rounded-xl border border-border bg-card p-5 text-sm text-muted-foreground">
        Đang tải sản phẩm…
      </p>
    );
  } else if (result.error) {
    // Trạng thái 2: Lỗi
    content = (
      <div role="alert" className="grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700">
        <p>{result.error}</p>
        <Button variant="outline" 
          type="button" 
          onClick={handleRetry} 
          className="min-h-11 rounded-lg border border-red-200 px-4 font-semibold hover:bg-red-50 transition-colors"
        >
          Thử lại
        </Button>
      </div>
    );
  } else if (result.products.length === 0) {
    // Trạng thái 3: Không có sản phẩm
    content = (
      <p className="rounded-xl border border-border bg-card p-8 text-center text-sm text-muted-foreground">
        Cửa hàng chưa có sản phẩm đang bán.
      </p>
    );
  } else {
    // Trạng thái 4: Có sản phẩm
    content = (
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
        {result.products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    );
  }

  // --- RENDER CHÍNH ---
  return (
    <section aria-labelledby="shop-products-title" className="grid gap-4">
      
      {/* HEADER CỦA SECTION */}
      <div className="flex flex-wrap items-end justify-between gap-3">
        <h2 id="shop-products-title" className="text-xl font-bold sm:text-2xl">
          Sản phẩm của shop
        </h2>
        {result?.products && (
          <p className="text-sm text-muted-foreground">
            {result.products.length} sản phẩm đang hiển thị
          </p>
        )}
      </div>

      {/* NỘI DUNG ĐỘNG (Loading / Error / Empty / Grid) */}
      {content}
      
    </section>
  );
}
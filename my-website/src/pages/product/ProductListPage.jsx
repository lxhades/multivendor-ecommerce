import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { getProducts, searchProducts } from "../../api/product/productApi.js";
import ProductCard from "../../components/product/ProductCard";

const productListPageStyles = {
  section: " grid max-w-7xl scroll-mt-6 gap-4 ",
  container: "flex flex-wrap items-end justify-between gap-3",
  heading: "sr-only",
  description: "text-sm text-muted-foreground",
  description2: "rounded-xl border border-border bg-card p-5 text-sm text-muted-foreground",
  errorContainer: "rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button: "mt-3 min-h-11 rounded-lg border border-red-200 px-4 font-semibold",
  description3: "rounded-xl border border-border bg-card p-5 text-muted-foreground",
  container2: "grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4 xl:gap-5",
};

export default function ProductListPage() {
  const [params] = useSearchParams();
  const keyword = params.get("q")?.trim() ?? "";
  const categoryId = params.get("categoryId") ?? "";
  const shopId = params.get("shopId") ?? "";
  return <ProductResults key={`${keyword}:${categoryId}:${shopId}`} keyword={keyword} categoryId={categoryId} shopId={shopId} />;
}

function ProductResults({ keyword, categoryId, shopId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      try {
        const filters = { ...(categoryId && { categoryId, includeChildren: true }), ...(shopId && { shopId }) };
        const data = keyword ? await searchProducts({ ...filters, q: keyword }, controller.signal)
          : await getProducts(filters, { signal: controller.signal, skipAuth: true });
        const products = data?.items ?? data;
        if (!Array.isArray(products)) throw new Error("Phản hồi danh sách sản phẩm không hợp lệ.");
        if (!controller.signal.aborted) setResult({ products });
      } catch (error) {
        if (!controller.signal.aborted) setResult({ error: error.response?.data?.message || error.message });
      }
    }
    void load();
    return () => controller.abort();
  }, [keyword, categoryId, shopId, attempt]);
  return <section id="products" className={productListPageStyles.section}>
    <div className={productListPageStyles.container}>
      
        <h2 className={productListPageStyles.heading}>{keyword ? `Kết quả cho “${keyword}”` : "Danh sách sản phẩm"}</h2>
      {result?.products && <p className={productListPageStyles.description}>{result.products.length} sản phẩm đang hiển thị</p>}
    </div>
    {!result && <p role="status" className={productListPageStyles.description2}>Đang tải sản phẩm…</p>}
    {result?.error && <div role="alert" className={productListPageStyles.errorContainer}>
      <p>Không tải được sản phẩm: {result.error}</p>
      <Button variant="outline" type="button" className={productListPageStyles.button} onClick={() => { setResult(null); setAttempt((value) => value + 1); }}>Thử lại</Button>
    </div>}

    {result?.products?.length === 0 && <p className={productListPageStyles.description3}>Chưa tìm thấy sản phẩm phù hợp.</p>}
    <div className={productListPageStyles.container2}>
      {result?.products?.map((product) => <ProductCard key={product.id} product={product} />)}
    </div>
  </section>;
}

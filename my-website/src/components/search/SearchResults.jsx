import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { searchProducts } from "../../api/product/productApi.js";
import ProductCard from "../product/ProductCard.jsx";
import { authErrorMessage } from "../auth/authHelpers.js";
import { searchApiParams, searchParamsFor, validateSearchPage } from "./searchModel.js";

export default function SearchResults({ filters, onChange }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const request = new AbortController();
    searchProducts(searchApiParams(filters), request.signal).then((data) => {
      if (!request.signal.aborted) setResult({ data: validateSearchPage(data, filters) });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được kết quả tìm kiếm.") }); });
    return () => request.abort();
  }, [filters, attempt]);
  if (!result) return <div role="status" className="rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground">Đang tìm sản phẩm…</div>;
  if (result.error) return <div role="alert" className="grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="min-h-11 rounded-lg border border-red-200 px-4">Thử lại</Button></div>;
  const data = result.data;
  const lastPage = Math.min(200, data.totalPages);
  const pages = [...new Set([1, filters.page - 1, filters.page, filters.page + 1, lastPage].filter((page) => page >= 1 && page <= lastPage))].sort((a, b) => a - b);
  return <div className="grid min-w-0 gap-5" aria-live="polite">
    <p className="text-sm text-muted-foreground">Tìm thấy <strong className="text-foreground">{data.totalElements.toLocaleString("vi-VN")}</strong> sản phẩm{data.items.length > 0 && ` · Hiển thị ${(filters.page - 1) * filters.size + 1}–${(filters.page - 1) * filters.size + data.items.length}`}.</p>
    {!data.items.length ? <div className="grid justify-items-start gap-3 rounded-2xl border border-border bg-card p-6"><p className="font-semibold">{data.totalElements ? "Trang này không còn kết quả." : "Không tìm thấy sản phẩm phù hợp."}</p><p className="text-sm leading-7 text-muted-foreground">Thử đổi từ khóa, mở rộng khoảng giá hoặc bỏ bộ lọc.</p>{filters.page > 1 && <Button variant="outline" type="button" onClick={() => onChange(searchParamsFor(filters, { page: 1 }))} className="min-h-11 rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary">Về trang đầu</Button>}</div>
      : <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4">{data.items.map((product) => <ProductCard key={product.id} product={product} />)}</div>}
    {(lastPage > 1 || filters.page > 1) && <nav aria-label="Phân trang tìm kiếm sản phẩm" className="flex flex-wrap items-center justify-center gap-2 text-sm"><Button variant="outline" type="button" disabled={filters.page <= 1} onClick={() => onChange(searchParamsFor(filters, { page: filters.page - 1 }))} className="min-h-11 rounded-lg border border-input bg-card px-3 disabled:opacity-50">Trước</Button>{pages.map((page, index) => <span key={page} className="flex items-center gap-2">{index > 0 && page - pages[index - 1] > 1 && <span aria-hidden="true">…</span>}<Button variant="default" type="button" aria-current={page === filters.page ? "page" : undefined} onClick={() => onChange(searchParamsFor(filters, { page }))} className={`min-h-11 min-w-11 rounded-lg border px-3 ${page === filters.page ? "border-primary bg-primary text-white" : "border-input bg-card"}`}>{page}</Button></span>)}<Button variant="outline" type="button" disabled={filters.page >= lastPage} onClick={() => onChange(searchParamsFor(filters, { page: filters.page + 1 }))} className="min-h-11 rounded-lg border border-input bg-card px-3 disabled:opacity-50">Sau</Button></nav>}
    {data.totalPages > 200 && <p className="text-xs text-muted-foreground">API hỗ trợ tối đa 200 trang. Hãy thu hẹp từ khóa hoặc bộ lọc để xem chính xác hơn.</p>}
  </div>;
}

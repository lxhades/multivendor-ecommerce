import { Button } from "@/components/ui/button";
import { useEffect, useId, useState } from "react";
import { getProductReviews } from "../../api/order/reviewApi.js";
import ReviewItem from "./ReviewItem";

export default function ProductReviewCard({ productId }) {
  return productId ? <ProductReviews key={productId} productId={productId} /> : null;
}

function ProductReviews({ productId }) {
  const [page, setPage] = useState(0);
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState(null);
  const titleId = useId();

  useEffect(() => {
    const controller = new AbortController();
    getProductReviews(productId, { page, size: 5 }, { signal: controller.signal }).then((data) => {
      if (!controller.signal.aborted) setResult({ data });
    }).catch((error) => {
      if (!controller.signal.aborted) setResult({ error: error.response?.data?.message || "Không tải được đánh giá. Vui lòng thử lại." });
    });
    return () => controller.abort();
  }, [productId, page, attempt]);

  function changePage(nextPage) {
    setResult(null);
    setPage(nextPage);
  }

  const data = result?.data;
  return <section aria-labelledby={titleId} className="grid min-w-0 gap-5 rounded-2xl border border-border bg-card p-5 sm:p-7">
    <div className="flex flex-wrap items-center justify-between gap-3">
      <h2 id={titleId} className="text-lg font-bold text-foreground">Bình luận & đánh giá</h2>
      {data && <span className="text-sm text-muted-foreground">{data.totalElements.toLocaleString("vi-VN")} đánh giá</span>}
    </div>
    {!result && <p role="status" className="py-3 text-sm text-muted-foreground">Đang tải đánh giá…</p>}
    {result?.error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
      <p>{result.error}</p>
      <Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="mt-3 min-h-11 rounded-lg border border-red-200 bg-card px-3 font-semibold">Thử lại</Button>
    </div>}
    {data && <>
      {data.totalElements > 0 ? <div className="flex flex-wrap items-center gap-3 rounded-xl border border-primary/15 bg-accent p-4 sm:gap-5">
        <p className="text-accent-foreground"><span className="text-3xl font-extrabold">{data.averageRating.toLocaleString("vi-VN", { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</span><span className="ml-1 text-sm">/ 5</span></p>
        <div className="text-sm"><p className="font-semibold text-foreground">Điểm đánh giá trung bình</p><p className="mt-1 text-muted-foreground">Từ {data.totalElements.toLocaleString("vi-VN")} lượt đánh giá của sản phẩm</p></div>
      </div> : <p className="rounded-xl bg-background p-5 text-sm text-muted-foreground">Sản phẩm chưa có bình luận hoặc đánh giá.</p>}
      {data.items.length > 0 && <ul className="divide-y divide-border">
        {data.items.map((review) => <li key={review.id} className="min-w-0"><ReviewItem review={review} /></li>)}
      </ul>}
      {data.totalElements > 0 && data.items.length === 0 && <p className="text-sm text-muted-foreground">Không có đánh giá ở trang này.</p>}
      {(data.totalPages > 1 || page > 0) && <nav aria-label="Phân trang đánh giá" className="flex flex-wrap items-center justify-center gap-3 border-t border-border/60 pt-5 text-sm">
        <Button variant="outline" type="button" disabled={page <= 0} onClick={() => changePage(page - 1)} className="min-h-11 rounded-lg border border-input px-3 font-semibold hover:border-primary/60 disabled:cursor-not-allowed disabled:opacity-40">Trang trước</Button>
        <span>Trang {page + 1}{data.totalPages > 0 ? ` / ${data.totalPages}` : ""}</span>
        <Button variant="outline" type="button" disabled={page + 1 >= data.totalPages} onClick={() => changePage(page + 1)} className="min-h-11 rounded-lg border border-input px-3 font-semibold hover:border-primary/60 disabled:cursor-not-allowed disabled:opacity-40">Trang sau</Button>
      </nav>}
    </>}
  </section>;
}

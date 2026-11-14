import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getShops } from "../../api/shop/shopApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import ShopCard from "../../components/shop/ShopCard.jsx";

const shopListPageStyles = {
  page: "mx-auto grid max-w-7xl gap-3 px-4 py-10",
  page2: "mx-auto grid w-full max-w-7xl gap-8 px-4 py-10 sm:px-6 lg:px-8",
  heading: "text-3xl font-extrabold",
  description: "mt-2 text-sm text-muted-foreground",
  description2: "py-8 text-sm text-muted-foreground",
  errorContainer: "grid justify-items-start gap-3 text-sm text-red-700",
  button: "min-h-11 rounded-lg border px-4",
  description3: "text-sm text-muted-foreground",
  container: "grid justify-items-start gap-3 rounded-xl border bg-card p-6",
  button2: "min-h-11 text-primary underline",
  container2: "grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4",
  navigation: "flex flex-wrap items-center justify-center gap-3 text-sm",
  element: "min-h-11 rounded-lg border bg-card px-3 disabled:opacity-50",
};

export default function ShopListPage() {
  const [params, setParams] = useSearchParams();
  const page = Number(params.get("page") || 1);
  if (!Number.isSafeInteger(page) || page < 1 || page > 10000) return <div className={shopListPageStyles.page}><p role="alert">Trang cửa hàng không hợp lệ.</p><Link to="/shops">Về trang đầu</Link></div>;
  return <ShopList key={page} page={page} onPage={(value) => setParams(value === 1 ? {} : { page: value })} />;
}
function ShopList({ page, onPage }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const request = new AbortController();
    getShops({ page: page - 1, size: 12, sort: "createdAt,desc" }, { signal: request.signal, skipAuth: true }).then((data) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(data?.content) || data.number !== page - 1 || !Number.isSafeInteger(data.totalElements) || !Number.isInteger(data.totalPages) || data.content.some((shop) => !shop?.id || typeof shop.name !== "string")) throw new Error("Danh sách cửa hàng không hợp lệ.");
      setResult({ data });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được cửa hàng.") }); });
    window.scrollTo({ top: 0, behavior: "instant" });
    return () => request.abort();
  }, [page, attempt]);
  const data = result?.data;
  return <div className={shopListPageStyles.page2}><div><h1 className={shopListPageStyles.heading}>Khám phá cửa hàng</h1><p className={shopListPageStyles.description}>Thông tin, sản phẩm và trạng thái hoạt động của các shop trên Market Lane.</p></div>
    {!result ? <p role="status" className={shopListPageStyles.description2}>Đang tải cửa hàng…</p> : result.error ? <div role="alert" className={shopListPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className={shopListPageStyles.button}>Thử lại</Button></div> : <>
      <p className={shopListPageStyles.description3}>{data.totalElements.toLocaleString("vi-VN")} cửa hàng</p>
      {!data.content.length ? <div className={shopListPageStyles.container}><p>Chưa có cửa hàng trong trang này.</p>{page > 1 && <Button variant="outline" type="button" onClick={() => onPage(1)} className={shopListPageStyles.button2}>Về trang đầu</Button>}</div> : <div className={shopListPageStyles.container2}>{data.content.map((shop) => <ShopCard key={shop.id} shop={shop} />)}</div>}
      {(data.totalPages > 1 || page > 1) && <nav aria-label="Phân trang cửa hàng" className={shopListPageStyles.navigation}><Button variant="outline" type="button" disabled={page <= 1} onClick={() => onPage(page - 1)} className={shopListPageStyles.element}>Trang trước</Button><span>Trang {page}/{Math.max(1, data.totalPages)}</span><Button variant="outline" type="button" disabled={page >= data.totalPages || page >= 10000} onClick={() => onPage(page + 1)} className={shopListPageStyles.element}>Trang sau</Button></nav>}
    </>}
  </div>;
}

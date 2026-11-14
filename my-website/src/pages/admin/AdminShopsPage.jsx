import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getAdminShops } from "../../api/shop/adminShopApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { searchText } from "../../components/seller/sellerProductHelpers.js";
import { adminShopStatuses, validateAdminShopPage } from "../../components/admin/adminShopModel.js";
import AdminShopsTable from "../../components/admin/AdminShopsTable.jsx";
import AdminShopDialog from "../../components/admin/AdminShopDialog.jsx";

const adminShopsPageStyles = {
  container: "grid min-w-0 gap-5",
  container2: "flex flex-wrap items-center justify-between gap-3",
  title: "text-2xl font-extrabold tracking-tight",
  description: "mt-1 text-sm text-muted-foreground",
  button: "min-h-11 rounded-lg border border-border bg-card px-4 text-sm font-semibold disabled:opacity-50",
  filters: "grid gap-4 rounded-xl border border-border bg-card p-4 sm:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_180px_110px_auto]",
  label: "grid min-w-0 gap-2 text-xs font-semibold",
  input: "min-h-12 min-w-0 rounded-lg border border-input px-3 text-sm",
  label2: "grid gap-2 text-xs font-semibold",
  select: "min-h-12 rounded-lg border border-input bg-card px-3 text-sm",
  button2: "min-h-12 self-end rounded-lg border border-input px-3 text-sm",
  description2: "text-xs leading-6 text-muted-foreground",
  description3: "rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground",
  errorContainer: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button3: "min-h-11 rounded-lg border border-red-200 px-4",
  description4: "text-sm text-muted-foreground",
  description5: "rounded-xl border border-border bg-card p-8 text-center text-sm text-muted-foreground",
  navigation: "flex flex-wrap items-center justify-center gap-3 text-sm",
  button4: "min-h-11 rounded-lg border bg-card px-3 disabled:opacity-50",
};

export default function AdminShopsPage() {
  const { auth } = useOutletContext();
  return <AdminShops key={auth.user.userId} adminId={auth.user.userId} />;
}
function AdminShops({ adminId }) {
  const [result, setResult] = useState(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [attempt, setAttempt] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [selected, setSelected] = useState(null);
  useEffect(() => {
    const request = new AbortController();
    getAdminShops({ page, size, sort: "createdAt,desc" }, { signal: request.signal }).then((data) => {
      if (request.signal.aborted) return;
      const checked = validateAdminShopPage(data, page, size);
      const lastPage = Math.max(0, checked.totalPages - 1);
      if (page > lastPage) { setPage(lastPage); return; }
      setResult({ data: checked });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được danh sách cửa hàng.") }); });
    return () => request.abort();
  }, [page, size, attempt]);
  function refresh() { setResult(null); setAttempt((value) => value + 1); }
  function changePage(next) { setResult(null); setPage(next); }
  const data = result?.data;
  const term = searchText(keyword);
  const shops = data?.content.filter((shop) => (!status || shop.status === status) && searchText([shop.id, shop.name, shop.slug, shop.sellerId, shop.address].join(" ")).includes(term)) ?? [];
  return <div className={adminShopsPageStyles.container}>
    <div className={adminShopsPageStyles.container2}><div><h2 className={adminShopsPageStyles.title}>Quản lý cửa hàng</h2><p className={adminShopsPageStyles.description}>Xem thông tin và quản lý trạng thái hoạt động của các shop.</p></div><Button variant="outline" type="button" disabled={!result || Boolean(selected)} onClick={refresh} className={adminShopsPageStyles.button}>Tải lại</Button></div>
    <fieldset disabled={Boolean(selected)} className={adminShopsPageStyles.filters}>
      <label className={adminShopsPageStyles.label}>Tìm trong trang hiện tại<Input type="search" value={keyword} onChange={(event) => setKeyword(event.target.value)} maxLength={150} placeholder="Tên shop, slug, ID shop hoặc seller…" className={adminShopsPageStyles.input} /></label>
      <label className={adminShopsPageStyles.label2}>Trạng thái trong trang<select value={status} onChange={(event) => setStatus(event.target.value)} className={adminShopsPageStyles.select}><option value="">Tất cả</option>{Object.entries(adminShopStatuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <label className={adminShopsPageStyles.label2}>Mỗi trang<select value={size} onChange={(event) => { setResult(null); setPage(0); setSize(Number(event.target.value)); }} className={adminShopsPageStyles.select}>{[10, 20, 50].map((value) => <option key={value} value={value}>{value}</option>)}</select></label>
      <Button variant="outline" type="button" onClick={() => { setKeyword(""); setStatus(""); }} className={adminShopsPageStyles.button2}>Bỏ lọc</Button>
    </fieldset>
    <p className={adminShopsPageStyles.description2}>Tìm kiếm và lọc trạng thái chỉ áp dụng cho các shop trong trang đang xem. API hiện phân trang toàn bộ shop; tổng số bên dưới không phải số shop khớp bộ lọc trên toàn hệ thống.</p>
    {!result ? <p role="status" className={adminShopsPageStyles.description3}>Đang tải cửa hàng…</p> : result.error ? <div role="alert" className={adminShopsPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={adminShopsPageStyles.button3}>Thử lại</Button></div> : <>
      <p className={adminShopsPageStyles.description4}>Tổng {data.totalElements.toLocaleString("vi-VN")} cửa hàng · Hiển thị {shops.length}/{data.content.length} shop trong trang này.</p>
      {!shops.length ? <p className={adminShopsPageStyles.description5}>{keyword || status ? "Không có shop khớp bộ lọc trong trang hiện tại. Thử bỏ lọc hoặc chuyển trang." : "Chưa có cửa hàng trong trang này."}</p> : <AdminShopsTable shops={shops} onOpen={(shopId, desiredStatus) => setSelected({ shopId, desiredStatus })} />}
      {(data.totalPages > 1 || page > 0) && <nav aria-label="Phân trang cửa hàng quản trị" className={adminShopsPageStyles.navigation}><Button variant="outline" type="button" disabled={page === 0 || Boolean(selected)} onClick={() => changePage(page - 1)} className={adminShopsPageStyles.button4}>Trang trước</Button><span>Trang {page + 1}/{Math.max(1, data.totalPages)}</span><Button variant="outline" type="button" disabled={page + 1 >= data.totalPages || Boolean(selected)} onClick={() => changePage(page + 1)} className={adminShopsPageStyles.button4}>Trang sau</Button></nav>}
    </>}
    {selected && <AdminShopDialog key={`${selected.shopId}:${selected.desiredStatus ?? "detail"}`} shopId={selected.shopId} desiredStatus={selected.desiredStatus} adminId={adminId} onClose={() => setSelected(null)} onChanged={refresh} />}
  </div>;
}

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { getMyShop } from "../../api/shop/sellerShopApi.js";
import { getSellerOrderPage } from "../../api/order/sellerOrderApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { toast } from "../../components/common/toast.js";
import { orderStatuses } from "../../components/order/orderHelpers.js";
import { searchText } from "../../components/seller/sellerProductHelpers.js";
import { validateSellerOrderPage } from "../../components/seller/sellerOrderHelpers.js";
import SellerOrdersTable from "../../components/seller/SellerOrdersTable.jsx";
import SellerOrderDialog from "../../components/seller/SellerOrderDialog.jsx";

const sellerOrdersPageStyles = {
  description: "rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground",
  container: "grid justify-items-start gap-3 rounded-xl border border-primary/25 bg-accent p-5 text-sm",
  link: "inline-flex min-h-11 items-center font-semibold text-primary underline",
  errorContainer: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button: "min-h-11 rounded-lg border border-red-200 px-4",
  container2: "grid min-w-0 gap-5",
  container3: "flex flex-wrap items-center justify-between gap-3",
  title: "text-2xl font-extrabold tracking-tight",
  description2: "mt-1 break-words text-sm text-muted-foreground",
  button2: "min-h-11 rounded-lg border border-border bg-card px-4 text-sm font-semibold disabled:opacity-50",
  form: "rounded-xl border border-border bg-card p-4",
  filters: "grid gap-3 sm:grid-cols-2 xl:grid-cols-5",
  label: "grid gap-2 text-xs font-semibold",
  select: "min-h-11 min-w-0 rounded-lg border border-input bg-card px-3 text-sm",
  input: "min-h-11 min-w-0 rounded-lg border border-input px-3 text-sm",
  select2: "min-h-11 rounded-lg border border-input bg-card px-3 text-sm",
  primaryButton: "min-h-11 self-end rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50",
  description3: "text-xs text-muted-foreground",
  label2: "grid gap-2 text-sm font-semibold",
  input2: "min-h-12 min-w-0 rounded-lg border border-input bg-card px-3 text-sm font-normal",
  description4: "text-sm text-muted-foreground",
  description5: "rounded-xl border border-border bg-card p-8 text-center text-sm text-muted-foreground",
  navigation: "flex flex-wrap items-center justify-center gap-3 text-sm",
  button3: "min-h-11 rounded-lg border border-input bg-card px-3 disabled:opacity-50",
};

export default function SellerOrdersPage() {
  const { auth } = useOutletContext();
  return <SellerOrdersShop key={auth.user.userId} sellerId={auth.user.userId} />;
}
function SellerOrdersShop({ sellerId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const request = new AbortController();
    getMyShop({ signal: request.signal }).then((shop) => {
      if (request.signal.aborted) return;
      if (!shop?.id || shop.sellerId !== sellerId) throw new Error("Thông tin cửa hàng không khớp tài khoản.");
      setResult({ shop });
    }).catch((failure) => {
      if (request.signal.aborted) return;
      if (failure.response?.status === 404 && (failure.response.data?.message === "Không tìm thấy shop" || failure.response.data?.error === "SHOP_NOT_FOUND")) setResult({ missingShop: true });
      else setResult({ error: authErrorMessage(failure, "Không tải được cửa hàng.") });
    });
    return () => request.abort();
  }, [sellerId, attempt]);
  if (!result) return <p role="status" className={sellerOrdersPageStyles.description}>Đang tải cửa hàng…</p>;
  if (result.missingShop) return <div className={sellerOrdersPageStyles.container}><p>Bạn chưa có cửa hàng để quản lý đơn hàng.</p><Link to="/seller/shop" className={sellerOrdersPageStyles.link}>Tạo cửa hàng</Link></div>;
  if (result.error) return <div role="alert" className={sellerOrdersPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className={sellerOrdersPageStyles.button}>Thử lại</Button></div>;
  return <SellerOrders key={result.shop.id} shop={result.shop} sellerId={sellerId} />;
}
function SellerOrders({ shop, sellerId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [filters, setFilters] = useState({ status: "", from: "", to: "", size: 20 });
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [selected, setSelected] = useState(null);
  useEffect(() => {
    const request = new AbortController();
    const params = { page, size: filters.size, ...(filters.status && { status: filters.status }), ...(filters.from && { from: filters.from }), ...(filters.to && { to: filters.to }) };
    getSellerOrderPage(shop.id, params, { signal: request.signal }).then((data) => {
      if (request.signal.aborted) return;
      const checked = validateSellerOrderPage(data, shop.id, page, filters.size);
      const lastPage = Math.max(0, checked.totalPages - 1);
      if (page > lastPage) { setPage(lastPage); return; }
      setResult({ data: checked });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được danh sách đơn hàng.") }); });
    return () => request.abort();
  }, [shop.id, filters, page, attempt]);
  function refresh() { setResult(null); setAttempt((value) => value + 1); }
  function apply(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const from = String(form.get("from") ?? "");
    const to = String(form.get("to") ?? "");
    if (from && to && from > to) { toast.error("Ngày bắt đầu không được sau ngày kết thúc."); return; }
    setResult(null); setPage(0); setKeyword("");
    setFilters({ from, to, status: String(form.get("status") ?? ""), size: Number(form.get("size")) });
  }
  function changePage(next) { setResult(null); setKeyword(""); setPage(next); }
  const data = result?.data;
  const term = searchText(keyword);
  const orders = data?.orders.filter((order) => searchText([order.id, order.shippingAddress, ...order.items.map((item) => `${item.productName ?? ""} ${item.variantName ?? ""}`)].join(" ")).includes(term)) ?? [];
  return <div className={sellerOrdersPageStyles.container2}>
    <div className={sellerOrdersPageStyles.container3}><div><h2 className={sellerOrdersPageStyles.title}>Quản lý đơn hàng</h2><p className={sellerOrdersPageStyles.description2}>Đơn hàng của {shop.name}.</p></div><Button variant="outline" type="button" onClick={refresh} disabled={!result || Boolean(selected)} className={sellerOrdersPageStyles.button2}>Tải lại</Button></div>
    <form onSubmit={apply} className={sellerOrdersPageStyles.form}><fieldset disabled={Boolean(selected)} className={sellerOrdersPageStyles.filters}>
      <label className={sellerOrdersPageStyles.label}>Trạng thái<select name="status" defaultValue="" className={sellerOrdersPageStyles.select}><option value="">Tất cả</option>{Object.entries(orderStatuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <label className={sellerOrdersPageStyles.label}>Từ ngày đặt<Input name="from" type="date" className={sellerOrdersPageStyles.input} /></label>
      <label className={sellerOrdersPageStyles.label}>Đến ngày đặt<Input name="to" type="date" className={sellerOrdersPageStyles.input} /></label>
      <label className={sellerOrdersPageStyles.label}>Số đơn/trang<select name="size" defaultValue="20" className={sellerOrdersPageStyles.select2}>{[10, 20, 50].map((size) => <option key={size} value={size}>{size}</option>)}</select></label>
      <Button variant="default" type="submit" className={sellerOrdersPageStyles.primaryButton}>Áp dụng</Button>
    </fieldset></form>
    <p className={sellerOrdersPageStyles.description3}>Bộ lọc ngày theo ngày đặt hàng, múi giờ Việt Nam; bao gồm cả ngày kết thúc.</p>
    <label className={sellerOrdersPageStyles.label2}>Tìm trong trang hiện tại<Input type="search" disabled={Boolean(selected)} value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Mã đơn, người nhận, điện thoại, sản phẩm…" className={sellerOrdersPageStyles.input2} /></label>
    {!result ? <p role="status" className={sellerOrdersPageStyles.description}>Đang tải đơn hàng…</p> : result.error ? <div role="alert" className={sellerOrdersPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={sellerOrdersPageStyles.button}>Thử lại</Button></div> : <>
      <p className={sellerOrdersPageStyles.description4}>Tổng {data.totalElements.toLocaleString("vi-VN")} đơn theo bộ lọc · Hiển thị {orders.length}/{data.orders.length} đơn trong trang này.</p>
      {!orders.length ? <p className={sellerOrdersPageStyles.description5}>{keyword ? "Không có đơn phù hợp trong trang hiện tại. Hãy bỏ từ khóa hoặc chuyển trang khác." : "Không có đơn hàng theo bộ lọc đã chọn."}</p> : <SellerOrdersTable orders={orders} onOpen={setSelected} />}
      {(data.totalPages > 1 || page > 0) && <nav aria-label="Phân trang đơn hàng Seller" className={sellerOrdersPageStyles.navigation}><Button variant="outline" type="button" disabled={page === 0 || Boolean(selected)} onClick={() => changePage(page - 1)} className={sellerOrdersPageStyles.button3}>Trang trước</Button><span>Trang {page + 1}/{Math.max(1, data.totalPages)}</span><Button variant="outline" type="button" disabled={page + 1 >= data.totalPages || Boolean(selected)} onClick={() => changePage(page + 1)} className={sellerOrdersPageStyles.button3}>Trang sau</Button></nav>}
    </>}
    {selected && <SellerOrderDialog key={selected} orderId={selected} shopId={shop.id} sellerId={sellerId} onClose={() => setSelected(null)} onChanged={refresh} />}
  </div>;
}

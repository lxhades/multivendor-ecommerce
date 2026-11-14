import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useId, useRef, useState } from "react";
import { getAdminOrderPage } from "../../api/order/adminOrderApi.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { money } from "../product/productHelpers.js";
import { orderDate, orderStatuses, paymentStatuses } from "../order/orderHelpers.js";
import { validateReportRange } from "../seller/sellerReportHelpers.js";
import { validateAdminOrderPage } from "./adminOrderModel.js";
import AdminOrderDialog from "./AdminOrderDialog.jsx";

export default function AdminShopOrdersDialog({ shop, initialRange, adminId, onClose, onChanged }) {
  const dialog = useRef(null), titleId = useId();
  const [result, setResult] = useState(null), [attempt, setAttempt] = useState(0);
  const [filters, setFilters] = useState(() => ({ ...initialRange, dateBasis: "updatedAt", status: "DELIVERED" }));
  const [draft, setDraft] = useState(filters), [page, setPage] = useState(0), [selected, setSelected] = useState(null);
  useEffect(() => {
    const node = dialog.current; node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); };
  }, []);
  useEffect(() => {
    const request = new AbortController();
    getAdminOrderPage({ shopId: shop.id, from: filters.from, to: filters.to,
      dateBasis: filters.dateBasis === "updatedAt" ? "UPDATED_AT" : "CREATED_AT",
      ...(filters.status && { status: filters.status }), page, size: 20 }, { signal: request.signal }).then((data) => {
      if (request.signal.aborted) return;
      const checked = validateAdminOrderPage(data, page, 20);
      if (checked.orders.some((order) => order.shopId !== shop.id)) throw new Error("Dữ liệu đơn hàng không khớp cửa hàng.");
      if (page > Math.max(0, checked.totalPages - 1)) { setPage(Math.max(0, checked.totalPages - 1)); return; }
      setResult(checked);
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được đơn hàng cửa hàng.") }); });
    return () => request.abort();
  }, [shop.id, attempt, filters, page]);
  function refresh() { setResult(null); setAttempt((value) => value + 1); }
  function apply(event) {
    event.preventDefault();
    try { validateReportRange(draft.from, draft.to); }
    catch (failure) { toast.error(failure.message); return; }
    setResult(null); setFilters({ ...draft }); setPage(0);
  }
  const orders = result?.orders ?? [];
  const pages = result?.totalPages ?? 0, current = page;
  function close() { if (!selected) onClose(); }
  return <>
    <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-5xl overflow-y-auto rounded-2xl bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50">
      <header className="sticky top-0 z-10 flex items-center justify-between gap-3 border-b bg-card p-5"><div className="min-w-0"><h2 id={titleId} className="break-words text-xl font-bold">Đơn hàng — {shop.name}</h2><p className="mt-1 break-all text-xs text-muted-foreground">{shop.id}</p></div><Button variant="outline" type="button" onClick={close} className="min-h-11 rounded-lg border px-4">Đóng</Button></header>
      <div className="grid gap-4 p-5">
        <form onSubmit={apply} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4"><label className="grid gap-2 text-sm">Từ ngày<Input type="date" required value={draft.from} onChange={(event) => setDraft((value) => ({ ...value, from: event.target.value }))} className="min-h-12 rounded-lg border px-3" /></label><label className="grid gap-2 text-sm">Đến ngày<Input type="date" required value={draft.to} onChange={(event) => setDraft((value) => ({ ...value, to: event.target.value }))} className="min-h-12 rounded-lg border px-3" /></label><label className="grid gap-2 text-sm">Lọc ngày theo<select value={draft.dateBasis} onChange={(event) => setDraft((value) => ({ ...value, dateBasis: event.target.value }))} className="min-h-12 rounded-lg border bg-card px-3"><option value="createdAt">Ngày đặt hàng</option><option value="updatedAt">Ngày cập nhật (đối chiếu doanh thu)</option></select></label><label className="grid gap-2 text-sm">Trạng thái đơn<select value={draft.status} onChange={(event) => setDraft((value) => ({ ...value, status: event.target.value }))} className="min-h-12 rounded-lg border bg-card px-3"><option value="">Tất cả trạng thái</option>{Object.entries(orderStatuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label><Button variant="default" type="submit" className="min-h-11 w-fit rounded-lg bg-primary px-4 font-bold text-white">Áp dụng bộ lọc</Button></form>
        <p className="text-xs leading-6 text-muted-foreground">Đang xem {filters.from} → {filters.to}, theo {filters.dateBasis === "createdAt" ? "ngày đặt" : "ngày cập nhật"}, {orderStatuses[filters.status] || "tất cả trạng thái"}. Múi giờ Việt Nam, bao gồm ngày kết thúc. Bộ lọc dialog độc lập với báo cáo; updatedAt chưa phải ngày giao hàng cố định.</p>
        <div className="flex items-center justify-between gap-3"><p className="text-sm">{result?.totalElements ?? 0} đơn phù hợp (lọc và phân trang server)</p><Button variant="outline" type="button" disabled={!result} onClick={refresh} className="min-h-11 rounded-lg border px-4 text-sm disabled:opacity-50">Tải lại đơn hàng</Button></div>
        {!result ? <p role="status">Đang tải đơn hàng…</p> : result.error ? <div role="alert" className="rounded-xl bg-red-50 p-4 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className="min-h-11 underline">Thử lại</Button></div> : !orders.length ? <p className="py-5 text-sm text-muted-foreground">Không có đơn phù hợp trong khoảng ngày đã chọn.</p> : <div className="overflow-x-auto"><table className="w-full min-w-[850px] text-left text-sm"><caption className="sr-only">Đơn hàng của cửa hàng theo khoảng ngày</caption><thead className="bg-background text-xs text-muted-foreground"><tr>{["Mã đơn / Người nhận", "Ngày đặt / Cập nhật", "Trạng thái", "Tổng tiền", "Thao tác"].map((label) => <th key={label} scope="col" className="p-3">{label}</th>)}</tr></thead><tbody className="divide-y">{orders.map((order) => <tr key={order.id} className="align-top"><td className="max-w-64 p-3"><p className="break-all font-semibold">{order.id}</p><p className="mt-2 whitespace-pre-line break-words text-xs text-muted-foreground">{order.shippingAddress}</p></td><td className="p-3 text-xs"><p>Đặt: {orderDate(order.createdAt)}</p><p className="mt-2">Cập nhật: {orderDate(order.updatedAt)}</p></td><td className="p-3"><p>{orderStatuses[order.orderStatus] ?? order.orderStatus}</p><p className="mt-2 text-xs text-muted-foreground">{order.paymentMethod} · {paymentStatuses[order.paymentStatus] ?? order.paymentStatus}</p></td><td className="whitespace-nowrap p-3 font-semibold">{money.format(order.totalAmount)}</td><td className="p-3"><Button variant="outline" type="button" onClick={() => setSelected(order.id)} className="min-h-11 rounded-lg border px-3">Chi tiết đơn</Button></td></tr>)}</tbody></table></div>}
        {pages > 1 && <nav aria-label="Phân trang đơn hàng của shop" className="flex items-center justify-center gap-3 text-sm"><Button variant="outline" type="button" disabled={!current} onClick={() => { setResult(null); setPage(current - 1); }} className="min-h-11 rounded-lg border px-3 disabled:opacity-50">Trước</Button><span>{current + 1}/{pages}</span><Button variant="outline" type="button" disabled={current + 1 >= pages} onClick={() => { setResult(null); setPage(current + 1); }} className="min-h-11 rounded-lg border px-3 disabled:opacity-50">Sau</Button></nav>}
      </div>
    </dialog>
    {selected && <AdminOrderDialog key={selected} orderId={selected} adminId={adminId} onClose={() => setSelected(null)} onChanged={() => { refresh(); onChanged(); }} />}
  </>;
}

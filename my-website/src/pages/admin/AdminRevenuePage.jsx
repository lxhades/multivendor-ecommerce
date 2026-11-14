import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getAdminShop } from "../../api/shop/adminShopApi.js";
import { getAdminRevenueReport } from "../../api/order/adminOrderApi.js";
import { validateAdminRevenueReport } from "../../components/admin/adminRevenueModel.js";
import { uuidPattern } from "../../components/admin/adminOrderModel.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { toast } from "../../components/common/toast.js";
import { money } from "../../components/product/productHelpers.js";
import { initialReportRange, validateReportRange } from "../../components/seller/sellerReportHelpers.js";
import AdminShopOrdersDialog from "../../components/admin/AdminShopOrdersDialog.jsx";

const adminRevenuePageStyles = {
  container: "grid min-w-0 gap-5",
  header: "flex flex-wrap items-center justify-between gap-3",
  title: "text-2xl font-extrabold tracking-tight",
  button: "min-h-11 rounded-lg border bg-card px-4 disabled:opacity-50",
  description: "rounded-xl border border-primary/25 bg-accent p-4 text-sm leading-7 text-accent-foreground",
  filters: "grid gap-3 rounded-xl border bg-card p-4 sm:grid-cols-2 xl:grid-cols-4",
  label: "grid gap-2 text-sm",
  input: "min-h-12 rounded-lg border px-3",
  label2: "grid min-w-0 gap-2 text-sm",
  input2: "min-h-12 min-w-0 rounded-lg border px-3",
  primaryButton: "min-h-12 self-end rounded-lg bg-primary px-4 font-bold text-white",
  description2: "text-sm text-muted-foreground",
  errorContainer: "rounded-xl bg-red-50 p-4 text-sm text-red-700",
  button2: "min-h-11 underline",
  description3: "text-xs leading-6 text-muted-foreground",
  container2: "grid gap-3 sm:grid-cols-2 xl:grid-cols-3",
  container3: "rounded-xl border bg-card p-4",
  description4: "text-xs text-muted-foreground",
  description5: "mt-2 text-lg font-bold text-primary",
  description6: "mt-2 text-lg font-bold",
  description7: "rounded-xl border bg-card p-6 text-sm",
  container4: "overflow-x-auto rounded-xl border bg-card",
  table: "w-full min-w-[1200px] text-left text-sm",
  caption: "sr-only",
  thead: "bg-background text-xs text-muted-foreground",
  th: "p-4",
  tbody: "divide-y",
  tr: "align-top",
  td: "max-w-60 p-4",
  description8: "break-words font-semibold",
  description9: "mt-2 break-all text-xs text-muted-foreground",
  td2: "whitespace-nowrap p-4",
  button3: "min-h-11 rounded-lg border border-primary/25 px-3 text-primary",
  navigation: "flex items-center justify-center gap-3 text-sm",
  button4: "min-h-11 rounded-lg border bg-card px-3 disabled:opacity-50",
};

export default function AdminRevenuePage() {
  const { auth } = useOutletContext();
  return <Revenue key={auth.user.userId} adminId={auth.user.userId} />;
}
function Revenue({ adminId }) {
  const [filters, setFilters] = useState(() => ({ ...initialReportRange(), shopId: "" }));
  const [draft, setDraft] = useState(filters), [page, setPage] = useState(0);
  const [result, setResult] = useState(null), [attempt, setAttempt] = useState(0);
  const [shops, setShops] = useState({}), [selectedShop, setSelectedShop] = useState(null);
  useEffect(() => {
    const request = new AbortController();
    async function load() {
      try {
        const response = await getAdminRevenueReport({ from: filters.from, to: filters.to, ...(filters.shopId && { shopId: filters.shopId }), page, size: 10 }, { signal: request.signal });
        if (request.signal.aborted) return;
        const data = validateAdminRevenueReport(response, filters, page, 10);
        if (page > Math.max(0, data.totalPages - 1)) { setPage(Math.max(0, data.totalPages - 1)); return; }
        setResult({ data });
        // Only enrich the current page with shop names; revenue never depends on these requests.
        let cursor = 0;
        async function worker() {
          while (cursor < data.items.length && !request.signal.aborted) {
            const id = data.items[cursor++].shopId;
            try {
              const shop = await getAdminShop(id, { signal: request.signal });
              if (request.signal.aborted) return;
              if (shop?.id !== id || !shop.name) throw new Error("Thông tin cửa hàng không hợp lệ.");
              setShops((current) => ({ ...current, [id]: { shop } }));
            } catch (failure) { if (!request.signal.aborted) setShops((current) => ({ ...current, [id]: { error: authErrorMessage(failure, "Không tải được tên cửa hàng.") } })); }
          }
        }
        await Promise.all(Array.from({ length: Math.min(4, data.items.length) }, () => worker()));
      } catch (failure) { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được báo cáo doanh thu.") }); }
    }
    void load(); return () => request.abort();
  }, [filters, page, attempt]);
  function refresh() { setResult(null); setShops({}); setAttempt((value) => value + 1); }
  function apply(event) {
    event.preventDefault();
    try { validateReportRange(draft.from, draft.to); if (draft.shopId.trim() && !uuidPattern.test(draft.shopId.trim())) throw new Error("Mã cửa hàng phải là UUID hợp lệ."); }
    catch (failure) { toast.error(failure.message); return; }
    setResult(null); setShops({}); setPage(0); setFilters({ ...draft, shopId: draft.shopId.trim() });
  }
  function changePage(next) { setResult(null); setShops({}); setPage(next); }
  const data = result?.data;
  return <div className={adminRevenuePageStyles.container}>
    <header className={adminRevenuePageStyles.header}><h2 className={adminRevenuePageStyles.title}>Thống kê đối soát doanh thu</h2><Button variant="outline" type="button" onClick={refresh} disabled={!result || Boolean(selectedShop)} className={adminRevenuePageStyles.button}>Tải lại</Button></header>
    <p className={adminRevenuePageStyles.description}>Server tổng hợp đơn DELIVERED theo updatedAt, múi giờ Việt Nam và bao gồm ngày kết thúc. PAID là trạng thái thanh toán đơn, chưa chứng minh tiền đã về ngân hàng. Chưa có phí nền tảng, hoàn tiền, sổ công nợ hoặc chốt/chuyển tiền cho shop.</p>
    <form onSubmit={apply}><fieldset disabled={Boolean(selectedShop)} className={adminRevenuePageStyles.filters}><label className={adminRevenuePageStyles.label}>Từ ngày<Input type="date" required value={draft.from} onChange={(event) => setDraft((current) => ({ ...current, from: event.target.value }))} className={adminRevenuePageStyles.input} /></label><label className={adminRevenuePageStyles.label}>Đến ngày<Input type="date" required value={draft.to} onChange={(event) => setDraft((current) => ({ ...current, to: event.target.value }))} className={adminRevenuePageStyles.input} /></label><label className={adminRevenuePageStyles.label2}>Mã shop (tùy chọn)<Input value={draft.shopId} maxLength={36} onChange={(event) => setDraft((current) => ({ ...current, shopId: event.target.value }))} placeholder="Để trống: toàn hệ thống" className={adminRevenuePageStyles.input2} /></label><Button variant="default" type="submit" className={adminRevenuePageStyles.primaryButton}>Xem thống kê</Button></fieldset></form>
    <p className={adminRevenuePageStyles.description2}>Kỳ áp dụng: {filters.from} → {filters.to}{filters.shopId ? ` · Shop: ${filters.shopId}` : " · Toàn hệ thống"}</p>
    {!result ? <p role="status">Đang tổng hợp báo cáo trên server…</p> : result.error ? <div role="alert" className={adminRevenuePageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={adminRevenuePageStyles.button2}>Thử lại</Button></div> : <>
      <p className={adminRevenuePageStyles.description3}>Tổng dưới đây tính trên toàn bộ phạm vi lọc, độc lập phân trang. Có {data.totalElements} shop phát sinh đơn DELIVERED trong kỳ; shop không có đơn phù hợp không xuất hiện. Sắp xếp tiền hàng giảm dần.</p>
      <div className={adminRevenuePageStyles.container2}>{[["Tiền hàng đơn đã giao", "revenue"], ["Phí vận chuyển", "shippingFee"], ["Tổng giá trị đơn đã giao", "totalOrderAmount"], ["Giá trị DELIVERED + PAID", "paidOrderAmount"], ["Giá trị DELIVERED chưa PAID", "unpaidOrderAmount"]].map(([label, key]) => <div key={key} className={adminRevenuePageStyles.container3}><p className={adminRevenuePageStyles.description4}>{label}</p><p className={adminRevenuePageStyles.description5}>{money.format(data.summary[key])}</p></div>)}<div className={adminRevenuePageStyles.container3}><p className={adminRevenuePageStyles.description4}>Số đơn đã giao / PAID</p><p className={adminRevenuePageStyles.description6}>{data.summary.deliveredOrders} / {data.summary.paidOrders}</p></div></div>
      {!data.items.length ? <p className={adminRevenuePageStyles.description7}>Không có đơn DELIVERED trong phạm vi đã chọn.</p> : <div className={adminRevenuePageStyles.container4}><table className={adminRevenuePageStyles.table}><caption className={adminRevenuePageStyles.caption}>Doanh thu server tổng hợp theo cửa hàng</caption><thead className={adminRevenuePageStyles.thead}><tr>{["Cửa hàng", "Đơn đã giao / PAID", "Tiền hàng", "Phí vận chuyển", "Tổng giá trị", "DELIVERED + PAID", "DELIVERED chưa PAID", "Thao tác"].map((label) => <th key={label} scope="col" className={adminRevenuePageStyles.th}>{label}</th>)}</tr></thead><tbody className={adminRevenuePageStyles.tbody}>{data.items.map((row) => <tr key={row.shopId} className={adminRevenuePageStyles.tr}><td className={adminRevenuePageStyles.td}><p className={adminRevenuePageStyles.description8}>{shops[row.shopId]?.shop?.name || (shops[row.shopId]?.error ? "Chưa tải được tên shop" : "Đang tải tên shop…")}</p><p className={adminRevenuePageStyles.description9}>{row.shopId}</p></td><td className={adminRevenuePageStyles.th}>{row.summary.deliveredOrders} / {row.summary.paidOrders}</td>{["revenue", "shippingFee", "totalOrderAmount", "paidOrderAmount", "unpaidOrderAmount"].map((key) => <td key={key} className={adminRevenuePageStyles.td2}>{money.format(row.summary[key])}</td>)}<td className={adminRevenuePageStyles.th}><Button variant="outline" type="button" onClick={() => setSelectedShop(shops[row.shopId]?.shop || { id: row.shopId, name: row.shopId })} className={adminRevenuePageStyles.button3}>Xem đơn hàng của shop</Button></td></tr>)}</tbody></table></div>}
      {data.totalPages > 1 && <nav aria-label="Phân trang doanh thu shop" className={adminRevenuePageStyles.navigation}><Button variant="outline" type="button" disabled={!page || Boolean(selectedShop)} onClick={() => changePage(page - 1)} className={adminRevenuePageStyles.button4}>Trang trước</Button><span>{page + 1}/{data.totalPages}</span><Button variant="outline" type="button" disabled={page + 1 >= data.totalPages || Boolean(selectedShop)} onClick={() => changePage(page + 1)} className={adminRevenuePageStyles.button4}>Trang sau</Button></nav>}
    </>}
    {selectedShop && <AdminShopOrdersDialog key={selectedShop.id} shop={selectedShop} initialRange={filters} adminId={adminId} onClose={() => setSelectedShop(null)} onChanged={refresh} />}
  </div>;
}

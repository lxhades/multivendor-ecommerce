import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { getMyShop } from "../../api/shop/sellerShopApi.js";
import { getSellerDemandReport, getSellerSalesReport } from "../../api/order/sellerReportApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { toast } from "../../components/common/toast.js";
import SellerReportResults from "../../components/seller/SellerReportResults.jsx";
import { initialReportRange, periodLabel, reportGroups, validateReportRange, validateSellerReport, vietnamToday } from "../../components/seller/sellerReportHelpers.js";

const sellerReportsPageStyles = {
  formControl: "min-h-11 min-w-0 rounded-lg border border-input bg-card px-3 text-sm",
  reportTypeButton: (active) => `min-h-12 rounded-xl border px-4 text-sm font-semibold ${active ? "border-primary/40 bg-accent text-accent-foreground" : "border-border bg-card text-muted-foreground"}`,
  description: "rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground",
  container: "grid justify-items-start gap-3 rounded-xl border border-primary/25 bg-accent p-5 text-sm",
  link: "inline-flex min-h-11 items-center font-semibold text-primary underline",
  errorContainer: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button: "min-h-11 rounded-lg border border-red-200 px-4",
  container2: "grid min-w-0 gap-5",
  title: "text-2xl font-extrabold tracking-tight",
  description2: "mt-1 break-words text-sm text-muted-foreground",
  container3: "flex flex-wrap gap-2",
  form: "grid gap-4 rounded-xl border border-border bg-card p-4",
  button2: "min-h-11 rounded-lg border border-border px-3 text-xs font-semibold text-muted-foreground hover:border-primary/40",
  container4: "grid gap-3 sm:grid-cols-2 xl:grid-cols-4",
  label: "grid gap-2 text-xs font-semibold",
  primaryButton: "min-h-11 self-end rounded-lg bg-primary px-4 text-sm font-bold text-white",
  description3: "text-xs text-muted-foreground",
  container5: "rounded-xl border border-primary/15 bg-accent p-4 text-xs leading-6 text-accent-foreground",
  container6: "flex flex-wrap items-center justify-between gap-3",
  description4: "text-sm font-semibold text-muted-foreground",
  button3: "min-h-11 rounded-lg border border-border bg-card px-4 text-sm font-semibold disabled:opacity-50",
};

export default function SellerReportsPage() {
  const { auth } = useOutletContext();
  return <SellerReportsShop key={auth.user.userId} sellerId={auth.user.userId} />;
}
function SellerReportsShop({ sellerId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [kind, setKind] = useState("demand");
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
  if (!result) return <p role="status" className={sellerReportsPageStyles.description}>Đang tải cửa hàng…</p>;
  if (result.missingShop) return <div className={sellerReportsPageStyles.container}><p>Bạn chưa có cửa hàng để xem thống kê.</p><Link to="/seller/shop" className={sellerReportsPageStyles.link}>Tạo cửa hàng</Link></div>;
  if (result.error) return <div role="alert" className={sellerReportsPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className={sellerReportsPageStyles.button}>Thử lại</Button></div>;
  return <div className={sellerReportsPageStyles.container2}>
    <div><h2 className={sellerReportsPageStyles.title}>Thống kê cửa hàng</h2><p className={sellerReportsPageStyles.description2}>Báo cáo của {result.shop.name}.</p></div>
    <div role="group" aria-label="Loại báo cáo" className={sellerReportsPageStyles.container3}>{[["demand", "Đặt mua và hủy đơn"], ["sales", "Doanh thu và sản phẩm đã bán"]].map(([value, label]) => <Button variant="outline" key={value} type="button" aria-pressed={kind === value} onClick={() => setKind(value)} className={sellerReportsPageStyles.reportTypeButton(kind === value)}>{label}</Button>)}</div>
    <ReportContent key={kind} kind={kind} shopId={result.shop.id} />
  </div>;
}
function ReportContent({ kind, shopId }) {
  const [filters, setFilters] = useState(() => ({ ...initialReportRange(), groupBy: kind === "demand" ? "DAY" : "MONTH" }));
  const [draft, setDraft] = useState(filters);
  const [variantPage, setVariantPage] = useState(0);
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const request = new AbortController();
    const params = { ...filters, ...(kind === "sales" && { variantPage, variantSize: 20 }) };
    const load = kind === "demand" ? getSellerDemandReport : getSellerSalesReport;
    load(shopId, params, { signal: request.signal }).then((data) => {
      if (request.signal.aborted) return;
      const checked = validateSellerReport(data, shopId, filters, kind, variantPage);
      if (kind === "sales" && variantPage > Math.max(0, checked.totalVariantPages - 1)) { setVariantPage(Math.max(0, checked.totalVariantPages - 1)); return; }
      setResult({ data: checked });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được báo cáo thống kê.") }); });
    return () => request.abort();
  }, [shopId, kind, filters, variantPage, attempt]);
  function apply(event) {
    event.preventDefault();
    try { validateReportRange(draft.from, draft.to); }
    catch (failure) { toast.error(failure.message); return; }
    setResult(null); setVariantPage(0); setFilters({ ...draft });
  }
  function preset(type) {
    const to = vietnamToday();
    let from = to;
    if (type === "week") from = new Date(Date.parse(`${to}T00:00:00Z`) - 6 * 86400000).toISOString().slice(0, 10);
    if (type === "month") from = `${to.slice(0, 7)}-01`;
    if (type === "year") from = `${to.slice(0, 4)}-01-01`;
    setDraft((current) => ({ ...current, from, to }));
  }
  function changeVariantPage(page) { setResult(null); setVariantPage(page); }
  function refresh() { setResult(null); setAttempt((value) => value + 1); }
  const data = result?.data;
  return <div className={sellerReportsPageStyles.container2}>
    <form onSubmit={apply} className={sellerReportsPageStyles.form}>
      <div className={sellerReportsPageStyles.container3}>{[["today", "Hôm nay"], ["week", "7 ngày gần đây"], ["month", "Tháng này"], ["year", "Năm nay"]].map(([value, label]) => <Button variant="outline" key={value} type="button" onClick={() => preset(value)} className={sellerReportsPageStyles.button2}>{label}</Button>)}</div>
      <div className={sellerReportsPageStyles.container4}>
        <label className={sellerReportsPageStyles.label}>Từ ngày<Input type="date" required min="0001-01-01" max="9998-12-31" value={draft.from} onChange={(event) => setDraft((current) => ({ ...current, from: event.target.value }))} className={sellerReportsPageStyles.formControl} /></label>
        <label className={sellerReportsPageStyles.label}>Đến ngày<Input type="date" required min="0001-01-01" max="9998-12-31" value={draft.to} onChange={(event) => setDraft((current) => ({ ...current, to: event.target.value }))} className={sellerReportsPageStyles.formControl} /></label>
        <label className={sellerReportsPageStyles.label}>Nhóm theo<select value={draft.groupBy} onChange={(event) => setDraft((current) => ({ ...current, groupBy: event.target.value }))} className={sellerReportsPageStyles.formControl}>{Object.entries(reportGroups).filter(([value]) => kind === "sales" || value !== "YEAR").map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
        <Button variant="default" type="submit" className={sellerReportsPageStyles.primaryButton}>Xem báo cáo</Button>
      </div>
      <p className={sellerReportsPageStyles.description3}>Chọn nhanh chỉ điền khoảng ngày; bấm Xem báo cáo để áp dụng.</p>
    </form>
    <div className={sellerReportsPageStyles.container5}>{kind === "demand" ? "Tính theo ngày tạo đơn và trạng thái hiện tại: không hủy / đã hủy. Không phải số lượt khách duy nhất hay số thao tác hủy xảy ra trong kỳ. Tỷ lệ hủy tính theo số đơn." : "Chỉ tính đơn DELIVERED, tạm dùng updatedAt để xác định kỳ. Doanh thu tiền hàng là tổng subtotal, chưa gồm phí vận chuyển; đây không phải lợi nhuận."} Múi giờ Việt Nam, bao gồm ngày kết thúc. Tuần bắt đầu thứ Hai; kỳ đầu/cuối chỉ tính phần nằm trong khoảng đã chọn.</div>
    <div className={sellerReportsPageStyles.container6}><p className={sellerReportsPageStyles.description4}>{periodLabel({ periodStart: filters.from, periodEnd: filters.to }, filters)} · Theo {reportGroups[filters.groupBy].toLowerCase()}</p><Button variant="outline" type="button" disabled={!result} onClick={refresh} className={sellerReportsPageStyles.button3}>Tải lại</Button></div>
    {!result ? <p role="status" className={sellerReportsPageStyles.description}>Đang tổng hợp báo cáo…</p> : result.error ? <div role="alert" className={sellerReportsPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={sellerReportsPageStyles.button}>Thử lại</Button></div> : <SellerReportResults report={data} kind={kind} onVariantPage={changeVariantPage} />}
  </div>;
}

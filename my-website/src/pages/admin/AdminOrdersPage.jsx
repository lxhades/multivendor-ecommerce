import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useRef, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getAdminOrderPage } from "../../api/order/adminOrderApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { toast } from "../../components/common/toast.js";
import {
  orderStatuses,
  paymentStatuses,
  orderDate,
} from "../../components/order/orderHelpers.js";
import { money } from "../../components/product/productHelpers.js";
import {
  uuidPattern,
  validateAdminOrderPage,
} from "../../components/admin/adminOrderModel.js";
import AdminOrderDialog from "../../components/admin/AdminOrderDialog.jsx";

/* ------------------------- Icons ------------------------- */
const IconReceipt = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 0 0 2.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 0 0-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 0 0 .75-.75 2.25 2.25 0 0 0-.1-.664m-5.8 0A2.251 2.251 0 0 1 13.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25Z"
    />
  </svg>
);
const IconRefresh = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M16.023 9.348h4.992V4.356M3.02 14.652h4.992v4.992M4.5 9.35a7.5 7.5 0 0 1 12.36-3.536l4.155 4.156M19.5 14.65a7.5 7.5 0 0 1-12.36 3.535l-4.155-4.155"
    />
  </svg>
);
const IconSearch = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"
    />
  </svg>
);
const IconX = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
  </svg>
);
const IconAlert = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M12 9v3.75m9.303 3.376c.866 1.5-.217 3.374-1.948 3.374H4.645c-1.73 0-2.813-1.874-1.948-3.374l7.007-12.13c.866-1.5 3.032-1.5 3.898 0l7.701 13.13ZM12 15.75h.007v.008H12v-.008Z"
    />
  </svg>
);
const IconEmpty = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M20.25 7.5 12 15.75 3.75 7.5m16.5 0v9a1.5 1.5 0 0 1-1.5 1.5H5.25a1.5 1.5 0 0 1-1.5-1.5v-9m16.5 0-8.25 5.25L3.75 7.5"
    />
  </svg>
);
const IconChevronLeft = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
  </svg>
);
const IconChevronRight = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
  </svg>
);
const IconFilter = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v1.044a2.25 2.25 0 0 1-.659 1.591l-5.432 5.432a2.25 2.25 0 0 0-.659 1.591v2.927a2.25 2.25 0 0 1-1.244 2.013L9.75 21v-6.568a2.25 2.25 0 0 0-.659-1.591L3.659 7.409A2.25 2.25 0 0 1 3 5.818V4.774c0-.54.384-1.006.917-1.096A48.32 48.32 0 0 1 12 3Z"
    />
  </svg>
);
const IconHash = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M5.25 8.25h13.5m-13.5 7.5h13.5m-10.5-15-3 22.5m10.5-22.5-3 22.5"
    />
  </svg>
);

/* ------------------------- Helpers ------------------------- */
const MODES = {
  orderId: "Mã đơn hàng",
  checkoutId: "Mã checkout",
  shopId: "Mã cửa hàng",
  buyerId: "Mã người mua",
};
const adminOrdersPageStyles = {
  refreshIcon: (loading) => `size-4 ${loading ? "animate-spin" : ""}`,
  container: "grid min-w-0 gap-6",
  container2: "relative overflow-hidden rounded-3xl border border-border bg-card p-6 sm:p-8",
  container3: "hidden",
  container4: "hidden",
  container5: "relative flex flex-wrap items-start justify-between gap-4",
  container6: "flex items-start gap-4",
  container7: "flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-accent text-primary",
  iconReceipt: "h-5 w-5",
  title: "text-2xl font-extrabold tracking-tight text-foreground sm:text-3xl",
  description: "mt-1 max-w-2xl text-sm leading-7 text-muted-foreground",
  button: "inline-flex min-h-11 items-center gap-2 rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground shadow-sm transition hover:border-input hover:bg-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
  filters: "grid gap-3 rounded-2xl border border-border bg-card p-5 shadow-sm sm:grid-cols-[minmax(0,auto)_minmax(0,1fr)_auto]",
  label: "grid gap-2 text-xs font-semibold text-muted-foreground",
  labelText: "inline-flex items-center gap-1.5",
  iconFilter: "h-3.5 w-3.5 text-muted-foreground",
  select: "min-h-12 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20",
  label2: "grid min-w-0 gap-2 text-xs font-semibold text-muted-foreground",
  input: "min-h-12 min-w-0 rounded-lg border border-input bg-card px-3 font-mono text-sm text-foreground shadow-sm transition placeholder:font-sans placeholder:text-muted-foreground focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20",
  primaryButton: "inline-flex min-h-12 items-center justify-center gap-2 self-end rounded-lg bg-primary px-5 text-sm font-bold text-white shadow-sm transition hover:bg-primary/90 focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2",
  iconSearch: "h-4 w-4",
  filters2: "grid gap-4 rounded-2xl border border-border bg-card p-5 shadow-sm sm:grid-cols-2 xl:grid-cols-3",
  input2: "min-h-12 min-w-0 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition placeholder:text-muted-foreground focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20",
  button2: "inline-flex min-h-11 w-fit items-center gap-2 self-end rounded-lg border border-input bg-card px-4 text-sm font-semibold text-foreground transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-rose-200",
  errorDescription: "inline-flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700",
  iconAlert: "mt-0.5 h-4 w-4 shrink-0",
  description2: "text-xs text-muted-foreground",
  errorDescription2: "text-sm text-red-700",
  container8: "flex items-center gap-3 rounded-2xl border border-border bg-card p-6 text-sm text-muted-foreground shadow-sm",
  labelText2: "inline-block h-4 w-4 animate-spin rounded-full border-2 border-input border-t-orange-500",
  errorContainer: "grid justify-items-start gap-4 rounded-2xl border border-red-200 bg-card from-red-50  p-5 text-sm text-red-700 shadow-sm",
  container9: "flex items-start gap-3",
  errorLabelText: "flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-red-100 text-red-600 ring-1 ring-red-200",
  description3: "leading-6",
  errorButton: "inline-flex min-h-11 items-center gap-2 rounded-lg border border-red-200 bg-card px-4 text-sm font-semibold text-red-700 transition hover:bg-red-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-300",
  container10: "flex flex-wrap items-center gap-2 text-sm text-muted-foreground",
  labelText3: "inline-flex items-center gap-1.5 rounded-full bg-muted px-3 py-1 font-semibold text-foreground",
  iconReceipt2: "h-3.5 w-3.5",
  container11: "grid justify-items-center gap-3 rounded-2xl border border-dashed border-input bg-background/60 p-10 text-center shadow-sm",
  labelText4: "flex h-12 w-12 items-center justify-center rounded-full bg-muted text-muted-foreground ring-1 ring-slate-200",
  iconEmpty: "h-6 w-6",
  description4: "text-sm font-semibold text-foreground",
  description5: "max-w-sm text-xs leading-6 text-muted-foreground",
  container12: "overflow-hidden rounded-2xl border border-border bg-card shadow-sm",
  container13: "overflow-x-auto",
  table: "w-full min-w-[1100px] text-left text-sm",
  caption: "sr-only",
  thead: "bg-background text-xs uppercase tracking-wide text-muted-foreground",
  th: "px-4 py-3 font-semibold",
  tbody: "divide-y divide-border",
  tr: "align-top transition hover:bg-background/70",
  td: "max-w-52 px-4 py-4",
  description6: "break-all font-mono text-xs font-semibold text-foreground",
  description7: "mt-2 text-xs text-muted-foreground",
  description8: "mt-1 text-xs text-muted-foreground",
  td2: "max-w-52 px-4 py-4 text-xs",
  description9: "break-all text-foreground",
  labelText5: "text-muted-foreground",
  description10: "mt-2 break-all text-muted-foreground",
  td3: "max-w-52 whitespace-pre-line break-words px-4 py-4 text-xs leading-6 text-foreground",
  td4: "whitespace-nowrap px-4 py-4",
  description11: "font-bold text-primary",
  td5: "px-4 py-4",
  description12: "inline-flex items-center gap-1.5 rounded-full bg-muted px-2.5 py-1 text-xs font-semibold text-foreground",
  button3: "inline-flex min-h-11 items-center rounded-lg border border-primary/25 bg-accent px-3 text-xs font-bold text-primary transition hover:border-primary/40 hover:bg-accent focus:outline-none focus-visible:ring-2 focus-visible:ring-primary/30",
  navigation: "flex flex-wrap items-center justify-center gap-2 text-sm",
  button4: "inline-flex min-h-11 items-center gap-1 rounded-full border border-border bg-card px-4 font-semibold text-foreground transition hover:border-input hover:bg-background disabled:cursor-not-allowed disabled:opacity-50",
  labelText6: "rounded-full bg-secondary px-4 py-2 text-xs font-bold text-white",
};

export default function AdminOrdersPage() {
  const { auth } = useOutletContext();
  return <AdminOrders key={auth.user.userId} adminId={auth.user.userId} />;
}

function AdminOrders({ adminId }) {
  const lookupForm = useRef(null);
  const [query, setQuery] = useState({});
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [selected, setSelected] = useState(null);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [payment, setPayment] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [sort, setSort] = useState("desc");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  useEffect(() => {
    const request = new AbortController();
    if (from && to && from > to) return () => request.abort();
    const timer = setTimeout(async () => {
      try {
        const data = await getAdminOrderPage({ page, size, direction: sort.toUpperCase(),
          ...(query.id && { [query.mode]: query.id }), ...(keyword.trim() && { q: keyword.trim() }),
          ...(status && { status }), ...(payment && { paymentStatus: payment }),
          ...(from && { from }), ...(to && { to }) }, { signal: request.signal });
        if (request.signal.aborted) return;
        const checked = validateAdminOrderPage(data, page, size);
        const last = Math.max(0, checked.totalPages - 1);
        if (page > last) { setPage(last); return; }
        setResult(checked);
      } catch (failure) { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được đơn hàng.") }); }
    }, 300);
    return () => { clearTimeout(timer); request.abort(); };
  }, [query, attempt, page, size, keyword, status, payment, from, to, sort]);

  function refresh() {
    setResult(null);
    setAttempt((value) => value + 1);
  }

  function submit(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const id = String(form.get("id") ?? "").trim();
    if (id && !uuidPattern.test(id)) {
      toast.error("Nhập mã UUID hợp lệ để tra cứu.");
      return;
    }
    setResult(null); setPage(0);
    setQuery({ mode: String(form.get("mode")), id });
  }

  function resetFilters() {
    lookupForm.current?.reset();
    setQuery({});
    setResult(null);
    setKeyword("");
    setStatus("");
    setPayment("");
    setFrom("");
    setTo("");
    setSort("desc");
    setResult(null); setPage(0);
  }

  /* ---- Derived state ---- */
  const invalidDates = Boolean(from && to && from > to);
  const filtered = result?.orders ?? [];
  const pages = result?.totalPages ?? 0;
  const current = page;
  const paged = filtered;
  const hasFilter = Boolean(query.id || keyword || status || payment || from || to);

  return (
    <div className={adminOrdersPageStyles.container}>
      {/* -------- Hero header -------- */}
      <div className={adminOrdersPageStyles.container2}>
        <div
          aria-hidden
          className={adminOrdersPageStyles.container3}
        />
        <div
          aria-hidden
          className={adminOrdersPageStyles.container4}
        />
        <div className={adminOrdersPageStyles.container5}>
          <div className={adminOrdersPageStyles.container6}>
            <div className={adminOrdersPageStyles.container7}>
              <IconReceipt className={adminOrdersPageStyles.iconReceipt} />
            </div>
            <div>
              <h2 className={adminOrdersPageStyles.title}>
                Quản lý đơn hàng
              </h2>
              <p className={adminOrdersPageStyles.description}>
                Danh sách toàn hệ thống, tìm kiếm, lọc và phân trang tại server.
                Có thể thu hẹp theo mã đơn, checkout, cửa hàng hoặc người mua.
              </p>
            </div>
          </div>
          <Button variant="outline"
            type="button"
            disabled={!result || Boolean(selected)}
            onClick={refresh}
            className={adminOrdersPageStyles.button}
          >
            <IconRefresh
              className={adminOrdersPageStyles.refreshIcon(!result)}
            />
            Tải lại
          </Button>
        </div>
      </div>

      {/* -------- Lookup form -------- */}
      <form ref={lookupForm} onSubmit={submit}>
        <fieldset
          disabled={Boolean(selected)}
          className={adminOrdersPageStyles.filters}
        >
          <label className={adminOrdersPageStyles.label}>
            <span className={adminOrdersPageStyles.labelText}>
              <IconFilter className={adminOrdersPageStyles.iconFilter} />
              Tra cứu theo
            </span>
            <select
              name="mode"
              className={adminOrdersPageStyles.select}
            >
              {Object.entries(MODES).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </label>

          <label className={adminOrdersPageStyles.label2}>
            <span className={adminOrdersPageStyles.labelText}>
              <IconHash className={adminOrdersPageStyles.iconFilter} />
              Mã UUID
            </span>
            <Input
              name="id"
              maxLength={36}
              placeholder="Để trống để xem toàn hệ thống"
              className={adminOrdersPageStyles.input}
            />
          </label>

          <Button variant="default"
            type="submit"
            className={adminOrdersPageStyles.primaryButton}
          >
            <IconSearch className={adminOrdersPageStyles.iconSearch} />
            Tra cứu
          </Button>
        </fieldset>
      </form>

      {/* -------- Filters -------- */}
      <fieldset
        disabled={Boolean(selected)}
        className={adminOrdersPageStyles.filters2}
      >
        <label className={adminOrdersPageStyles.label}>
          <span className={adminOrdersPageStyles.labelText}>
            <IconSearch className={adminOrdersPageStyles.iconFilter} />
            Tìm trên toàn bộ đơn hàng
          </span>
          <Input
            type="search"
            maxLength={150}
            value={keyword}
            onChange={(event) => {
              setKeyword(event.target.value);
              setResult(null); setPage(0);
            }}
            placeholder="Mã, người nhận, sản phẩm…"
            className={adminOrdersPageStyles.input2}
          />
        </label>

        <label className={adminOrdersPageStyles.label}>
          <span className={adminOrdersPageStyles.labelText}>
            <IconFilter className={adminOrdersPageStyles.iconFilter} />
            Trạng thái đơn
          </span>
          <select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value);
              setResult(null); setPage(0);
            }}
            className={adminOrdersPageStyles.select}
          >
            <option value="">Tất cả</option>
            {Object.entries(orderStatuses).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>

        <label className={adminOrdersPageStyles.label}>
          <span className={adminOrdersPageStyles.labelText}>
            <IconFilter className={adminOrdersPageStyles.iconFilter} />
            Thanh toán
          </span>
          <select
            value={payment}
            onChange={(event) => {
              setPayment(event.target.value);
              setResult(null); setPage(0);
            }}
            className={adminOrdersPageStyles.select}
          >
            <option value="">Tất cả</option>
            {Object.entries(paymentStatuses).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>

        <label className={adminOrdersPageStyles.label}>
          Từ ngày đặt
          <Input
            type="date"
            value={from}
            onChange={(event) => {
              setFrom(event.target.value);
              setResult(null); setPage(0);
            }}
            className={adminOrdersPageStyles.select}
          />
        </label>

        <label className={adminOrdersPageStyles.label}>
          Đến ngày đặt
          <Input
            type="date"
            value={to}
            onChange={(event) => {
              setTo(event.target.value);
              setResult(null); setPage(0);
            }}
            className={adminOrdersPageStyles.select}
          />
        </label>

        <label className={adminOrdersPageStyles.label}>
          Sắp xếp ngày đặt
          <select
            value={sort}
            onChange={(event) => {
              setSort(event.target.value);
              setResult(null); setPage(0);
            }}
            className={adminOrdersPageStyles.select}
          >
            <option value="desc">Mới nhất</option>
            <option value="asc">Cũ nhất</option>
          </select>
        </label>

        <label className={adminOrdersPageStyles.label}>
          Mỗi trang
          <select
            value={size}
            onChange={(event) => {
              setSize(Number(event.target.value));
              setResult(null); setPage(0);
            }}
            className={adminOrdersPageStyles.select}
          >
            {[10, 20, 50].map((value) => (
              <option key={value}>{value}</option>
            ))}
          </select>
        </label>

        {hasFilter && (
          <Button variant="outline"
            type="button"
            onClick={resetFilters}
            className={adminOrdersPageStyles.button2}
          >
            <IconX className={adminOrdersPageStyles.iconSearch} />
            Bỏ bộ lọc
          </Button>
        )}
      </fieldset>

      {invalidDates && (
        <p
          role="alert"
          className={adminOrdersPageStyles.errorDescription}
        >
          <IconAlert className={adminOrdersPageStyles.iconAlert} />
          Ngày bắt đầu không được sau ngày kết thúc.
        </p>
      )}
      <p className={adminOrdersPageStyles.description2}>
        Ngày đặt theo múi giờ Việt Nam, bao gồm cả ngày kết thúc.
      </p>

      {/* -------- Results -------- */}
      {invalidDates ? <p role="alert" className={adminOrdersPageStyles.errorDescription2}>Chọn lại khoảng ngày hợp lệ để xem đơn hàng.</p> : !result ? (
        <div
          role="status"
          className={adminOrdersPageStyles.container8}
        >
          <span className={adminOrdersPageStyles.labelText2} />
          Đang tải đơn hàng…
        </div>
      ) : result.error ? (
        <div
          role="alert"
          className={adminOrdersPageStyles.errorContainer}
        >
          <div className={adminOrdersPageStyles.container9}>
            <span className={adminOrdersPageStyles.errorLabelText}>
              <IconAlert className={adminOrdersPageStyles.iconReceipt} />
            </span>
            <p className={adminOrdersPageStyles.description3}>{result.error}</p>
          </div>
          <Button variant="outline"
            type="button"
            onClick={refresh}
            className={adminOrdersPageStyles.errorButton}
          >
            <IconRefresh className={adminOrdersPageStyles.iconSearch} />
            Thử lại
          </Button>
        </div>
      ) : (
        <>
          {/* Counter */}
          <div className={adminOrdersPageStyles.container10}>
            <span className={adminOrdersPageStyles.labelText3}>
              <IconReceipt className={adminOrdersPageStyles.iconReceipt2} />
              {filtered.length}/{result.totalElements} đơn
            </span>
            <span>khớp bộ lọc trên toàn hệ thống.</span>
          </div>

          {/* Table / empty */}
          {!filtered.length ? (
            <div className={adminOrdersPageStyles.container11}>
              <span className={adminOrdersPageStyles.labelText4}>
                <IconEmpty className={adminOrdersPageStyles.iconEmpty} />
              </span>
              <p className={adminOrdersPageStyles.description4}>
                Không có đơn hàng phù hợp
              </p>
              <p className={adminOrdersPageStyles.description5}>
                Thử đổi từ khóa hoặc xóa bộ lọc để xem danh sách đơn trên hệ thống.
              </p>
            </div>
          ) : (
            <div className={adminOrdersPageStyles.container12}>
              <div className={adminOrdersPageStyles.container13}>
                <table className={adminOrdersPageStyles.table}>
                  <caption className={adminOrdersPageStyles.caption}>
                    Kết quả tra cứu đơn hàng Admin
                  </caption>
                  <thead className={adminOrdersPageStyles.thead}>
                    <tr>
                      {[
                        "Mã đơn / Ngày đặt",
                        "Người mua / Cửa hàng",
                        "Người nhận",
                        "Tổng tiền",
                        "Trạng thái",
                        "Thao tác",
                      ].map((label) => (
                        <th key={label} scope="col" className={adminOrdersPageStyles.th}>
                          {label}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className={adminOrdersPageStyles.tbody}>
                    {paged.map((order) => (
                      <tr
                        key={order.id}
                        className={adminOrdersPageStyles.tr}
                      >
                        <td className={adminOrdersPageStyles.td}>
                          <p className={adminOrdersPageStyles.description6}>
                            {order.id}
                          </p>
                          <p className={adminOrdersPageStyles.description7}>
                            {orderDate(order.createdAt)}
                          </p>
                          <p className={adminOrdersPageStyles.description8}>
                            Cập nhật: {orderDate(order.updatedAt)}
                          </p>
                        </td>
                        <td className={adminOrdersPageStyles.td2}>
                          <p className={adminOrdersPageStyles.description9}>
                            <span className={adminOrdersPageStyles.labelText5}>Người mua:</span>{" "}
                            {order.buyerId}
                          </p>
                          <p className={adminOrdersPageStyles.description10}>
                            <span className={adminOrdersPageStyles.labelText5}>Shop:</span>{" "}
                            {order.shopId}
                          </p>
                        </td>
                        <td className={adminOrdersPageStyles.td3}>
                          {order.shippingAddress || "Chưa có thông tin"}
                        </td>
                        <td className={adminOrdersPageStyles.td4}>
                          <p className={adminOrdersPageStyles.description11}>
                            {money.format(order.totalAmount)}
                          </p>
                          <p className={adminOrdersPageStyles.description7}>
                            {order.paymentMethod}
                          </p>
                        </td>
                        <td className={adminOrdersPageStyles.td5}>
                          <p className={adminOrdersPageStyles.description12}>
                            {orderStatuses[order.orderStatus] ?? order.orderStatus}
                          </p>
                          <p className={adminOrdersPageStyles.description7}>
                            {paymentStatuses[order.paymentStatus] ?? order.paymentStatus}
                          </p>
                        </td>
                        <td className={adminOrdersPageStyles.td5}>
                          <Button variant="outline"
                            type="button"
                            onClick={() => setSelected(order.id)}
                            className={adminOrdersPageStyles.button3}
                          >
                            Chi tiết
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Pagination */}
          {pages > 1 && (
            <nav
              aria-label="Phân trang đơn hàng Admin"
              className={adminOrdersPageStyles.navigation}
            >
              <Button variant="outline"
                type="button"
                disabled={current === 0 || Boolean(selected)}
                onClick={() => { setResult(null); setPage(current - 1); }}
                className={adminOrdersPageStyles.button4}
              >
                <IconChevronLeft className={adminOrdersPageStyles.iconSearch} />
                Trang trước
              </Button>
              <span className={adminOrdersPageStyles.labelText6}>
                {current + 1}/{pages}
              </span>
              <Button variant="outline"
                type="button"
                disabled={current + 1 >= pages || Boolean(selected)}
                onClick={() => { setResult(null); setPage(current + 1); }}
                className={adminOrdersPageStyles.button4}
              >
                Trang sau
                <IconChevronRight className={adminOrdersPageStyles.iconSearch} />
              </Button>
            </nav>
          )}
        </>
      )}

      {/* -------- Dialog -------- */}
      {selected && (
        <AdminOrderDialog
          key={selected}
          orderId={selected}
          adminId={adminId}
          onClose={() => setSelected(null)}
          onChanged={refresh}
        />
      )}
    </div>
  );
}
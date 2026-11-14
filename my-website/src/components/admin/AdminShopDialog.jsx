import { Button } from "@/components/ui/button";
import { useEffect, useId, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { getAdminShop, updateAdminShopStatus } from "../../api/shop/adminShopApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { orderDate } from "../order/orderHelpers.js";
import ShopHeader from "../shop/ShopHeader.jsx";
import { adminShopStatuses, validateAdminShop } from "./adminShopModel.js";

export default function AdminShopDialog({ shopId, adminId, desiredStatus, onClose, onChanged }) {
  const dialog = useRef(null);
  const titleId = useId();
  const confirmId = useId();
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [target, setTarget] = useState(desiredStatus ?? null);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const lock = useRef(false);
  const mutation = useRef(null);
  useEffect(() => {
    const node = dialog.current; node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); mutation.current?.abort(); };
  }, []);
  useEffect(() => {
    const request = new AbortController();
    getAdminShop(shopId, { signal: request.signal }).then((shop) => {
      if (!request.signal.aborted) setResult({ shop: validateAdminShop(shop, shopId) });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được cửa hàng.") }); });
    return () => request.abort();
  }, [shopId, attempt]);
  function close() { if (!lock.current) onClose(); }
  function refresh() { if (lock.current) return; setResult(null); setAttempt((value) => value + 1); }
  function confirm(event) {
    event.preventDefault();
    const next = selectedStatus ?? result?.shop?.status;
    if (!Object.hasOwn(adminShopStatuses, next)) { toast.error("Chọn trạng thái hợp lệ."); return; }
    if (next === result.shop.status) { toast.error("Trạng thái chưa thay đổi."); return; }
    setError(""); setTarget(next);
  }
  async function execute() {
    if (lock.current || !result?.shop || !Object.hasOwn(adminShopStatuses, target) || getSession()?.userId !== adminId) return;
    lock.current = true; setPending(true); setError("");
    const request = new AbortController(); mutation.current = request;
    try {
      const response = await updateAdminShopStatus(shopId, target, { signal: request.signal });
      if (request.signal.aborted || getSession()?.userId !== adminId) return;
      const shop = validateAdminShop(response, shopId);
      if (shop.status !== target) throw new Error("Chưa xác minh được trạng thái cửa hàng mới.");
      setResult({ shop }); setSelectedStatus(shop.status); setTarget(null);
      onChanged(); toast.success(`Đã chuyển cửa hàng sang trạng thái ${adminShopStatuses[shop.status].toLowerCase()}.`);
    } catch (failure) {
      if (!request.signal.aborted && getSession()?.userId === adminId) {
        const message = authErrorMessage(failure, "Không thay đổi được trạng thái cửa hàng.");
        setError(message); toast.error(message); onChanged();
        setResult(null); setAttempt((value) => value + 1);
      }
    } finally { lock.current = false; if (!request.signal.aborted) setPending(false); mutation.current = null; }
  }
  const shop = result?.shop;
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-4xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50"><div className="sticky top-0 z-10 flex items-center justify-between border-b border-border/60 bg-card px-5 py-3"><h2 id={titleId} className="text-xl font-bold">Quản lý cửa hàng</h2><Button variant="outline" type="button" disabled={pending} onClick={close} aria-label="Đóng quản lý cửa hàng" className="min-h-11 min-w-11 text-2xl">×</Button></div>
    <div className="grid gap-5 p-5 sm:p-7" aria-busy={pending}>{!result ? <p role="status">Đang tải cửa hàng…</p> : result.error ? <div role="alert" className="grid gap-3 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className="min-h-11 rounded-lg border px-4">Thử lại</Button></div> : <>
      {target && <section role="alertdialog" aria-modal="true" aria-labelledby={confirmId} className="grid gap-3 rounded-xl border border-primary/25 bg-accent p-4"><h3 id={confirmId} className="font-bold">Xác nhận chuyển sang {adminShopStatuses[target]?.toLowerCase()}?</h3><p className="break-words text-sm leading-7">Cửa hàng: {shop.name}. Trạng thái hiện tại: {adminShopStatuses[shop.status]}. Shop không hoạt động sẽ không có sản phẩm trong kết quả tìm kiếm công khai.</p><p className="text-xs leading-6 text-muted-foreground">Thao tác này đổi trạng thái shop, không xóa cửa hàng, tài khoản seller hoặc lịch sử đơn hàng.</p>{error && <p role="alert" className="text-sm text-red-700">{error}</p>}<div className="flex flex-wrap gap-3"><Button variant="outline" autoFocus type="button" disabled={pending} onClick={() => setTarget(null)} className="min-h-11 rounded-lg border bg-card px-4 text-sm">Hủy</Button><Button variant="destructive" type="button" disabled={pending} onClick={() => void execute()} className={`min-h-11 rounded-lg px-4 text-sm font-bold text-white disabled:opacity-50 ${target === "BANNED" ? "bg-red-600" : "bg-primary"}`}>{pending ? "Đang cập nhật…" : "Xác nhận"}</Button></div></section>}
      <div hidden={Boolean(target)} className="space-y-5">
        <ShopHeader key={`${shop.id}:${shop.updatedAt}`} shop={shop} />
        <dl className="grid gap-4 rounded-xl bg-background p-4 text-sm sm:grid-cols-2">{[["ID cửa hàng", shop.id], ["ID người bán", shop.sellerId], ["Slug", shop.slug], ["Cập nhật gần nhất", orderDate(shop.updatedAt)]].map(([label, value]) => <div key={label}><dt className="text-xs text-muted-foreground">{label}</dt><dd className="mt-1 break-all font-medium">{value || "Chưa cập nhật"}</dd></div>)}<div className="sm:col-span-2"><dt className="text-xs text-muted-foreground">Thông tin ngân hàng</dt><dd className="mt-1 whitespace-pre-line break-words">{shop.bankDetails || "Chưa cập nhật"}</dd></div></dl>
        <form onSubmit={confirm} className="grid gap-3 rounded-xl border border-border p-4"><label className="grid gap-2 text-sm font-semibold">Trạng thái cửa hàng<select value={selectedStatus ?? shop.status} onChange={(event) => setSelectedStatus(event.target.value)} className="min-h-12 rounded-lg border border-input bg-card px-3">{Object.entries(adminShopStatuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label><Button variant="default" type="submit" className="min-h-11 w-fit rounded-lg bg-primary px-4 text-sm font-bold text-white">Cập nhật trạng thái</Button></form>
        <div className="flex flex-wrap gap-3"><Link to={`/shops/${encodeURIComponent(shop.id)}`} className="inline-flex min-h-11 items-center rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary">Xem trang shop</Link><Button variant="outline" type="button" onClick={refresh} className="min-h-11 rounded-lg border border-border px-4 text-sm">Tải lại chi tiết</Button></div>
      </div>
    </>}</div>
  </dialog>;
}

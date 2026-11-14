import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { useEffect, useId, useRef, useState } from "react";
import { getAdminProduct, approveProduct, rejectProduct } from "../../api/product/adminProductApi.js";
import { getAdminShop } from "../../api/shop/adminShopApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { moderationLabels, validateModerationProduct } from "./adminProductModel.js";
import AdminModerationDetails from "./AdminModerationDetails.jsx";

export default function AdminProductModerationDialog({ productId, adminId, categories, onClose, onChanged }) {
  const dialog = useRef(null), mutation = useRef(null), lock = useRef(false);
  const titleId = useId();
  const [result, setResult] = useState(null), [attempt, setAttempt] = useState(0);
  const [shopResult, setShopResult] = useState(null), [shopAttempt, setShopAttempt] = useState(0);
  const [decision, setDecision] = useState("APPROVED"), [reason, setReason] = useState("");
  const [action, setAction] = useState(null), [pending, setPending] = useState(false), [error, setError] = useState("");
  useEffect(() => {
    const node = dialog.current; node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); mutation.current?.abort(); };
  }, []);
  useEffect(() => {
    const request = new AbortController();
    getAdminProduct(productId, { signal: request.signal }).then((product) => {
      if (!request.signal.aborted) setResult({ product: validateModerationProduct(product, productId) });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được sản phẩm.") }); });
    return () => request.abort();
  }, [productId, attempt]);
  const shopId = result?.product?.shopId;
  useEffect(() => {
    if (!shopId) return;
    const request = new AbortController();
    getAdminShop(shopId, { signal: request.signal }).then((shop) => {
      if (request.signal.aborted) return;
      if (shop?.id !== shopId || !shop.name) throw new Error("Thông tin cửa hàng không hợp lệ.");
      setShopResult({ shop });
    }).catch((failure) => { if (!request.signal.aborted) setShopResult({ error: authErrorMessage(failure, "Không tải được tên cửa hàng.") }); });
    return () => request.abort();
  }, [shopId, shopAttempt]);
  function close() { if (!lock.current) onClose(); }
  function refresh() { if (lock.current) return; setAction(null); setResult(null); setAttempt((value) => value + 1); }
  function confirm(event) {
    event.preventDefault();
    if (decision === "REJECTED" && !reason.trim()) { toast.error("Nhập lý do từ chối."); return; }
    setError(""); setAction({ decision, reason: reason.trim() });
  }
  async function execute() {
    if (lock.current || !action || result?.product?.approvalStatus !== "PENDING" || result.product.deletedAt || getSession()?.userId !== adminId) return;
    lock.current = true; setPending(true); setError("");
    const request = new AbortController(); mutation.current = request;
    try {
      const api = action.decision === "APPROVED" ? approveProduct : rejectProduct;
      const response = await api(productId, { reason: action.reason }, { signal: request.signal });
      if (request.signal.aborted || getSession()?.userId !== adminId) return;
      const product = validateModerationProduct(response, productId);
      if (product.approvalStatus !== action.decision) throw new Error("Chưa xác minh được kết quả duyệt.");
      setResult({ product }); setAction(null); onChanged();
      toast.success(action.decision === "APPROVED" ? "Đã duyệt sản phẩm." : "Đã từ chối sản phẩm.");
    } catch (failure) {
      if (!request.signal.aborted && getSession()?.userId === adminId) {
        const message = authErrorMessage(failure, "Không thực hiện được xét duyệt. Hãy tải lại trạng thái trước khi thử lại.");
        setError(message); toast.error(message); setAction(null);
        setResult(null); setAttempt((value) => value + 1); onChanged();
      }
    } finally { lock.current = false; if (!request.signal.aborted) setPending(false); }
  }
  const product = result?.product;
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%-2rem)] max-w-4xl overflow-y-auto rounded-2xl bg-card p-0 text-foreground shadow-xl backdrop:bg-secondary/50">
    <div className="p-5 sm:p-7"><header className="mb-5 flex items-center justify-between gap-3"><h2 id={titleId} className="text-xl font-bold">{action ? "Xác nhận xét duyệt" : "Chi tiết và duyệt sản phẩm"}</h2><Button variant="outline" type="button" onClick={close} disabled={pending} aria-label="Đóng chi tiết" className="min-h-11 rounded-lg border px-4 disabled:opacity-50">Đóng</Button></header>
      {error && <p role="alert" className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
      {action ? <section className="grid gap-4"><p>Bạn muốn <strong>{action.decision === "APPROVED" ? "duyệt" : "từ chối"}</strong> sản phẩm <strong>{product.name}</strong> của {shopResult?.shop?.name || product.shopId}?</p><p className="whitespace-pre-line break-words text-sm text-muted-foreground">{action.reason || "Không có ghi chú."}</p><div className="flex gap-3"><Button variant="outline" type="button" disabled={pending} onClick={() => setAction(null)} className="min-h-11 rounded-lg border px-4 disabled:opacity-50">Quay lại</Button><Button variant="default" type="button" disabled={pending} onClick={execute} className="min-h-11 rounded-lg bg-primary px-4 font-semibold text-white disabled:opacity-50">{pending ? "Đang xử lý…" : "Xác nhận"}</Button></div></section> : <div>
        {!result ? <p role="status">Đang tải chi tiết sản phẩm…</p> : result.error ? <div role="alert"><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className="min-h-11 underline">Thử lại</Button></div> : <>
          <AdminModerationDetails key={`${product.id}:${product.updatedAt}`} product={product} shop={shopResult?.shop} shopError={shopResult?.error} retryShop={() => { setShopResult(null); setShopAttempt((value) => value + 1); }} categoryName={categories.find((category) => category.id === product.categoryId)?.name} />
          {product.approvalStatus === "PENDING" && !product.deletedAt ? <form onSubmit={confirm} className="mt-6 grid gap-4 border-t pt-5"><h3 className="font-bold">Kết quả xét duyệt</h3><label className="grid gap-2 text-sm">Quyết định<select value={decision} onChange={(event) => setDecision(event.target.value)} className="min-h-12 rounded-lg border px-3"><option value="APPROVED">Duyệt sản phẩm</option><option value="REJECTED">Từ chối</option></select></label><label className="grid gap-2 text-sm">{decision === "REJECTED" ? "Lý do từ chối (bắt buộc)" : "Ghi chú (tùy chọn)"}<Textarea value={reason} onChange={(event) => setReason(event.target.value)} required={decision === "REJECTED"} maxLength={2000} rows={3} className="rounded-lg border p-3" /></label><p className="text-xs text-muted-foreground">Duyệt sản phẩm chưa đảm bảo hiển thị công khai nếu cửa hàng hoặc trạng thái bán chưa đủ điều kiện.</p><Button variant="default" type="submit" className="min-h-11 w-fit rounded-lg bg-primary px-4 font-bold text-white">Tiếp tục xác nhận</Button></form> : <p role="status" className="mt-5 rounded-lg bg-muted p-4 text-sm">{product.deletedAt ? "Sản phẩm đã xóa, không thể xét duyệt." : `Sản phẩm ${moderationLabels[product.approvalStatus].toLowerCase()}.`}</p>}
          <Button variant="outline" type="button" onClick={refresh} className="mt-4 min-h-11 rounded-lg border px-4 text-sm">Tải lại chi tiết</Button>
        </>}
      </div>}
    </div>
  </dialog>;
}

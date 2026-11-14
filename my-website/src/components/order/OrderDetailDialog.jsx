import { Button } from "@/components/ui/button";
import { useEffect, useId, useRef, useState } from "react";
import { cancelBuyerOrder, getBuyerOrder } from "../../api/order/orderApi.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { isMediaUrl, money } from "../product/productHelpers.js";
import { canCancelOrder, orderDate, orderStatuses, paymentStatuses, validateBuyerOrder } from "./orderHelpers.js";

import OrderReviews from "./OrderReviews.jsx";

export default function OrderDetailDialog({ orderId, buyerId, onClose, onChanged }) {
  const dialog = useRef(null);
  const titleId = useId();
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [confirming, setConfirming] = useState(false);
  const [pending, setPending] = useState(false);
  const [reviewPending, setReviewPending] = useState(false);
  const [reviewed, setReviewed] = useState({});
  const reviewLock = useRef(false);
  const lock = useRef(false);
  const mutation = useRef(null);
  useEffect(() => {
    const node = dialog.current;
    node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); mutation.current?.abort(); };
  }, []);
  useEffect(() => {
    const request = new AbortController();
    getBuyerOrder(orderId, { signal: request.signal }).then((order) => {
      if (!request.signal.aborted) setResult({ order: validateBuyerOrder(order, buyerId, orderId) });
    }).catch((failure) => {
      if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được chi tiết đơn hàng.") });
    });
    return () => request.abort();
  }, [orderId, buyerId, attempt]);

  function close() { if (!lock.current && !reviewLock.current) onClose(); }
  function setReviewBusy(value) { reviewLock.current = value; setReviewPending(value); }
  function refresh() { if (reviewLock.current) return; setConfirming(false); setResult(null); setAttempt((value) => value + 1); }
  async function cancel() {
    if (lock.current || !result?.order || !canCancelOrder(result.order)) return;
    lock.current = true; setPending(true);
    const request = new AbortController(); mutation.current = request;
    try {
      const order = validateBuyerOrder(await cancelBuyerOrder(orderId, { signal: request.signal }), buyerId, orderId);
      if (request.signal.aborted) return;
      if (order.orderStatus !== "CANCELLED") throw new Error("Chưa xác nhận được kết quả hủy đơn. Vui lòng tải lại để kiểm tra.");
      setResult({ order }); setConfirming(false);
      onChanged(order);
      toast.success("Hủy đơn hàng thành công.");
    } catch (failure) {
      if (!request.signal.aborted) {
        toast.error(authErrorMessage(failure, "Không hủy được đơn hàng. Vui lòng kiểm tra lại trạng thái."));
        refresh(); onChanged();
      }
    } finally {
      lock.current = false;
      if (!request.signal.aborted) setPending(false);
      mutation.current = null;
    }
  }
  const order = result?.order;
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-3xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50">
    <div className="sticky top-0 z-10 flex items-center justify-between border-b border-border/60 bg-card px-5 py-3">
      <h2 id={titleId} className="text-xl font-bold">Chi tiết đơn hàng</h2>
      <Button variant="outline" type="button" onClick={close} disabled={pending || reviewPending} aria-label="Đóng chi tiết đơn hàng" className="min-h-11 min-w-11 rounded-lg text-2xl disabled:opacity-50">×</Button>
    </div>
    <div className="grid gap-5 p-5 sm:p-7" aria-busy={pending || reviewPending}>
      {!result ? <p role="status" className="text-sm text-muted-foreground">Đang tải chi tiết đơn hàng…</p> : result.error ? <div role="alert" className="grid gap-3 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className="min-h-11 rounded-lg border px-4">Thử lại</Button></div> : <>
        <div className="grid gap-2 text-sm">
          <p className="break-all"><strong>Mã đơn:</strong> {order.id}</p>
          <p><strong>Trạng thái:</strong> {orderStatuses[order.orderStatus] ?? order.orderStatus}</p>
          <p><strong>Ngày đặt:</strong> {orderDate(order.createdAt)}</p>
          <p><strong>Cập nhật:</strong> {orderDate(order.updatedAt)}</p>
          <p><strong>Thanh toán:</strong> {order.paymentMethod === "COD" ? "Thanh toán khi nhận hàng (COD)" : order.paymentMethod} · {paymentStatuses[order.paymentStatus] ?? order.paymentStatus}</p>
        </div>
        <section><h3 className="mb-2 font-bold">Thông tin nhận hàng</h3><p className="whitespace-pre-line break-words text-sm leading-7 text-muted-foreground">{order.shippingAddress || "Chưa có thông tin nhận hàng"}</p></section>
        <section><h3 className="font-bold">Sản phẩm</h3><ul className="divide-y divide-border">
          {order.items.map((item, index) => <li key={item.id ?? index} className="flex gap-3 py-4">
            {isMediaUrl(item.variantImageUrl) && <img src={item.variantImageUrl} alt={item.productName || "Sản phẩm"} className="size-16 shrink-0 rounded-lg border border-border/60 object-cover" loading="lazy" />}
            <div className="min-w-0 flex-1"><p className="break-words text-sm font-semibold">{item.productName || "Sản phẩm"}</p><p className="mt-1 break-words text-xs text-muted-foreground">{item.variantName}</p><p className="mt-2 text-sm text-muted-foreground">{money.format(item.unitPrice)} × {item.quantity}</p><p className="mt-1 text-sm font-bold">{money.format(item.unitPrice * item.quantity)}</p></div>
          </li>)}
        </ul></section>
        <dl className="grid gap-3 rounded-xl bg-background p-4 text-sm">
          <div className="flex justify-between gap-3"><dt>Tiền hàng</dt><dd>{money.format(order.subtotal)}</dd></div>
          <div className="flex justify-between gap-3"><dt>Phí vận chuyển</dt><dd>{money.format(order.shippingFee)}</dd></div>
          <div className="flex justify-between gap-3 border-t border-border pt-3 font-bold"><dt>Tổng thanh toán</dt><dd className="text-lg text-primary">{money.format(order.totalAmount)}</dd></div>
        </dl>
        {canCancelOrder(order) ? confirming ? <div className="grid gap-3 rounded-xl border border-red-200 bg-red-50 p-4">
          <p className="text-sm text-red-800">Bạn có chắc muốn hủy đơn hàng này?</p>
          <div className="flex flex-wrap gap-3"><Button variant="destructive" type="button" disabled={pending || reviewPending} onClick={() => void cancel()} className="min-h-11 rounded-lg bg-red-600 px-4 text-sm font-bold text-white disabled:opacity-50">{pending ? "Đang hủy…" : "Xác nhận hủy đơn"}</Button><Button variant="outline" type="button" disabled={pending || reviewPending} onClick={() => setConfirming(false)} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Giữ đơn hàng</Button></div>
        </div> : <Button variant="outline" type="button" onClick={() => setConfirming(true)} className="min-h-11 rounded-lg border border-red-200 px-4 text-sm font-semibold text-red-700">Hủy đơn hàng</Button> : <p className="text-xs leading-6 text-muted-foreground">{order.orderStatus === "CANCELLED" ? "Đơn hàng đã được hủy." : "Chỉ có thể hủy đơn COD đã xác nhận và chưa thanh toán."}</p>}
        <OrderReviews order={order} reviewed={reviewed} pending={reviewPending} onBusyChange={setReviewBusy} onComplete={(productId, outcome) => setReviewed((current) => ({ ...current, [productId]: outcome }))} />
        <Button variant="outline" type="button" disabled={pending || reviewPending} onClick={refresh} className="min-h-11 rounded-lg border border-border px-4 text-sm font-semibold disabled:opacity-50">Cập nhật trạng thái</Button>
      </>}
    </div>
  </dialog>;
}

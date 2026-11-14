import { Button } from "@/components/ui/button";
import { useEffect, useId, useRef, useState } from "react";
import { getSellerOrder, shipSellerOrder, cancelSellerOrder } from "../../api/order/sellerOrderApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { isMediaUrl, money } from "../product/productHelpers.js";
import { canCancelOrder, orderDate, orderStatuses, paymentStatuses } from "../order/orderHelpers.js";
import { validateSellerOrder } from "./sellerOrderHelpers.js";

export default function SellerOrderDialog({ orderId, shopId, sellerId, onClose, onChanged }) {
  const dialog = useRef(null);
  const titleId = useId();
  const confirmId = useId();
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [action, setAction] = useState(null);
  const [pending, setPending] = useState(false);
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
    getSellerOrder(orderId, { signal: request.signal }).then((order) => {
      if (!request.signal.aborted) setResult({ order: validateSellerOrder(order, shopId, orderId) });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được chi tiết đơn hàng.") }); });
    return () => request.abort();
  }, [orderId, shopId, attempt]);
  function close() { if (!lock.current) { if (action) setAction(null); else onClose(); } }
  function refresh() { if (lock.current) return; setAction(null); setResult(null); setAttempt((value) => value + 1); }
  async function execute() {
    const current = result?.order;
    if (lock.current || !action || !current || getSession()?.userId !== sellerId) return;
    if ((action === "ship" && current.orderStatus !== "CONFIRMED") || (action === "cancel" && !canCancelOrder(current))) return;
    lock.current = true; setPending(true);
    const request = new AbortController(); mutation.current = request;
    try {
      const response = action === "ship" ? await shipSellerOrder(orderId, { signal: request.signal }) : await cancelSellerOrder(orderId, { signal: request.signal });
      if (request.signal.aborted || getSession()?.userId !== sellerId) return;
      const order = validateSellerOrder(response, shopId, orderId);
      if ((action === "cancel" && order.orderStatus !== "CANCELLED") || (action === "ship" && !["SHIPPED", "DELIVERED"].includes(order.orderStatus))) throw new Error("Chưa xác minh được kết quả thao tác. Vui lòng kiểm tra lại trạng thái đơn hàng.");
      setResult({ order }); setAction(null);
      onChanged();
      toast.success(action === "ship" ? "Đã gửi đơn hàng cho bên vận chuyển." : "Hủy đơn hàng thành công.");
    } catch (failure) {
      if (!request.signal.aborted && getSession()?.userId === sellerId) {
        toast.error(authErrorMessage(failure, "Không cập nhật được đơn hàng. Vui lòng kiểm tra lại trạng thái."));
        setAction(null); setResult(null); setAttempt((value) => value + 1); onChanged();
      }
    } finally {
      lock.current = false;
      if (!request.signal.aborted) setPending(false);
      mutation.current = null;
    }
  }
  const order = result?.order;
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-3xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50">
    <div className="sticky top-0 z-10 flex items-center justify-between gap-3 border-b border-border/60 bg-card px-5 py-3"><h2 id={titleId} className="text-xl font-bold">Chi tiết đơn hàng</h2><Button variant="outline" type="button" disabled={pending} onClick={close} aria-label="Đóng chi tiết đơn hàng" className="min-h-11 min-w-11 rounded-lg text-2xl disabled:opacity-50">×</Button></div>
    <div className="grid gap-5 p-5 sm:p-7" aria-busy={pending}>
      {action && <section role="alertdialog" aria-modal="true" aria-labelledby={confirmId} className="grid gap-4 rounded-xl border border-primary/25 bg-accent p-4">
        <h3 id={confirmId} className="font-bold">{action === "ship" ? "Giao đơn cho vận chuyển?" : "Hủy đơn hàng?"}</h3><p className="text-sm leading-7">{action === "ship" ? "Xác nhận đơn đã sẵn sàng giao cho bên vận chuyển. Trạng thái đơn sẽ chuyển sang đang giao." : "Bạn có chắc muốn hủy đơn hàng này? Backend sẽ kiểm tra lại điều kiện hủy trước khi lưu."}</p>
        <div className="flex flex-wrap gap-3"><Button variant="outline" autoFocus type="button" disabled={pending} onClick={() => setAction(null)} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Quay lại</Button><Button variant="default" type="button" disabled={pending} onClick={() => void execute()} className={`min-h-11 rounded-lg px-4 text-sm font-bold text-white disabled:opacity-50 ${action === "ship" ? "bg-primary" : "bg-red-600"}`}>{pending ? "Đang xử lý…" : "Xác nhận"}</Button></div>
      </section>}
      <div hidden={Boolean(action)}>
        {!result ? <p role="status" className="text-sm text-muted-foreground">Đang tải đơn hàng…</p> : result.error ? <div role="alert" className="grid gap-3 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className="min-h-11 rounded-lg border px-4">Thử lại</Button></div> : <div className="grid gap-5">
          <dl className="grid gap-2 text-sm"><div><dt className="font-semibold">Mã đơn</dt><dd className="break-all text-muted-foreground">{order.id}</dd></div><div><dt className="font-semibold">Mã checkout</dt><dd className="break-all text-muted-foreground">{order.checkoutId || "Chưa cập nhật"}</dd></div><div><dt className="font-semibold">Mã người mua</dt><dd className="break-all text-muted-foreground">{order.buyerId}</dd></div></dl>
          <div className="grid gap-2 text-sm"><p><strong>Trạng thái:</strong> {orderStatuses[order.orderStatus] ?? order.orderStatus}</p><p><strong>Ngày đặt:</strong> {orderDate(order.createdAt)}</p><p><strong>Cập nhật:</strong> {orderDate(order.updatedAt)}</p><p><strong>Thanh toán:</strong> {order.paymentMethod === "COD" ? "Thanh toán khi nhận hàng (COD)" : order.paymentMethod} · {paymentStatuses[order.paymentStatus] ?? order.paymentStatus}</p></div>
          <section><h3 className="mb-2 font-bold">Thông tin người nhận</h3><p className="whitespace-pre-line break-words text-sm leading-7 text-muted-foreground">{order.shippingAddress || "Chưa có thông tin"}</p></section>
          <section><h3 className="font-bold">Sản phẩm</h3><ul className="divide-y divide-border">{order.items.map((item, index) => <li key={item.id ?? index} className="flex gap-3 py-4">{isMediaUrl(item.variantImageUrl) && <img src={item.variantImageUrl} alt={item.productName || "Sản phẩm"} loading="lazy" className="size-16 shrink-0 rounded-lg border border-border/60 object-contain" />}<div className="min-w-0 flex-1"><p className="break-words text-sm font-semibold">{item.productName}</p><p className="mt-1 break-words text-xs text-muted-foreground">{item.variantName}</p><p className="mt-2 text-sm text-muted-foreground">{money.format(item.unitPrice)} × {item.quantity}</p><p className="mt-1 text-sm font-bold">{money.format(item.unitPrice * item.quantity)}</p></div></li>)}</ul></section>
          <dl className="grid gap-3 rounded-xl bg-background p-4 text-sm"><div className="flex justify-between gap-3"><dt>Tiền hàng</dt><dd>{money.format(order.subtotal)}</dd></div><div className="flex justify-between gap-3"><dt>Phí vận chuyển</dt><dd>{money.format(order.shippingFee)}</dd></div><div className="flex justify-between gap-3 border-t border-border pt-3 font-bold"><dt>Tổng tiền</dt><dd className="text-lg text-primary">{money.format(order.totalAmount)}</dd></div></dl>
          <div className="flex flex-wrap gap-3">{order.orderStatus === "CONFIRMED" && <Button variant="default" type="button" onClick={() => setAction("ship")} className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">Giao cho vận chuyển</Button>}{canCancelOrder(order) && <Button variant="outline" type="button" onClick={() => setAction("cancel")} className="min-h-11 rounded-lg border border-red-200 px-4 text-sm font-semibold text-red-700">Hủy đơn hàng</Button>}<Button variant="outline" type="button" onClick={refresh} className="min-h-11 rounded-lg border border-border px-4 text-sm font-semibold">Cập nhật trạng thái</Button></div>
          {order.orderStatus !== "CONFIRMED" && <p className="text-xs leading-6 text-muted-foreground">Giao hàng chỉ áp dụng cho đơn đã xác nhận; hủy áp dụng cho đơn COD đã xác nhận, chưa thanh toán. Trạng thái giao thành công được cập nhật bởi hệ thống vận chuyển.</p>}
        </div>}
      </div>
    </div>
  </dialog>;
}

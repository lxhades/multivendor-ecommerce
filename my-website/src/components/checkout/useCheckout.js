import { useEffect, useEffectEvent, useRef, useState } from "react";
import { getSession } from "../../api/auth/authSession.js";
import { createCheckout, getCheckout } from "../../api/order/checkoutApi.js";
import { getBuyerOrdersByCheckout } from "../../api/order/orderApi.js";
import { toast } from "../common/toast.js";
import { readCheckoutIntent, saveCheckoutIntent, clearCheckoutIntent, validateCheckout, validateCheckoutOrders, isTerminal } from "./checkoutModel.js";

export function useCheckout(buyerId, cart, onSuccess) {
  const [recovery, setRecovery] = useState(() => readCheckoutIntent(buyerId));
  const intentRef = useRef(recovery.intent);
  const [active, setActive] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const [verified, setVerified] = useState(null);
  const [pollAttempt, setPollAttempt] = useState(0);
  const controller = useRef(null);
  const lock = useRef(false);
  const finished = useRef(null);
  const storageWarning = useRef(false);
  const intent = active ? recovery.intent : null;
  const checkoutId = intent?.checkout?.id;
  const intentKey = intent?.key;

  useEffect(() => () => controller.current?.abort(), []);

  function remember(next) { intentRef.current = next; setRecovery({ intent: next, error: "" }); }
  const acceptStatus = useEffectEvent((checkout, orders) => {
    const current = intentRef.current;
    if (!current || checkout.idempotencyKey !== current.key) return;
    const next = { ...current, checkout, ...(orders && { orders }) };
    try { saveCheckoutIntent(next); }
    catch { if (!storageWarning.current) { toast.error("Chưa lưu được kết quả trong tab. Hãy giữ cửa sổ này để kiểm tra đơn."); storageWarning.current = true; } }
    remember(next); setVerified(checkout); setError("");
    if (isTerminal(checkout.status) && finished.current !== current.key) {
      finished.current = current.key;
      if (checkout.status === "COMPLETED") {
        toast.success("Đặt hàng thành công. Bạn thanh toán khi nhận hàng.");
        try { cart.removePurchased(current.lines); }
        catch { toast.error("Đơn đã đặt thành công nhưng chưa cập nhật được giỏ. Vui lòng kiểm tra lại giỏ hàng."); }
        try { clearCheckoutIntent(buyerId); }
        catch { toast.error("Chưa xóa được dữ liệu checkout đã hoàn tất trong tab."); }
        onSuccess?.(checkout, next.orders ?? []);
      } else toast.error(checkout.status === "FAILED" ? "Đặt hàng không thành công. Giỏ hàng được giữ nguyên." : "Yêu cầu đặt hàng đã hết hạn. Giỏ hàng được giữ nguyên.");
    }
  });

  useEffect(() => {
    if (!checkoutId || !intentKey) return;
    const request = new AbortController();
    let timer;
    let ordersLoaded = false;
    async function poll() {
      try {
        const checkout = await getCheckout(checkoutId, { signal: request.signal });
        if (request.signal.aborted || getSession()?.userId !== buyerId) return;
        validateCheckout(checkout, { buyerId, key: intentKey });
        let orders;
        if (!ordersLoaded || isTerminal(checkout.status)) {
          orders = validateCheckoutOrders(await getBuyerOrdersByCheckout(checkoutId, { signal: request.signal }), checkout);
          ordersLoaded = true;
        }
        if (request.signal.aborted || getSession()?.userId !== buyerId) return;
        acceptStatus(checkout, orders);
        if (!isTerminal(checkout.status)) timer = setTimeout(poll, 2500);
      } catch (failure) {
        if (!request.signal.aborted && getSession()?.userId === buyerId) {
          const message = failure.response?.data?.message || "Chưa kiểm tra được kết quả đặt hàng. Vui lòng kiểm tra lại, không tạo yêu cầu mới.";
          setError(message); toast.error(message);
        }
      }
    }
    void poll();
    return () => { request.abort(); clearTimeout(timer); };
  }, [buyerId, checkoutId, intentKey, pollAttempt]);

  async function send(next) {
    if (lock.current || getSession()?.userId !== buyerId) return;
    if (recovery.error || (intentRef.current && intentRef.current.key !== next.key)) return;
    lock.current = true; setSending(true); setError("");
    const request = new AbortController(); controller.current = request;
    let dispatched = false;
    try {
      // Persist the exact payload/key before dispatch. Never regenerate on retry.
      saveCheckoutIntent(next); remember(next); setActive(true);
      dispatched = true;
      const result = await createCheckout(next.input, next.key, { signal: request.signal });
      if (request.signal.aborted || getSession()?.userId !== buyerId) return;
      const checkout = validateCheckout(result?.checkout, next);
      const orders = validateCheckoutOrders(result?.orders, checkout);
      const accepted = { ...next, checkout, orders };
      remember(accepted);
      try { saveCheckoutIntent(accepted); }
      catch { toast.error("Đã gửi đơn nhưng chưa lưu được kết quả. Hãy giữ cửa sổ này để kiểm tra."); }
      // Only the verified GET status may complete the cart; POST acceptance is not payment success.
    } catch (failure) {
      if (request.signal.aborted || getSession()?.userId !== buyerId) return;
      let message = failure.response?.data?.message || (dispatched ? "Chưa xác nhận được yêu cầu. Gửi lại cùng mã yêu cầu để kiểm tra, tránh trùng đơn." : "Không lưu được yêu cầu đặt hàng. Vui lòng cho phép sessionStorage rồi thử lại.");
      // This backend rejects invalid commands before persisting a checkout (HTTP 400).
      if (dispatched && failure.response?.status === 400) {
        try { clearCheckoutIntent(buyerId); remember(null); }
        catch { message += " Chưa xóa được dữ liệu yêu cầu cũ trong tab."; }
      }
      setError(message); toast.error(message);
    } finally { lock.current = false; if (!request.signal.aborted) setSending(false); controller.current = null; }
  }

  function finish() {
    if (!verified || !isTerminal(verified.status)) return false;
    try { clearCheckoutIntent(buyerId); remember(null); return true; }
    catch { toast.error("Chưa xóa được yêu cầu đã hoàn tất khỏi tab. Vui lòng thử lại."); return false; }
  }
  return { intent, pendingRecovery: !active && Boolean(recovery.intent),
    resume: () => setActive(true), recoveryError: recovery.error, sending, error, verified, send, finish,
    retryPoll: () => { setError(""); setPollAttempt((value) => value + 1); },
    retryRecovery: () => { const next = readCheckoutIntent(buyerId); intentRef.current = next.intent; setRecovery(next); },
  };
}

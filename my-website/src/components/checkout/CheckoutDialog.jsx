import { Button } from "@/components/ui/button";
import { useEffect, useId, useRef, useState } from "react";
import { useCartAvailability } from "../cart/useCartAvailability.js";
import { toast, registerToastHost } from "../common/toast.js";
import { useCheckout } from "./useCheckout.js";
import { checkoutLabels, isTerminal, makeCheckoutIntent, shippingAddressFromForm } from "./checkoutModel.js";
import CheckoutAddressFields from "./CheckoutAddressFields";
import CheckoutOrderSummary from "./CheckoutOrderSummary";

export default function CheckoutDialog({ buyerId, cart, onClose, onSuccess }) {
  // --- KHU VỰC HOOKS & REFS ---
  const dialog = useRef(null);
  const titleId = useId();
  const [addressReady, setAddressReady] = useState(false);
  
  const checkout = useCheckout(buyerId, cart, onSuccess);
  
  // Kiểm tra tồn kho (nếu đã có intent thì không cần gọi lại API kiểm tra)
  const { checks, loading, refresh } = useCartAvailability(checkout.intent ? [] : cart.items);
  
  // Danh sách sản phẩm hiển thị: Ưu tiên lấy từ Intent (nếu có), nếu không thì lấy từ Giỏ hàng
  const lines = checkout.intent?.lines ?? cart.items.map((line) => ({ 
    ...line, 
    ...checks[line.variantId]?.snapshot, 
    quantity: line.quantity, 
    revision: line.revision 
  }));
  
  // Lọc ra các sản phẩm có lỗi hoặc không đủ tồn kho
  const issues = cart.items.filter((line) => 
    checks[line.variantId]?.error || 
    (checks[line.variantId] && checks[line.variantId].availableQuantity < line.quantity)
  );
  
  // Xác định xem quá trình checkout đã kết thúc (Thành công/Thất bại) chưa
  const terminal = checkout.verified && isTerminal(checkout.verified.status);

  // --- EFFECTS ---
  useEffect(() => {
    const node = dialog.current;
    if (!node.open) node.showModal();
    const releaseToastHost = registerToastHost(node);
    return () => {
      releaseToastHost();
      node.close();
    };
  }, []);

  // --- HÀM XỬ LÝ ---
  async function confirm(event) {
    event.preventDefault();
    if (checkout.sending || checkout.intent || checkout.pendingRecovery || !cart.ready || !addressReady || loading || issues.length) return;
    
    try {
      const shippingAddress = shippingAddressFromForm(new FormData(event.currentTarget));
      
      // Tạo snapshot giỏ hàng mới nhất
      const snapshot = cart.checkoutSnapshot().map((line) => {
        const checked = checks[line.variantId];
        if (!checked?.snapshot || checked.availableQuantity < line.quantity) {
          throw new Error("Giỏ hàng đã thay đổi. Vui lòng kiểm tra lại sản phẩm và tồn kho.");
        }
        return { ...line, ...checked.snapshot, quantity: line.quantity, revision: line.revision };
      });
      
      await checkout.send(makeCheckoutIntent(buyerId, snapshot, shippingAddress));
    } catch (error) {
      toast.error(error.message);
    }
  }

  function close() {
    if (!checkout.sending) onClose();
  }

  // --- RENDER LOGIC (Tách khỏi JSX để tránh Ternary lồng nhau) ---
  let content;

  if (checkout.recoveryError) {
    content = (
      <div role="alert" className="grid gap-3 text-sm text-red-700">
        <p>{checkout.recoveryError}</p>
        <Button variant="outline" 
          type="button" 
          className="min-h-11 rounded-lg border border-red-200 px-3" 
          onClick={checkout.retryRecovery}
        >
          Đọc lại yêu cầu
        </Button>
      </div>
    );
  } else if (checkout.intent) {
    content = (
      <>
        <section 
          aria-live="polite" 
          className={`grid gap-2 rounded-xl p-4 ${
            checkout.verified?.status === "COMPLETED" ? "bg-green-50 text-green-900" : "bg-accent text-accent-foreground"
          }`}
        >
          <h3 className="font-bold">
            {checkout.sending 
              ? "Đang gửi yêu cầu đặt hàng…" 
              : checkout.verified 
                ? checkoutLabels[checkout.verified.status] 
                : checkout.intent.checkout 
                  ? "Đang kiểm tra kết quả đặt hàng…" 
                  : "Yêu cầu đặt hàng cần được kiểm tra lại"}
          </h3>
          
          {checkout.verified?.status === "COMPLETED" && (
            <p className="text-sm">Đơn hàng đã được xác nhận. Bạn thanh toán khi nhận hàng (COD).</p>
          )}
          {checkout.verified && ["FAILED", "EXPIRED"].includes(checkout.verified.status) && (
            <p className="text-sm">Giỏ hàng được giữ nguyên. Bạn có thể kiểm tra lại trước khi đặt đơn mới.</p>
          )}
          {checkout.intent.checkout && (
            <p className="text-xs break-all">Mã đặt hàng: {checkout.intent.checkout.id}</p>
          )}
        </section>

        <div>
          <h3 className="mb-2 font-bold">Thông tin người nhận</h3>
          <p className="text-sm leading-7 whitespace-pre-line break-words text-muted-foreground">
            {checkout.intent.input.shippingAddress}
          </p>
        </div>

        <CheckoutOrderSummary lines={lines} orders={checkout.intent.orders} />
        
        <p className="text-sm">
          Phương thức thanh toán: <strong>Thanh toán khi nhận hàng (COD)</strong>
        </p>

        {checkout.error && (
          <p role="alert" className="text-sm text-red-700">{checkout.error}</p>
        )}

        {/* Nút gửi lại yêu cầu (nếu chưa tạo được đơn) */}
        {!checkout.intent.checkout && !checkout.sending && (
          <>
            <p className="text-xs leading-6 text-muted-foreground">
              Gửi lại đúng yêu cầu đã lưu để tránh tạo đơn trùng. Không thay đổi mặt hàng hoặc địa chỉ của yêu cầu đang chờ xác nhận.
            </p>
            <Button variant="default" 
              type="button" 
              onClick={() => void checkout.send(checkout.intent)} 
              className="min-h-12 rounded-lg bg-primary px-4 text-sm font-bold text-white"
            >
              Gửi lại yêu cầu đặt hàng
            </Button>
          </>
        )}

        {/* Nút kiểm tra lại kết quả (nếu có lỗi mạng khi polling) */}
        {checkout.intent.checkout && checkout.error && (
          <Button variant="outline" 
            type="button" 
            onClick={checkout.retryPoll} 
            className="min-h-11 rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary"
          >
            Kiểm tra lại kết quả
          </Button>
        )}

        {/* Nút Hoàn tất / Quay lại (khi đã có kết quả cuối cùng) */}
        {terminal && (
          <Button variant="default" 
            type="button" 
            onClick={() => { if (checkout.finish()) onClose(); }} 
            className="min-h-12 rounded-lg bg-primary px-4 text-sm font-bold text-white"
          >
            {checkout.verified.status === "COMPLETED" ? "Hoàn tất" : "Quay lại giỏ hàng"}
          </Button>
        )}
      </>
    );
  } else if (cart.items.length === 0) {
    content = (
      <p className="text-sm text-muted-foreground">
        Giỏ hàng đang trống. Hãy thêm sản phẩm trước khi đặt hàng.
      </p>
    );
  } else {
    // TRẠNG THÁI MẶC ĐỊNH: HIỂN THỊ FORM ĐIỀN THÔNG TIN
    content = (
      <form onSubmit={confirm} className="grid gap-5" aria-busy={checkout.sending}>
        <CheckoutAddressFields buyerId={buyerId} onReady={setAddressReady} />
        {checkout.pendingRecovery && (
          <div role="alert" className="grid gap-2 rounded-lg bg-amber-50 p-4 text-sm text-amber-900">
            <p>Bạn có yêu cầu đặt hàng trước đó chưa được kiểm tra. Hãy kiểm tra kết quả trước khi xác nhận đơn mới để tránh trùng đơn.</p>
            <Button variant="outline" type="button" onClick={checkout.resume} className="min-h-11 rounded-lg border border-amber-300 px-3 font-semibold">Kiểm tra yêu cầu trước đó</Button>
          </div>
        )}
        
        <CheckoutOrderSummary lines={lines} />

        <fieldset className="grid gap-3 border-t border-border/60 pt-5">
          <legend className="mb-3 font-bold">Phương thức thanh toán</legend>
          
          <label className="flex min-h-12 items-center gap-3 rounded-lg border border-primary/40 bg-accent px-4 text-sm font-semibold">
            <input type="radio" name="paymentMethod" value="COD" defaultChecked className="size-4 accent-primary" />
            Thanh toán khi nhận hàng (COD)
          </label>
          
          <label className="flex min-h-12 items-center gap-3 rounded-lg border border-border bg-background px-4 text-sm text-muted-foreground">
            <input type="radio" name="paymentMethod" value="VNPAY" disabled className="size-4 accent-primary" />
            VNPay — Chưa hỗ trợ
          </label>
        </fieldset>

        {loading && (
          <p role="status" className="text-sm text-muted-foreground">Đang xác nhận giá và tồn kho…</p>
        )}

        {issues.length > 0 && (
          <div role="alert" className="text-sm text-red-700">
            <p>Một số mặt hàng không còn bán hoặc không đủ tồn kho. Vui lòng điều chỉnh giỏ.</p>
            <Button variant="outline" 
              type="button" 
              onClick={refresh} 
              className="min-h-11 rounded px-2 underline"
            >
              Kiểm tra lại
            </Button>
          </div>
        )}

        {checkout.error && (
          <p role="alert" className="text-sm text-red-700">{checkout.error}</p>
        )}

        <Button variant="default" 
          type="submit" 
          disabled={checkout.sending || checkout.pendingRecovery || !addressReady || loading || issues.length > 0 || !cart.ready} 
          className="min-h-12 rounded-lg bg-primary px-5 text-sm font-bold text-white hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Xác nhận đặt hàng
        </Button>
      </form>
    );
  }

  // --- RENDER CHÍNH ---
  return (
    <dialog 
      ref={dialog} 
      aria-labelledby={titleId} 
      onCancel={(event) => { event.preventDefault(); close(); }} 
      onClick={(event) => { if (event.target === event.currentTarget) close(); }}
      className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-3xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50"
    >
      {/* HEADER CỦA DIALOG */}
      <div className="sticky top-0 z-10 flex items-center justify-between gap-3 border-b border-border/60 bg-card px-5 py-3 sm:px-7">
        <h2 id={titleId} className="text-xl font-bold">Thanh toán COD</h2>
        <Button variant="outline" 
          type="button" 
          onClick={close} 
          disabled={checkout.sending} 
          aria-label="Đóng thanh toán" 
          className="min-h-11 min-w-11 rounded-lg text-2xl text-muted-foreground hover:bg-background disabled:opacity-40"
        >
          ×
        </Button>
      </div>

      {/* BODY CỦA DIALOG (Chứa nội dung động) */}
      <div className="grid gap-5 p-5 sm:p-7">
        {content}
      </div>
    </dialog>
  );
}
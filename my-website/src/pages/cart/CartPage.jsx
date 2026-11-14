import { Button } from "@/components/ui/button";
import { useState } from "react";
import { Navigate, useNavigate, useSearchParams, Link, useOutletContext } from "react-router-dom";
import CheckoutDialog from "../../components/checkout/CheckoutDialog";
import { readCheckoutIntent } from "../../components/checkout/checkoutModel.js";
import { toast } from "../../components/common/toast.js";
import CartItem from "../../components/cart/CartItem";
import { useCartAvailability } from "../../components/cart/useCartAvailability.js";
import { money } from "../../components/product/productHelpers.js";
import ConfirmDialog from "../../components/common/ConfirmDialog"; // Dùng cho việc xóa toàn bộ giỏ

const cartPageStyles = {
  page: "mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:px-6 lg:px-8",
  container: "flex flex-wrap items-center justify-between gap-3 rounded-xl border border-primary/25 bg-accent p-4 text-sm",
  button: "min-h-11 rounded-lg px-3 font-semibold text-accent-foreground underline",
  container2: "flex flex-wrap items-center justify-between gap-3",
  heading: "text-3xl font-extrabold tracking-tight text-foreground sm:text-4xl",
  description: "mt-2 text-sm text-muted-foreground",
  container3: "flex flex-wrap items-center gap-2",
  link: "inline-flex min-h-11 items-center rounded-lg px-3 text-sm font-semibold text-primary hover:bg-accent",
  labelText: "ml-2",
  description2: "text-sm text-muted-foreground",
  description3: "rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900",
  description4: "rounded-2xl border border-border bg-card p-6 text-sm text-muted-foreground",
  section: "grid justify-items-center gap-4 rounded-2xl border border-border bg-card px-5 py-12 text-center",
  labelText2: "text-4xl",
  title: "text-lg font-bold text-foreground",
  link2: "inline-flex min-h-12 items-center rounded-lg bg-primary px-5 text-sm font-bold text-white hover:bg-primary/90",
  container4: "grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_20rem]",
  container5: "grid min-w-0 gap-3",
  container6: "flex flex-wrap items-center justify-between gap-2",
  description5: "text-xs text-muted-foreground",
  button2: "min-h-11 rounded-lg border border-input px-3 text-sm font-semibold disabled:opacity-50",
  ul: "grid gap-3",
  li: "min-w-0",
  aside: "grid gap-5 border-t-4 border-t-secondary rounded-2xl border border-border bg-card p-5 lg:sticky lg:top-24",
  container7: "flex flex-wrap justify-between gap-2 border-y border-border/60 py-4 text-sm",
  labelText3: "text-muted-foreground",
  strong: "text-xl text-primary",
  description6: "text-xs leading-6 text-muted-foreground",
  errorDescription: "rounded-lg bg-red-50 p-3 text-sm text-red-700",
  primaryButton: "min-h-12 rounded-lg bg-primary px-4 text-sm font-bold text-white hover:bg-primary/90 disabled:opacity-50",
  errorButton: "min-h-11 rounded-lg border border-red-200 px-3 text-sm font-semibold text-red-700 hover:bg-red-50",
};

export default function CartPage() {
  const { cart, auth } = useOutletContext();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  
  const checkoutOpen = params.get("checkout") === "1";
  const [canResume, setCanResume] = useState(() => {
    if (!auth.user) return false;
    const saved = readCheckoutIntent(auth.user.userId);
    return Boolean(saved.intent || saved.error);
  });

  // Quản lý Dialog xóa toàn bộ
  const [isClearCartOpen, setIsClearCartOpen] = useState(false);

  function openCheckout() {
    if (!auth.user) {
      toast.error("Vui lòng đăng nhập để đặt hàng. Giỏ khách và giỏ tài khoản được lưu riêng.");
      navigate("/login", { state: { returnToCheckout: true } });
      return;
    }
    setParams((current) => {
      const next = new URLSearchParams(current);
      next.set("checkout", "1");
      return next;
    });
  }

  function closeCheckout() {
    const saved = auth.user ? readCheckoutIntent(auth.user.userId) : {};
    setCanResume(Boolean(saved.intent || saved.error));
    setParams((current) => {
      const next = new URLSearchParams(current);
      next.delete("checkout");
      return next;
    }, { replace: true });
  }

  function handleCheckoutSuccess(checkout, orders) {
    navigate("/orders", {
      replace: true,
      state: { checkoutId: checkout.id, orderIds: orders.map((order) => order.id) },
    });
  }

  function handleClearCart() {
    cart.clear();
    setIsClearCartOpen(false);
    toast.success("Đã xóa toàn bộ sản phẩm khỏi giỏ hàng.");
  }

  const { checks, loading, refresh, updateCheck } = useCartAvailability(cart.items);

  const subtotal = cart.items.reduce((sum, item) => {
    const price = checks[item.variantId]?.snapshot?.unitPrice ?? item.unitPrice;
    return sum + price * item.quantity;
  }, 0);

  const issues = cart.items.filter((item) => {
    const check = checks[item.variantId];
    return check?.error || (check && check.availableQuantity < item.quantity);
  }).length;

  const checkoutDisabled = !cart.ready || cart.items.length === 0 || loading || issues > 0;
  const checkoutHint = !cart.ready
    ? "Vui lòng xác minh phiên tài khoản trước khi thanh toán."
    : cart.items.length === 0
    ? "Thêm sản phẩm vào giỏ để thanh toán."
    : loading
    ? "Đang kiểm tra giá và tồn kho trước khi thanh toán."
    : issues > 0
    ? "Vui lòng xử lý các mặt hàng lỗi hoặc không đủ tồn kho trước khi thanh toán."
    : "";

  if (checkoutOpen && !auth.checking && !auth.sessionError && !auth.user) {
    return <Navigate to="/login" state={{ returnToCheckout: true }} replace />;
  }

  return (
    <div className={cartPageStyles.page}>
      
      {/* DIALOG THANH TOÁN */}
      {checkoutOpen && auth.user && (
        <CheckoutDialog
          key={auth.user.userId}
          buyerId={auth.user.userId}
          cart={cart}
          onClose={closeCheckout}
          onSuccess={handleCheckoutSuccess} // Truyền hàm xử lý thành công
        />
      )}

      {canResume && !checkoutOpen && (
        <div className={cartPageStyles.container}>
          <p>Bạn có yêu cầu đặt hàng đã lưu trong tab này.</p>
          <Button variant="outline" type="button" onClick={openCheckout} className={cartPageStyles.button}>
            Kiểm tra yêu cầu đặt hàng
          </Button>
        </div>
      )}

      {/* HEADER */}
      <div className={cartPageStyles.container2}>
        <div>
          <h1 className={cartPageStyles.heading}>Giỏ hàng</h1>
          {cart.ready && (
            <p className={cartPageStyles.description}>
              {cart.items.length} phân loại · {cart.totalQuantity.toLocaleString("vi-VN")} sản phẩm
            </p>
          )}
        </div>
        <div className={cartPageStyles.container3}>
          <Link to="/" className={cartPageStyles.link}>
            Tiếp tục mua sắm <span aria-hidden="true" className={cartPageStyles.labelText}>→</span>
          </Link>
        </div>
      </div>

      {checkoutHint && <p id="cart-checkout-help" role="status" className={cartPageStyles.description2}>{checkoutHint}</p>}

      {cart.warning && (
        <p role="alert" className={cartPageStyles.description3}>
          {cart.warning}
        </p>
      )}

      {/* TRẠNG THÁI: ĐANG TẢI HOẶC GIỎ TRỐNG */}
      {!cart.ready && (
        <p role="status" className={cartPageStyles.description4}>
          Vui lòng xác minh phiên tài khoản để xem giỏ hàng.
        </p>
      )}

      {cart.ready && cart.items.length === 0 && (
        <section className={cartPageStyles.section}>
          <span aria-hidden="true" className={cartPageStyles.labelText2}>🛒</span>
          <h2 className={cartPageStyles.title}>Giỏ hàng của bạn đang trống</h2>
          <p className={cartPageStyles.description2}>Khám phá sản phẩm và thêm phân loại bạn yêu thích vào giỏ.</p>
          <Link to="/" className={cartPageStyles.link2}>
            Khám phá sản phẩm
          </Link>
        </section>
      )}

      {/* TRẠNG THÁI: CÓ SẢN PHẨM */}
      {cart.ready && cart.items.length > 0 && (
        <div className={cartPageStyles.container4}>
          
          <div className={cartPageStyles.container5}>
            <div className={cartPageStyles.container6}>
              <p role="status" className={cartPageStyles.description5}>
                {loading ? "Đang cập nhật giá và tồn kho…" : issues > 0 ? "Một số mặt hàng cần được kiểm tra." : "Đã kiểm tra thông tin sản phẩm."}
              </p>
              <Button variant="outline" type="button" disabled={loading} onClick={refresh} className={cartPageStyles.button2}>
                Làm mới
              </Button>
            </div>

            <ul className={cartPageStyles.ul}>
              {cart.items.map((item) => (
                <li key={item.variantId} className={cartPageStyles.li}>
                  <CartItem item={item} status={checks[item.variantId]} cart={cart} onChecked={updateCheck} />
                </li>
              ))}
            </ul>
          </div>

          {/* TÓM TẮT ĐƠN HÀNG */}
          <aside aria-label="Tạm tính giỏ hàng" className={cartPageStyles.aside}>
            <h2 className={cartPageStyles.title}>Tóm tắt giỏ hàng</h2>
            
            <div className={cartPageStyles.container7}>
              <span className={cartPageStyles.labelText3}>Tạm tính</span>
              <strong className={cartPageStyles.strong}>{money.format(subtotal)}</strong>
            </div>
            
            <p className={cartPageStyles.description6}>
              Giá và tồn kho có thể thay đổi. Thêm vào giỏ không giữ hàng; tổng tiền cuối cùng được xác nhận khi đặt hàng.
            </p>

            {issues > 0 && (
              <p role="alert" className={cartPageStyles.errorDescription}>
                Có {issues} mặt hàng cần kiểm tra hoặc điều chỉnh số lượng.
              </p>
            )}

            <Button variant="default" type="button" onClick={openCheckout} disabled={checkoutDisabled} className={cartPageStyles.primaryButton}>
              Thanh toán
            </Button>

            <Button variant="outline" type="button" onClick={() => setIsClearCartOpen(true)} className={cartPageStyles.errorButton}>
              Xóa toàn bộ giỏ
            </Button>
          </aside>

        </div>
      )}

      {/* DIALOG XÁC NHẬN XÓA GIỎ HÀNG */}
      <ConfirmDialog
        isOpen={isClearCartOpen}
        title="Xóa toàn bộ giỏ hàng?"
        message="Bạn có chắc chắn muốn xóa toàn bộ sản phẩm khỏi giỏ hàng không? Hành động này không thể hoàn tác."
        confirmText="Xóa toàn bộ"
        onConfirm={handleClearCart}
        onCancel={() => setIsClearCartOpen(false)}
        isDanger={true}
      />
    </div>
  );
}
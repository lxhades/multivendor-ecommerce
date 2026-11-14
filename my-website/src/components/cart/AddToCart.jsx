import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "../common/toast.js";
import { useEffect, useRef, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { loadCartItem, cartError } from "./cartRequests.js";
import { MAX_QUANTITY, validQuantity } from "./cartModel.js";

export default function AddToCart({ product, variant, availableQuantity }) {
  const { cart } = useOutletContext();
  const [quantity, setQuantity] = useState("1");
  const [pending, setPending] = useState(false);
  const controller = useRef(null);
  const lock = useRef(false);
  useEffect(() => () => controller.current?.abort(), []);
  const inCart = cart.items.find((item) => item.variantId === variant.id)?.quantity ?? 0;
  const remaining = Math.max(0, Math.min(MAX_QUANTITY, availableQuantity) - inCart);

  async function add(event) {
    event.preventDefault();
    if (lock.current || !cart.ready) return;
    const amount = Number(quantity);
    if (!validQuantity(amount)) { toast.error("Nhập số lượng là số nguyên dương hợp lệ."); return; }
    const request = new AbortController();
    controller.current = request; lock.current = true; setPending(true);
    try {
      const { snapshot, availableQuantity: freshStock } = await loadCartItem(product.id, variant.id, { signal: request.signal });
      if (request.signal.aborted) return;
      if (cart.add(snapshot, amount, freshStock)) setQuantity("1");
    } catch (failure) { if (!request.signal.aborted) toast.error(cartError(failure)); }
    finally { lock.current = false; if (!request.signal.aborted) setPending(false); controller.current = null; }
  }

  return <form onSubmit={add} aria-busy={pending} className="grid gap-3 border-t border-border/60 pt-4">
    {inCart > 0 && <p className="text-sm text-muted-foreground">Đã có {inCart.toLocaleString("vi-VN")} sản phẩm này trong giỏ.</p>}
    <div className="flex flex-wrap items-end gap-3">
      <label className="grid gap-2 text-sm font-semibold text-foreground">Số lượng
        <Input type="number" min="1" max={remaining || 1} step="1" required value={quantity} onChange={(event) => { setQuantity(event.target.value); }}
          disabled={pending || !cart.ready || remaining === 0} className="min-h-12 w-24 rounded-lg border border-input px-3 disabled:bg-background disabled:opacity-60" />
      </label>
      <Button variant="default" type="submit" disabled={pending || !cart.ready || remaining === 0} className="min-h-12 rounded-lg bg-primary px-5 text-sm font-bold text-white hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-50">
        {pending ? "Đang kiểm tra…" : "Thêm vào giỏ"}
      </Button>
    </div>
    {!cart.ready && <p className="text-sm text-muted-foreground">Vui lòng xác minh phiên tài khoản trước khi thêm vào giỏ.</p>}
    {cart.ready && remaining === 0 && availableQuantity > 0 && <p className="text-sm text-muted-foreground">Số lượng trong giỏ đã đạt tồn kho hiện có.</p>}
    {inCart > 0 && <Link to="/cart" className="inline-flex min-h-11 w-fit items-center rounded text-sm font-semibold text-primary underline">Xem giỏ hàng</Link>}
    {cart.warning && <p role="alert" className="text-sm text-amber-800">{cart.warning}</p>}
  </form>;
}

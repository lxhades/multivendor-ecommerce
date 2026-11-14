import { toast } from "../common/toast.js";
import { useEffect, useRef, useState } from "react";
import { getSession } from "../../api/auth/authSession.js";
import { addCartItem, cartStorageKey, loadCart, setCartItem, removePurchasedItems } from "./cartModel.js";

// MainLayout keys this hook's owner scope, so carts never cross account boundaries.
export function useCart(owner) {
  const [stored, setStored] = useState(() => loadCart(owner));
  const itemsRef = useRef(stored.items);
  const ready = Boolean(owner);

  useEffect(() => {
    if (!owner) return;
    function sync(event) {
      if (event.key !== cartStorageKey(owner) && event.key !== null) return;
      const next = loadCart(owner);
      itemsRef.current = next.items;
      setStored(next);
    }
    window.addEventListener("storage", sync);
    return () => window.removeEventListener("storage", sync);
  }, [owner]);

  function commit(next) {
    if (!ready || (getSession()?.userId ?? "guest") !== owner) throw new Error("Phiên tài khoản đã thay đổi. Vui lòng thử lại.");
    let warning = "";
    try { globalThis.localStorage.setItem(cartStorageKey(owner), JSON.stringify(next)); }
    catch { warning = "Không lưu được giỏ vào trình duyệt. Giỏ hàng hiện chỉ được giữ trong phiên đang mở."; }
    itemsRef.current = next;
    setStored({ items: next, warning });
    return warning;
  }

  function change(makeNext, message) {
    try {
      const warning = commit(makeNext());
      if (warning) toast.error(warning);
      else toast.success(message);
      return true;
    } catch (error) { toast.error(error.message || "Không thể cập nhật giỏ hàng."); return false; }
  }
  function add(snapshot, quantity, availableQuantity) {
    return change(() => addCartItem(itemsRef.current, snapshot, quantity, availableQuantity), `Đã thêm ${quantity.toLocaleString("vi-VN")} sản phẩm vào giỏ.`);
  }
  function update(snapshot, quantity, availableQuantity) {
    return change(() => {
      if (!itemsRef.current.some((item) => item.variantId === snapshot.variantId)) throw new Error("Mặt hàng đã được xóa khỏi giỏ.");
      return setCartItem(itemsRef.current, snapshot, quantity, availableQuantity);
    }, "Đã cập nhật số lượng trong giỏ.");
  }
  function remove(variantId) { return change(() => itemsRef.current.filter((item) => item.variantId !== variantId), "Đã xóa mặt hàng khỏi giỏ."); }
  function clear() { return change(() => [], "Đã xóa toàn bộ giỏ hàng."); }

  function checkoutSnapshot() {
    const warning = commit(itemsRef.current);
    if (warning) toast.error(warning);
    return itemsRef.current.map((item) => ({ ...item }));
  }
  function removePurchased(lines) {
    const next = removePurchasedItems(itemsRef.current, lines);
    if (next.length === itemsRef.current.length) return true;
    const warning = commit(next);
    if (warning) toast.error(warning);
    return !warning;
  }

  return { checkoutSnapshot, removePurchased, items: stored.items, warning: stored.warning, ready, add, update, remove, clear,
    totalQuantity: stored.items.reduce((sum, item) => sum + item.quantity, 0) };
}

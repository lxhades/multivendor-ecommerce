import { normalizeCart, validQuantity, MAX_CART_LINES } from "../cart/cartModel.js";

export const checkoutStorageKey = (buyerId) => `market-lane-checkout-v1:${encodeURIComponent(buyerId)}`;
export const checkoutLabels = { PENDING: "Đang chờ xử lý", PROCESSING: "Đang xác nhận tồn kho", COMPLETED: "Đặt hàng thành công", FAILED: "Đặt hàng không thành công", EXPIRED: "Yêu cầu đặt hàng đã hết hạn" };
export const isTerminal = (status) => ["COMPLETED", "FAILED", "EXPIRED"].includes(status);
export const isDefaultAddress = (address) => address.isDefault === true || address.default === true;
export function defaultAddressChoice(addresses) {
  return String((addresses.find(isDefaultAddress) ?? addresses[0])?.addressId ?? "new");
}
export function shippingAddressFromForm(form) {
  const receiver = String(form.get("receiver") ?? "").trim();
  const phone = String(form.get("phone") ?? "").trim();
  const address = String(form.get("address") ?? "").trim();
  if (!receiver || /[\r\n]/.test(receiver) || receiver.length > 100 || !/^0[0-9]{9}$/.test(phone) || !address || address.length > 500) {
    throw new Error("Nhập tên người nhận, số điện thoại 10 số bắt đầu bằng 0 và địa chỉ hợp lệ.");
  }
  return [receiver, phone, address].join("\n");
}
export function makeCheckoutIntent(buyerId, lines, shippingAddress) {
  if (!buyerId || !lines.length || lines.length > MAX_CART_LINES || normalizeCart(lines).length !== lines.length ||
      lines.some((line) => !validQuantity(line.quantity) || !line.revision)) throw new Error("Giỏ hàng chưa hợp lệ để đặt hàng.");
  return { buyerId, key: crypto.randomUUID(), input: { paymentMethod: "COD", shippingAddress,
    items: lines.map(({ variantId, quantity }) => ({ variantId, quantity })) }, lines: lines.map((line) => ({ ...line })) };
}
export function validateCheckout(checkout, intent) {
  if (!checkout?.id || checkout.buyerId !== intent.buyerId || checkout.idempotencyKey !== intent.key ||
      checkout.paymentMethod !== "COD" || !Object.hasOwn(checkoutLabels, checkout.status)) throw new Error("Phản hồi đặt hàng không khớp yêu cầu.");
  return checkout;
}
export function validateCheckoutOrders(orders, checkout) {
  if (!Array.isArray(orders) || !orders.length || orders.some((order) => order.checkoutId !== checkout.id || order.buyerId !== checkout.buyerId ||
      order.paymentMethod !== "COD" || ![order.subtotal, order.shippingFee, order.totalAmount].every((amount) => Number.isSafeInteger(amount) && amount >= 0) ||
      order.subtotal + order.shippingFee !== order.totalAmount || !Array.isArray(order.items) || !order.items.length ||
      order.items.some((item) => !item.variantId || typeof item.productName !== "string" || !validQuantity(item.quantity) || !Number.isSafeInteger(item.unitPrice) || item.unitPrice < 0))) {
    throw new Error("Chưa xác minh được thông tin đơn hàng.");
  }
  return orders;
}
export function saveCheckoutIntent(intent) { sessionStorage.setItem(checkoutStorageKey(intent.buyerId), JSON.stringify(intent)); }
export function clearCheckoutIntent(buyerId) { sessionStorage.removeItem(checkoutStorageKey(buyerId)); }
export function readCheckoutIntent(buyerId) {
  try {
    const intent = JSON.parse(sessionStorage.getItem(checkoutStorageKey(buyerId)) ?? "null");
    if (!intent) return { intent: null, error: "" };
    if (intent.buyerId !== buyerId || typeof intent.key !== "string" || !intent.key || intent.input?.paymentMethod !== "COD" ||
        typeof intent.input.shippingAddress !== "string" || !intent.input.shippingAddress.trim() || !Array.isArray(intent.lines) || !intent.lines.length ||
        normalizeCart(intent.lines).length !== intent.lines.length || intent.lines.some((line) => !line.revision) ||
        JSON.stringify(intent.input.items) !== JSON.stringify(intent.lines.map(({ variantId, quantity }) => ({ variantId, quantity })))) throw new Error();
    if (intent.checkout) { validateCheckout(intent.checkout, intent); if (intent.orders) validateCheckoutOrders(intent.orders, intent.checkout); }
    return { intent, error: "" };
  } catch { return { intent: null, error: "Không đọc được yêu cầu đặt hàng đã lưu. Vui lòng kiểm tra quyền lưu trữ của trình duyệt rồi thử lại; chưa tạo yêu cầu mới để tránh trùng đơn." }; }
}

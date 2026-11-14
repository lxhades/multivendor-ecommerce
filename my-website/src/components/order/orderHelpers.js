export const orderStatuses = { PENDING: "Đang xác nhận", PENDING_PAYMENT: "Chờ thanh toán", CONFIRMED: "Đã xác nhận", PROCESSING: "Đang chuẩn bị hàng", SHIPPED: "Đang giao hàng", DELIVERED: "Đã giao hàng", CANCELLED: "Đã hủy" };
export const paymentStatuses = { UNPAID: "Chưa thanh toán", PENDING: "Đang xử lý", PAID: "Đã thanh toán", FAILED: "Thanh toán thất bại", REFUND_PENDING: "Chờ hoàn tiền" };
export function orderDate(value) {
  const date = new Date(value);
  return value && Number.isFinite(date.getTime()) ? date.toLocaleString("vi-VN") : "Chưa cập nhật";
}
export function canCancelOrder(order) {
  return order.paymentMethod === "COD" && order.orderStatus === "CONFIRMED" && order.paymentStatus !== "PAID";
}
export function validateBuyerOrder(order, buyerId, id = order?.id) {
  if (!order?.id || order.id !== id || order.buyerId !== buyerId || !Array.isArray(order.items) ||
    ![order.subtotal, order.shippingFee, order.totalAmount].every((value) => Number.isFinite(value) && value >= 0) ||
    order.items.some((item) => !item || !Number.isFinite(item.quantity) || !Number.isFinite(item.unitPrice))) {
    throw new Error("Dữ liệu đơn hàng không hợp lệ.");
  }
  return order;
}

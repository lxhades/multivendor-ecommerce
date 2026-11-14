export function validateSellerOrder(order, shopId, id = order?.id) {
  if (!order?.id || order.id !== id || order.shopId !== shopId || !order.buyerId || !Array.isArray(order.items) ||
      ![order.subtotal, order.shippingFee, order.totalAmount].every((value) => Number.isSafeInteger(value) && value >= 0) ||
      order.subtotal + order.shippingFee !== order.totalAmount ||
      order.items.some((item) => !item || !Number.isSafeInteger(item.quantity) || item.quantity < 1 || !Number.isSafeInteger(item.unitPrice) || item.unitPrice < 0)) {
    throw new Error("Dữ liệu đơn hàng không hợp lệ hoặc không thuộc cửa hàng.");
  }
  return order;
}
export function validateSellerOrderPage(data, shopId, page, size) {
  if (!Array.isArray(data?.items) || data.page !== page || data.size !== size || !Number.isSafeInteger(data.totalElements) || data.totalElements < 0 || !Number.isInteger(data.totalPages) || data.totalPages < 0) {
    throw new Error("Dữ liệu phân trang đơn hàng không hợp lệ.");
  }
  return { ...data, orders: data.items.map((item) => validateSellerOrder(item?.order, shopId)) };
}

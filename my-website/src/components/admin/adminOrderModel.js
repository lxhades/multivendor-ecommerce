import { validateSellerOrder } from "../seller/sellerOrderHelpers.js";
export function validateAdminOrder(order, id = order?.id) {
  if (!order?.shopId || !order.buyerId) throw new Error("Dữ liệu đơn hàng không hợp lệ.");
  return validateSellerOrder(order, order.shopId, id);
}
export const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function validateAdminOrderPage(data, page, size) {
  if (!Array.isArray(data?.items) || data.page !== page || data.size !== size || !Number.isSafeInteger(data.totalElements) || data.totalElements < 0 || data.totalPages !== Math.ceil(data.totalElements / size) || data.items.length > size) throw new Error("Phân trang đơn hàng không hợp lệ.");
  const orders = data.items.map((order) => validateAdminOrder(order));
  if (new Set(orders.map((order) => order.id)).size !== orders.length) throw new Error("Trang đơn hàng trùng mã.");
  return { ...data, orders };
}

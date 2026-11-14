export const adminShopStatuses = { ACTIVE: "Hoạt động", SUSPENDED: "Tạm ngưng", CLOSED: "Đã đóng", BANNED: "Bị cấm" };
export function validateAdminShop(shop, id = shop?.id) {
  if (!shop?.id || shop.id !== id || !shop.sellerId || typeof shop.name !== "string" || !Object.hasOwn(adminShopStatuses, shop.status) || !Number.isSafeInteger(shop.productCount) || shop.productCount < 0 || !Number.isSafeInteger(shop.ratingCount) || shop.ratingCount < 0) throw new Error("Thông tin cửa hàng không hợp lệ.");
  return shop;
}
export function validateAdminShopPage(data, page, size) {
  if (!Array.isArray(data?.content) || data.number !== page || data.size !== size || !Number.isSafeInteger(data.totalElements) || data.totalElements < 0 || !Number.isInteger(data.totalPages) || data.totalPages < 0) throw new Error("Danh sách cửa hàng không hợp lệ.");
  data.content.forEach((shop) => validateAdminShop(shop));
  return data;
}

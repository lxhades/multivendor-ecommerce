export const productStatuses = { ACTIVE: "Đang bán", INACTIVE: "Ngừng bán" };
export const approvalStatuses = { PENDING: "Chờ duyệt", APPROVED: "Đã duyệt", REJECTED: "Bị từ chối" };
export function validateSellerProduct(product, shopId, id = product?.id) {
  if (!product?.id || product.id !== id || product.shopId !== shopId || !Array.isArray(product.variants)) throw new Error("Dữ liệu sản phẩm không khớp cửa hàng.");
  return product;
}
export function searchText(value) {
  return String(value ?? "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d").replace(/Đ/g, "D").toLowerCase().trim();
}

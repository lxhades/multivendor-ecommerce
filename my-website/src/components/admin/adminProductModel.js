export const moderationLabels = { PENDING: "Chờ duyệt", APPROVED: "Đã duyệt", REJECTED: "Bị từ chối" };
export function validateModerationProduct(product, id = product?.id) {
  if (!product?.id || product.id !== id || !product.shopId || typeof product.name !== "string" || !Object.hasOwn(moderationLabels, product.approvalStatus) || !Array.isArray(product.variants) || product.variants.some((variant) => !variant?.id || typeof variant.sku !== "string" || typeof variant.price !== "number" || !Number.isFinite(variant.price) || variant.price < 0)) throw new Error("Dữ liệu sản phẩm duyệt không hợp lệ.");
  return product;
}

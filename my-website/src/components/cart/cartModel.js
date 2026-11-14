import { isMediaUrl, isVideo, mediaUrls, variantName } from "../product/productHelpers.js";

export const MAX_QUANTITY = 2147483647;
export const MAX_CART_LINES = 100;
export const cartStorageKey = (owner) => `market-lane-cart-v1:${encodeURIComponent(owner)}`;
export const validQuantity = (quantity) => Number.isInteger(quantity) && quantity >= 1 && quantity <= MAX_QUANTITY;

export function normalizeCart(value) {
  if (!Array.isArray(value)) return [];
  const seen = new Set();
  return value.filter((line) => line && typeof line.productId === "string" && line.productId.trim() &&
    typeof line.variantId === "string" && line.variantId.trim() && typeof line.productName === "string" && line.productName.trim() &&
    validQuantity(line.quantity) && typeof line.unitPrice === "number" && Number.isFinite(line.unitPrice) && line.unitPrice >= 0 &&
    line.unitPrice * line.quantity <= Number.MAX_SAFE_INTEGER && !seen.has(line.variantId) && seen.add(line.variantId))
    .slice(0, MAX_CART_LINES).map((line) => ({
      productId: line.productId, variantId: line.variantId, productName: line.productName,
      variantName: typeof line.variantName === "string" ? line.variantName : "Phân loại sản phẩm",
      shopId: typeof line.shopId === "string" ? line.shopId : null,
      sku: typeof line.sku === "string" ? line.sku : "",
      imageUrl: isMediaUrl(line.imageUrl) && !isVideo(line.imageUrl) ? line.imageUrl : null,
      unitPrice: line.unitPrice, quantity: line.quantity,
      revision: typeof line.revision === "string" && line.revision ? line.revision : crypto.randomUUID(),
    }));
}

export function productCartSnapshot(product, variantId) {
  const variant = Array.isArray(product?.variants) ? product.variants.find((item) => item.id === variantId && item.active && !item.deletedAt) : null;
  if (!variant) throw new Error("Phân loại sản phẩm đã ngừng bán hoặc không còn tồn tại.");
  const line = { productId: product.id, variantId: variant.id, shopId: product.shopId, productName: product.name,
    variantName: variantName(variant), sku: variant.sku, unitPrice: variant.price, quantity: 1,
    imageUrl: isMediaUrl(variant.imageUrl) && !isVideo(variant.imageUrl) ? variant.imageUrl : mediaUrls(product.mediaJson).find((url) => !isVideo(url)) ?? null };
  const normalized = normalizeCart([line]);
  if (!normalized.length) throw new Error("Thông tin giá hoặc sản phẩm không hợp lệ.");
  return normalized[0];
}

export function setCartItem(cart, snapshot, quantity, availableQuantity) {
  if (!validQuantity(quantity)) throw new Error("Số lượng phải là số nguyên dương hợp lệ.");
  if (!Number.isSafeInteger(availableQuantity) || availableQuantity < 0) throw new Error("Chưa xác minh được tồn kho.");
  if (quantity > availableQuantity) throw new Error(`Chỉ còn ${availableQuantity.toLocaleString("vi-VN")} sản phẩm khả dụng. Vui lòng giảm số lượng.`);
  const line = normalizeCart([{ ...snapshot, quantity, revision: crypto.randomUUID() }])[0];
  if (!line) throw new Error("Thông tin mặt hàng hoặc số lượng không hợp lệ.");
  const existing = cart.find((item) => item.variantId === line.variantId);
  if (existing && existing.productId !== line.productId) throw new Error("Thông tin phân loại sản phẩm không khớp.");
  if (!existing && cart.length >= MAX_CART_LINES) throw new Error("Giỏ hàng chỉ nhận tối đa 100 phân loại sản phẩm.");
  return existing ? cart.map((item) => item.variantId === line.variantId ? line : item) : [...cart, line];
}

export function addCartItem(cart, snapshot, quantity, availableQuantity) {
  if (!validQuantity(quantity)) throw new Error("Số lượng phải là số nguyên dương hợp lệ.");
  const existing = cart.find((item) => item.variantId === snapshot.variantId);
  return setCartItem(cart, snapshot, (existing?.quantity ?? 0) + quantity, availableQuantity);
}

export function loadCart(owner) {
  if (!owner) return { items: [], warning: "" };
  try {
    return { items: normalizeCart(JSON.parse(globalThis.localStorage.getItem(cartStorageKey(owner)) ?? "[]")), warning: "" };
  } catch {
    return { items: [], warning: "Không đọc được giỏ đã lưu. Giỏ hàng hiện chỉ được giữ trong phiên đang mở." };
  }
}

export function removePurchasedItems(cart, purchased) {
  const submitted = new Map(purchased.map((line) => [line.variantId, line]));
  return cart.filter((line) => {
    const original = submitted.get(line.variantId);
    return !original || original.productId !== line.productId || original.quantity !== line.quantity || original.revision !== line.revision;
  });
}

import { catalogSlug } from "../common/catalogPaths.js";

export function defaultProductSlug(name, shopId) {
  if (!shopId) throw new Error("Chưa xác định được cửa hàng để tạo slug.");
  const suffix = `-${shopId}`;
  const prefix = catalogSlug(name).slice(0, Math.max(1, 220 - suffix.length)).replace(/-+$/, "");
  return `${prefix}${suffix}`;
}
export function newVariantDraft() {
  return { id: crypto.randomUUID(), sku: "", price: "", active: true, attributes: [], image: null };
}
export function initialVariantInputs(drafts) {
  if (!Array.isArray(drafts) || !drafts.length) throw new Error("Sản phẩm phải có ít nhất một biến thể.");
  const seen = new Set();
  return drafts.map((draft, index) => {
    const sku = String(draft.sku ?? "").trim();
    const priceText = String(draft.price ?? "").trim();
    const price = Number(priceText);
    if (!sku || !priceText || !Number.isFinite(price) || price < 0 || price > Number.MAX_SAFE_INTEGER) throw new Error(`Biến thể ${index + 1}: nhập SKU và giá không âm hợp lệ.`);
    if (seen.has(sku)) throw new Error(`SKU “${sku}” bị trùng trong sản phẩm.`);
    seen.add(sku);
    const attributes = draft.attributes.filter((row) => row.key.trim() || row.value.trim());
    if (attributes.some((row) => !row.key.trim() || !row.value.trim()) || new Set(attributes.map((row) => row.key.trim())).size !== attributes.length) throw new Error(`Biến thể ${index + 1}: thuộc tính phải đủ tên/giá trị và không trùng tên.`);
    return { sku, price, active: Boolean(draft.active), attributesJson: JSON.stringify(Object.fromEntries(attributes.map((row) => [row.key.trim(), row.value.trim()]))), imageUrl: null };
  });
}

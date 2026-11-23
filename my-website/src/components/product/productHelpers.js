export const money = new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" });

export function mediaUrls(mediaJson) {
  try {
    const values = JSON.parse(mediaJson ?? "[]");
    return Array.isArray(values) ? [...new Set(values.filter(isMediaUrl))] : [];
  } catch { return []; }
}

export function isMediaUrl(value) {
  if (typeof value !== "string" || !value.trim()) {
    return false;
  }

  // URL tuyệt đối: ảnh từ domain bên ngoài hoặc dữ liệu cũ.
  if (/^https?:\/\//i.test(value)) {
    return true;
  }

  // URL tương đối do backend trả về.
  return /^\/media\/.+/i.test(value);
}

export function isVideo(url) {
  return /\.(mp4|webm|ogg)(?:[?#]|$)/i.test(url ?? "");
}

export function variantAttributes(variant) {
  try {
    const value = JSON.parse(variant?.attributesJson ?? "null");
    if (!value || typeof value !== "object" || Array.isArray(value)) return [];
    return Object.entries(value).filter(([, item]) => ["string", "number", "boolean"].includes(typeof item) && String(item).trim());
  } catch { return []; }
}

export function variantName(variant) {
  return variant?.variantName || variantAttributes(variant).map(([, value]) => String(value)).join(" - ") || variant?.sku || "Phân loại sản phẩm";
}

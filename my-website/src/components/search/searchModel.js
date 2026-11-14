import { categorySearchPath } from "../common/catalogPaths.js";
export const searchSorts = { RELEVANCE: "Liên quan nhất", NEWEST: "Mới nhất", PRICE_ASC: "Giá tăng dần", PRICE_DESC: "Giá giảm dần" };
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
export function readSearchFilters(params) {
  const q = (params.get("q") ?? "").trim();
  const categoryId = params.get("categoryId") ?? "";
  const shopId = params.get("shopId") ?? "";
  const sort = params.get("sort") ?? (q ? "RELEVANCE" : "NEWEST");
  const integer = (name, fallback, min, max) => {
    const text = params.get(name);
    if (text === null || text === "") return fallback;
    const value = Number(text);
    if (!/^\d+$/.test(text) || !Number.isSafeInteger(value) || value < min || value > max) throw new Error(`Tham số ${name} không hợp lệ.`);
    return value;
  };
  const page = integer("page", 1, 1, 200);
  const size = integer("size", 12, 1, 48);
  const minPrice = integer("minPrice", "", 0, Number.MAX_SAFE_INTEGER);
  const maxPrice = integer("maxPrice", "", 0, Number.MAX_SAFE_INTEGER);
  if (q.length > 120 || (categoryId && !uuid.test(categoryId)) || (shopId && !uuid.test(shopId)) || !Object.hasOwn(searchSorts, sort)) throw new Error("Từ khóa, danh mục, cửa hàng hoặc cách sắp xếp trong URL không hợp lệ.");
  if (minPrice !== "" && maxPrice !== "" && minPrice > maxPrice) throw new Error("Giá tối thiểu không được lớn hơn giá tối đa.");
  const include = params.get("includeChildren");
  if (include !== null && !["true", "false"].includes(include)) throw new Error("Bộ lọc danh mục con không hợp lệ.");
  return { q, categoryId, shopId, sort, page, size, minPrice, maxPrice, includeChildren: Boolean(categoryId) && include !== "false" };
}
export function searchParamsFor(filters, changes = {}) {
  const next = { ...filters, ...changes };
  const params = new URLSearchParams();
  for (const name of ["q", "categoryId", "shopId", "minPrice", "maxPrice", "sort", "page", "size"]) {
    if (next[name] !== "" && next[name] != null) params.set(name, String(next[name]));
  }
  if (next.categoryId) params.set("includeChildren", String(next.includeChildren));
  return params;
}
export function searchApiParams(filters) { return Object.fromEntries(searchParamsFor(filters)); }
export function validateSearchPage(data, filters) {
  if (!Array.isArray(data?.items) || data.page !== filters.page || data.size !== filters.size || !Number.isSafeInteger(data.totalElements) || data.totalElements < 0 || !Number.isSafeInteger(data.totalPages) || data.totalPages < 0 || data.items.length > filters.size || data.items.some((product) => !product?.id || typeof product.name !== "string" || (filters.shopId && product.shopId !== filters.shopId) || (product.minPrice != null && (typeof product.minPrice !== "number" || !Number.isFinite(product.minPrice) || product.minPrice < 0)))) throw new Error("Dữ liệu kết quả tìm kiếm không hợp lệ.");
  return data;
}


export function resolveCategorySearch(params, categorySlug, categories) {
  const input = new URLSearchParams(params);
  const active = categories.filter((category) => category.status === "ACTIVE");
  if (categorySlug) {
    const matches = active.filter((category) => category.slug === categorySlug);
    if (matches.length !== 1) throw new Error("Không tìm thấy danh mục hoặc slug không duy nhất.");
    if (input.has("categoryId") && input.get("categoryId") !== matches[0].id) throw new Error("Slug và ID danh mục trong URL không khớp.");
    input.set("categoryId", matches[0].id);
  }
  const filters = readSearchFilters(input);
  let category = null;
  if (filters.categoryId) {
    const matches = active.filter((item) => item.id === filters.categoryId);
    if (matches.length !== 1 || !matches[0].slug) throw new Error("Không tìm thấy danh mục đang hoạt động hoặc danh mục chưa có slug.");
    category = matches[0];
    if (active.filter((item) => item.slug === category.slug).length !== 1) throw new Error("Slug danh mục không duy nhất.");
  }
  return { filters, canonical: categorySearchPath(category, input) };
}

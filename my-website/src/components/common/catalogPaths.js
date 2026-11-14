export function catalogSlug(value) {
  return String(value ?? "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d").replace(/Đ/g, "d").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "") || "san-pham";
}
export function productPath(product) {
  const id = product.productId ?? product.id;
  const slug = product.slug || catalogSlug(product.name ?? product.productName);
  return `/products/${encodeURIComponent(slug)}/${encodeURIComponent(id)}`;
}
export function categorySearchPath(category, source = new URLSearchParams()) {
  const query = new URLSearchParams();
  for (const key of ["q", "shopId", "minPrice", "maxPrice", "sort", "page", "size"]) {
    if (source.has(key) && source.get(key) !== "") query.set(key, source.get(key));
  }
  if (category && source.get("includeChildren") === "false") query.set("includeChildren", "false");
  const path = category ? `/search/danh-muc/${encodeURIComponent(category.slug)}` : "/search";
  return `${path}${query.size ? `?${query}` : ""}`;
}

import test from "node:test";
import assert from "node:assert/strict";
import { categorySearchPath, productPath } from "../src/components/common/catalogPaths.js";
import { resolveCategorySearch, searchApiParams } from "../src/components/search/searchModel.js";

const category = { id: "30000000-0000-4000-8000-000000000003", slug: "thoi-trang-nam", name: "Thời trang nam", status: "ACTIVE" };
const other = { id: "30000000-0000-4000-8000-000000000004", slug: "do-gia-dung", status: "ACTIVE" };
const productId = "40000000-0000-4000-8000-000000000001";

test("product paths use backend slug and stable UUID, distinguishing duplicate shop-scoped slugs", () => {
  assert.equal(productPath({ id: productId, slug: "ao-thun", name: "Áo thun" }), `/products/ao-thun/${productId}`);
  assert.notEqual(productPath({ id: "one", slug: "ao-thun" }), productPath({ id: "two", slug: "ao-thun" }));
});
test("cart/order links use productId rather than order-item id and normalize a missing slug", () => {
  assert.equal(productPath({ id: "order-item-id", productId, productName: "Áo Đỏ & Trắng" }), `/products/ao-do-trang/${productId}`);
  assert.equal(productPath({ productId }), `/products/san-pham/${productId}`);
});
test("category slug URLs resolve to UUID API filters without leaking categoryId into the address", () => {
  const result = resolveCategorySearch(new URLSearchParams("q=ao&page=2&minPrice=0"), category.slug, [category]);
  assert.equal(result.filters.categoryId, category.id);
  assert.equal(searchApiParams(result.filters).categoryId, category.id);
  assert.equal(searchApiParams(result.filters).includeChildren, "true");
  assert.equal(result.canonical, "/search/danh-muc/thoi-trang-nam?q=ao&minPrice=0&page=2");
});
test("legacy category UUID URLs migrate while keeping Vietnamese query, shop, sorting and pagination", () => {
  const params = new URLSearchParams({ categoryId: category.id, includeChildren: "true", q: "Áo xanh & trắng", sort: "PRICE_ASC", page: "3", size: "24", shopId: "20000000-0000-4000-8000-000000000001" });
  const result = resolveCategorySearch(params, undefined, [category]);
  const url = new URL(result.canonical, "https://catalog.example");
  assert.equal(url.pathname, "/search/danh-muc/thoi-trang-nam");
  for (const key of ["q", "sort", "page", "size", "shopId"]) assert.equal(url.searchParams.get(key), params.get(key));
  assert.ok(!url.searchParams.has("categoryId"));
  assert.ok(!url.searchParams.has("includeChildren"));
  assert.equal(resolveCategorySearch(url.searchParams, category.slug, [category]).canonical, result.canonical);
});
test("explicit exclusion of child categories survives URL conversion and API serialization", () => {
  const result = resolveCategorySearch(new URLSearchParams("includeChildren=false"), category.slug, [category]);
  assert.ok(result.canonical.endsWith("?includeChildren=false"));
  assert.equal(searchApiParams(result.filters).includeChildren, "false");
});
test("unknown, inactive, duplicate and conflicting category references are rejected", () => {
  assert.throws(() => resolveCategorySearch(new URLSearchParams(), "missing", [category]));
  assert.throws(() => resolveCategorySearch(new URLSearchParams(), category.slug, [{ ...category, status: "INACTIVE" }]));
  assert.throws(() => resolveCategorySearch(new URLSearchParams(), category.slug, [category, { ...other, slug: category.slug }]));
  assert.throws(() => resolveCategorySearch(new URLSearchParams({ categoryId: other.id }), category.slug, [category, other]));
});
test("clearing category returns to general search without child-category or unrelated route parameters", () => {
  const path = categorySearchPath(null, new URLSearchParams({ q: "ao", categoryId: category.id, includeChildren: "false", checkout: "1", page: "1" }));
  assert.equal(path, "/search?q=ao&page=1");
  assert.equal(resolveCategorySearch(new URLSearchParams("q=ao"), undefined, []).filters.categoryId, "");
});

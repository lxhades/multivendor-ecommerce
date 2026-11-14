import test from "node:test";
import assert from "node:assert/strict";
import { defaultProductSlug, initialVariantInputs } from "../src/components/seller/productCreationModel.js";

const shopId = "20000000-0000-4000-8000-000000000001";
const row = { sku: " AO-XANH-L ", price: "120000", active: true, attributes: [{ key: "Màu sắc", value: "Xanh" }, { key: "Cỡ", value: "L" }] };
test("new product slug normalizes Vietnamese name and includes shop id within database length", () => {
  assert.equal(defaultProductSlug("Áo Thun Đỏ & Trắng", shopId), `ao-thun-do-trang-${shopId}`);
  const slug = defaultProductSlug("a".repeat(500), shopId);
  assert.ok(slug.length <= 220);
  assert.ok(slug.endsWith(`-${shopId}`));
  assert.throws(() => defaultProductSlug("Áo", ""));
});
test("creating a product requires at least one valid variant and serializes all variants together", () => {
  assert.throws(() => initialVariantInputs([]), /ít nhất một/);
  const variants = initialVariantInputs([row, { ...row, sku: "AO-DO-M", price: "0", active: false, attributes: [] }]);
  assert.deepEqual(variants[0], { sku: "AO-XANH-L", price: 120000, active: true, attributesJson: '{"Màu sắc":"Xanh","Cỡ":"L"}', imageUrl: null });
  assert.equal(variants[1].price, 0);
  assert.equal(variants[1].active, false);
});
test("duplicate SKU, empty price and incomplete/duplicate attributes cannot create a product", () => {
  assert.throws(() => initialVariantInputs([row, { ...row, sku: "AO-XANH-L" }]), /SKU/);
  for (const price of ["", "-1", "not-a-price", "Infinity"]) assert.throws(() => initialVariantInputs([{ ...row, price }]));
  assert.throws(() => initialVariantInputs([{ ...row, attributes: [{ key: "Màu", value: "" }] }]));
  assert.throws(() => initialVariantInputs([{ ...row, attributes: [{ key: "Màu", value: "Xanh" }, { key: "Màu", value: "Đỏ" }] }]));
});

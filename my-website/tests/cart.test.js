import test from "node:test";
import assert from "node:assert/strict";
import { addCartItem, setCartItem, normalizeCart, productCartSnapshot, cartStorageKey, loadCart, MAX_QUANTITY } from "../src/components/cart/cartModel.js";
import { loadCartItem } from "../src/components/cart/cartRequests.js";
import axiosClient from "../src/api/axiosClient.js";

const snapshot = { productId: "p1", variantId: "v1", productName: "Áo thun", variantName: "Xanh", unitPrice: 100000, imageUrl: "http://localhost/image.png", quantity: 1 };
const product = { id: "p1", shopId: "s1", name: "Áo thun mới", mediaJson: '["http://localhost/video.mp4","http://localhost/image.png"]', variants: [{ id: "v1", sku: "AO-XANH", price: 120000, active: true, attributesJson: '{"Màu":"Xanh"}', imageUrl: "http://localhost/variant.png" }, { id: "v2", price: 150000, active: true }] };

test("adding the same variant merges quantities and refreshes the stored display snapshot", () => {
  const first = addCartItem([], snapshot, 2, 10);
  const merged = addCartItem(first, { ...snapshot, unitPrice: 110000 }, 3, 10);
  assert.equal(merged.length, 1);
  assert.equal(merged[0].quantity, 5);
  assert.equal(merged[0].unitPrice, 110000);
  assert.equal(first[0].quantity, 2);
  assert.equal(addCartItem(merged, { ...snapshot, variantId: "v2" }, 1, 10).length, 2);
});

test("cart enforces total observed stock, valid quantities, int32 limits and line identity", () => {
  const cart = addCartItem([], snapshot, 3, 5);
  assert.throws(() => addCartItem(cart, snapshot, 3, 5), /Chỉ còn/);
  assert.throws(() => addCartItem([], snapshot, 1, 0), /Chỉ còn/);
  for (const quantity of [0, -1, 1.5, MAX_QUANTITY + 1]) assert.throws(() => setCartItem(cart, snapshot, quantity, 10));
  assert.throws(() => addCartItem(cart, { ...snapshot, productId: "other" }, 1, 10), /không khớp/);
  assert.equal(setCartItem(cart, snapshot, 1, 5)[0].quantity, 1);
});

test("cart supports at most 100 distinct variants while allowing another quantity of an existing one", () => {
  const cart = Array.from({ length: 100 }, (_, index) => ({ ...snapshot, variantId: `v${index}`, quantity: 1 }));
  assert.throws(() => addCartItem(cart, { ...snapshot, variantId: "new" }, 1, 10), /100/);
  assert.equal(addCartItem(cart, snapshot, 1, 10).find((item) => item.variantId === "v1").quantity, 2);
});

test("untrusted cart storage is sanitized, deduplicated and cannot carry credentials", () => {
  const normalized = normalizeCart([snapshot, { ...snapshot, quantity: 2 }, { ...snapshot, variantId: "bad", quantity: -1 }, { ...snapshot, variantId: "v2", accessToken: "secret", unitPrice: -1 }]);
  assert.equal(normalized.length, 1);
  assert.equal(normalized[0].quantity, 1);
  assert.equal(normalizeCart([{ ...snapshot, accessToken: "secret", password: "secret" }])[0].accessToken, undefined);
  assert.equal(normalizeCart([{ ...snapshot, imageUrl: "javascript:alert(1)" }])[0].imageUrl, null);
});

test("guest/account carts use separate storage keys and corrupted/blocked storage is handled", () => {
  assert.notEqual(cartStorageKey("guest"), cartStorageKey("user-1"));
  assert.notEqual(cartStorageKey("user-1"), cartStorageKey("user-2"));
  const original = Object.getOwnPropertyDescriptor(globalThis, "localStorage");
  try {
    Object.defineProperty(globalThis, "localStorage", { configurable: true, value: { getItem: () => "{bad" } });
    assert.deepEqual(loadCart("guest").items, []);
    assert.ok(loadCart("guest").warning);
    Object.defineProperty(globalThis, "localStorage", { configurable: true, get: () => { throw new Error("blocked"); } });
    assert.deepEqual(loadCart("user-1").items, []);
    assert.ok(loadCart("user-1").warning);
  } finally { if (original) Object.defineProperty(globalThis, "localStorage", original); else delete globalThis.localStorage; }
});

test("product snapshots use the chosen active variant's price, image and attributes", () => {
  const result = productCartSnapshot(product, "v1");
  assert.equal(result.unitPrice, 120000);
  assert.throws(() => productCartSnapshot({ ...product, variants: {} }, "v1"), /ngừng bán/);
  assert.equal(result.imageUrl, "http://localhost/variant.png");
  assert.equal(result.variantName, "Xanh");
  assert.equal(productCartSnapshot(product, "v2").imageUrl, "http://localhost/image.png");
  assert.throws(() => productCartSnapshot({ ...product, variants: [{ ...product.variants[0], active: false }] }, "v1"), /ngừng bán/);
});

test("cart rechecks public product and variant availability and rejects mismatched stock responses", async () => {
  const original = axiosClient.defaults.adapter;
  let wrongVariant = false;
  const calls = [];
  axiosClient.defaults.adapter = async (config) => {
    calls.push(config);
    return { data: { data: config.url.startsWith("/api/products/") ? product : { variantId: wrongVariant ? "other" : "v1", availableQuantity: 3 } }, status: 200, headers: {}, config };
  };
  try {
    const signal = new AbortController().signal;
    const checked = await loadCartItem("p1", "v1", { signal });
    assert.equal(checked.availableQuantity, 3);
    assert.equal(checked.snapshot.unitPrice, 120000);
    assert.ok(calls.every((call) => call.skipAuth && call.signal === signal));
    wrongVariant = true;
    await assert.rejects(loadCartItem("p1", "v1"), /không khớp/);
  } finally { axiosClient.defaults.adapter = original; }
});

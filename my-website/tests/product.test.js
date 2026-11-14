import test from "node:test";
import assert from "node:assert/strict";
import { isVideo, mediaUrls, variantAttributes, variantName } from "../src/components/product/productHelpers.js";
import { getProduct } from "../src/api/product/productApi.js";
import { getAvailableStock } from "../src/api/inventory/inventoryApi.js";
import axiosClient from "../src/api/axiosClient.js";

 test("product media tolerates malformed JSON and keeps unique HTTP image/video URLs", () => {
  assert.deepEqual(mediaUrls('{bad'), []);
  assert.deepEqual(mediaUrls('{"image":"http://localhost/a.png"}'), []);
  assert.deepEqual(mediaUrls(JSON.stringify(["http://localhost/a.png", "https://example.com/video.mp4?token=1", "http://localhost/a.png", "javascript:alert(1)", null])), ["http://localhost/a.png", "https://example.com/video.mp4?token=1"]);
  assert.equal(isVideo("https://example.com/video.MP4?token=1"), true);
  assert.equal(isVideo("https://example.com/image.png"), false);
});

test("variant attributes retain zero/false and exclude nested values from display labels", () => {
  const variant = { attributesJson: '{"Cỡ":"L","Số lượng":0,"Có mũ":false,"Khác":{"a":1},"Trống":""}' };
  assert.deepEqual(variantAttributes(variant), [["Cỡ", "L"], ["Số lượng", 0], ["Có mũ", false]]);
  assert.equal(variantName(variant), "L - 0 - false");
  assert.equal(variantName({ variantName: "Tên từ backend", sku: "SKU" }), "Tên từ backend");
});

test("variant labels fall back to SKU when legacy attributes are absent or malformed", () => {
  for (const attributesJson of [null, "invalid", "[]", "null"]) {
    assert.equal(variantName({ attributesJson, sku: "AO-XANH" }), "AO-XANH");
  }
});

test("product detail and selected-variant stock use public existing APIs and unwrap envelopes", async () => {
  const original = axiosClient.defaults.adapter;
  const requests = [];
  axiosClient.defaults.adapter = async (config) => {
    requests.push(config);
    return { data: { data: config.url.startsWith("/api/products/") ? { id: "p1", variants: [] } : { variantId: "v1", availableQuantity: 0 } }, status: 200, headers: {}, config };
  };
  try {
    assert.equal((await getProduct("p1", { skipAuth: true })).id, "p1");
    assert.equal((await getAvailableStock("v1", { skipAuth: true })).availableQuantity, 0);
    assert.deepEqual(requests.map((request) => request.url), ["/api/products/p1", "/api/inventories/variant/v1"]);
    assert.ok(requests.every((request) => request.skipAuth));
  } finally { axiosClient.defaults.adapter = original; }
});

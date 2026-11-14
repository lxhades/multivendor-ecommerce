import test from "node:test";
import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { createServer } from "vite";
import axiosClient from "../src/api/axiosClient.js";
import { getAddresses } from "../src/api/auth/addressApi.js";
import { saveSession } from "../src/api/auth/authSession.js";

const addresses = [
  { addressId: 1, receiverName: "Người nhận khác", phone: "0901111111", detail: "Số 1", location: "Đà Nẵng", isDefault: false },
  { addressId: 2, receiverName: "Nguyễn An", phone: "0901234567", detail: "Số 12", location: "Hà Nội", isDefault: true },
];

test("checkout addresses use the logged-in user's address endpoint with bearer token and cancellation", async () => {
  const original = axiosClient.defaults.adapter;
  const signal = new AbortController().signal;
  saveSession({ userId: "buyer-1", accessToken: "buyer-access", refreshToken: "buyer-refresh" });
  axiosClient.defaults.adapter = async (config) => {
    assert.equal(config.url, "/auth/users/buyer-1/addresses");
    assert.equal(config.headers.get("Authorization"), "Bearer buyer-access");
    assert.equal(config.signal, signal);
    return { data: { data: addresses }, status: 200, headers: {}, config };
  };
  try { assert.deepEqual(await getAddresses("buyer-1", { signal }), addresses); }
  finally { axiosClient.defaults.adapter = original; saveSession(null); }
});

test("checkout form fills recipient/phone/address from default API address and allows a manual address", async () => {
  const server = await createServer({ root: fileURLToPath(new URL("../", import.meta.url)), server: { middlewareMode: true, hmr: false, ws: false, open: false }, appType: "custom", logLevel: "error" });
  try {
    const { CheckoutAddressForm } = await server.ssrLoadModule("/src/components/checkout/CheckoutAddressFields.jsx");
    const html = renderToStaticMarkup(React.createElement(CheckoutAddressForm, { addresses, retry() {} }));
    const selected = html.match(/<option[^>]*value="2"[^>]*>/)?.[0];
    assert.ok(selected?.includes("selected"));
    assert.ok(html.match(/<input[^>]*name="receiver"[^>]*>/)?.[0].includes('value="Nguyễn An"'));
    assert.ok(html.match(/<input[^>]*name="phone"[^>]*>/)?.[0].includes('value="0901234567"'));
    assert.match(html, /Số 12, Hà Nội/);
    assert.match(html, /Nhập địa chỉ mới/);
    const empty = renderToStaticMarkup(React.createElement(CheckoutAddressForm, { addresses: [], retry() {} }));
    assert.match(empty, /Bạn chưa có địa chỉ đã lưu/);
    assert.ok(!empty.includes('readOnly=""'));
    assert.match(empty, /name="receiver"/);
    assert.match(empty, /name="phone"/);
    assert.match(empty, /name="address"/);
  } finally { await server.close(); }
});


test("opening checkout keeps the confirmation form even with a saved request; recovery is explicit", async () => {
  const original = globalThis.sessionStorage;
  const values = new Map();
  globalThis.sessionStorage = { getItem: (key) => values.get(key) ?? null, setItem: (key, value) => values.set(key, value) };
  const server = await createServer({ root: fileURLToPath(new URL("../", import.meta.url)), server: { middlewareMode: true, hmr: false, ws: false, open: false }, appType: "custom", logLevel: "error" });
  try {
    const { makeCheckoutIntent, saveCheckoutIntent } = await server.ssrLoadModule("/src/components/checkout/checkoutModel.js");
    const CheckoutDialog = (await server.ssrLoadModule("/src/components/checkout/CheckoutDialog.jsx")).default;
    const line = { productId: "p1", variantId: "v1", productName: "Áo thun", variantName: "Xanh", unitPrice: 100000, quantity: 1, revision: "r1" };
    const cart = { ready: true, items: [line] };
    for (const saved of [false, true]) {
      if (saved) saveCheckoutIntent(makeCheckoutIntent("buyer-1", [line], "Saved address"));
      const html = renderToStaticMarkup(React.createElement(CheckoutDialog, { buyerId: "buyer-1", cart, onClose() {}, onSuccess() { assert.fail("opening cannot complete an order"); } }));
      assert.match(html, /<form/);
      assert.match(html, /Đang tải địa chỉ nhận hàng/);
      assert.match(html, /Xác nhận đặt hàng/);
      assert.equal(html.includes("Kiểm tra yêu cầu trước đó"), saved);
      assert.ok(!html.includes("Đang gửi yêu cầu đặt hàng"));
      assert.ok(!html.includes("Saved address"));
    }
  } finally { await server.close(); globalThis.sessionStorage = original; }
});

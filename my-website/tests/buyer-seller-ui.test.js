import test from "node:test";
import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { MemoryRouter } from "react-router-dom";
import { createServer } from "vite";
import axiosClient from "../src/api/axiosClient.js";
import { forgotPassword } from "../src/api/auth/authApi.js";
import { getShops } from "../src/api/shop/shopApi.js";
import { createSellerProduct, createSellerVariant, updateSellerProductStatus } from "../src/api/product/sellerProductApi.js";

test("forgot password uses the existing public PUT contract and shop listing forwards server pagination", async () => {
  const adapter = axiosClient.defaults.adapter;
  const calls = [];
  axiosClient.defaults.adapter = async (config) => { calls.push(config); return { data: { data: { content: [], number: 1, totalPages: 0, totalElements: 0 } }, status: 200, headers: {}, config }; };
  try {
    await forgotPassword("buyer@example.com");
    assert.equal(calls[0].method, "put");
    assert.equal(calls[0].url, "/auth/forgotpassword");
    assert.equal(calls[0].skipAuth, true);
    assert.deepEqual(JSON.parse(calls[0].data), { email: "buyer@example.com" });
    const data = await getShops({ page: 1, size: 12, sort: "createdAt,desc" }, { skipAuth: true });
    assert.equal(data.number, 1);
    assert.equal(calls[1].url, "/shops");
    assert.deepEqual(calls[1].params, { page: 1, size: 12, sort: "createdAt,desc" });
  } finally { axiosClient.defaults.adapter = adapter; }
});
test("seller creates product/variant separately and changes status through the dedicated endpoint", async () => {
  const adapter = axiosClient.defaults.adapter;
  const calls = [];
  const signal = new AbortController().signal;
  axiosClient.defaults.adapter = async (config) => { calls.push(config); return { data: { data: { id: "p1", shopId: "s1", variants: [] } }, status: 200, headers: {}, config }; };
  try {
    const input = { name: "Áo thun", slug: "ao-thun", categoryId: "c1", description: "Cotton", mediaJson: "[]", variants: [{ sku: "AO-XANH-L", price: 120000, attributesJson: "{}", active: true, imageUrl: null }] };
    await createSellerProduct("s1", input, { signal });
    await createSellerVariant("s1", "p1", { sku: "AO-XANH-L", price: 120000, attributesJson: '{"Màu":"Xanh"}', active: true, imageUrl: null }, { signal });
    await updateSellerProductStatus("s1", "p1", "ACTIVE", { signal });
    assert.equal(calls[0].url, "/api/products/seller/shops/s1");
    assert.deepEqual(JSON.parse(calls[0].data), input);
    assert.equal(calls[1].url, "/api/products/seller/shops/s1/p1/variants");
    assert.equal(JSON.parse(calls[1].data).sku, "AO-XANH-L");
    assert.equal(calls[2].method, "patch");
    assert.equal(calls[2].url, "/api/products/seller/shops/s1/p1/status");
    assert.deepEqual(JSON.parse(calls[2].data), { status: "ACTIVE" });
    assert.ok(calls.every((call) => call.signal === signal));
  } finally { axiosClient.defaults.adapter = adapter; }
});
test("new forms expose forgot password, initial category selection, media and add-variant fields", async () => {
  const server = await createServer({ root: fileURLToPath(new URL("../", import.meta.url)), server: { middlewareMode: true, hmr: false, ws: false, open: false }, appType: "custom", logLevel: "error" });
  try {
    const AuthForm = (await server.ssrLoadModule("/src/components/auth/AuthForm.jsx")).default;
    const ProductForm = (await server.ssrLoadModule("/src/components/seller/SellerProductEditForm.jsx")).default;
    const VariantForm = (await server.ssrLoadModule("/src/components/seller/SellerVariantEditForm.jsx")).default;
    const login = renderToStaticMarkup(React.createElement(MemoryRouter, null, React.createElement(AuthForm, { onForgotPassword() {} })));
    assert.match(login, /Quên mật khẩu/);
    const product = renderToStaticMarkup(React.createElement(ProductForm, { creating: true, shopId: "20000000-0000-4000-8000-000000000001", product: { name: "Áo thun", slug: "", categoryId: "", mediaJson: "[]" }, categories: [{ id: "c1", name: "Thời trang" }] }));
    assert.match(product, /Tạo sản phẩm/);
    assert.match(product, /Biến thể sản phẩm/);
    assert.match(product, /ao-thun-20000000-0000-4000-8000-000000000001/);
    assert.match(product, /readOnly/);
    assert.match(product, /Chọn danh mục/);
    assert.match(product, /type="file"/);
    const variant = renderToStaticMarkup(React.createElement(VariantForm, { creating: true }));
    assert.match(variant, /Thêm biến thể mới/);
    assert.match(variant, /name="sku"/);
    assert.match(variant, /name="price"/);
    assert.match(variant, /type="file"/);
  } finally { await server.close(); }
});

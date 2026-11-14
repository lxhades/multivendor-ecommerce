import test from "node:test";
import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { MemoryRouter, Routes, Route, Outlet } from "react-router-dom";
import { createServer } from "vite";

test("cart checkout action is available for items and empty/loading carts cannot submit", async () => {
  const root = fileURLToPath(new URL("../", import.meta.url));
  const server = await createServer({ root, server: { middlewareMode: true, hmr: false, ws: false, open: false }, appType: "custom", logLevel: "error" });
  try {
    const CartPage = (await server.ssrLoadModule("/src/pages/cart/CartPage.jsx")).default;
    for (const items of [[], [{ productId: "p1", variantId: "v1", productName: "Áo thun", variantName: "Xanh", unitPrice: 100000, quantity: 1, revision: "r1" }]]) {
      const cart = { ready: true, items, totalQuantity: items.length, warning: "" };
      const auth = { user: null, checking: false, sessionError: null };
      const html = renderToStaticMarkup(React.createElement(MemoryRouter, { initialEntries: ["/cart"] },
        React.createElement(Routes, null, React.createElement(Route, { element: React.createElement(Outlet, { context: { cart, auth } }) },
          React.createElement(Route, { path: "/cart", element: React.createElement(CartPage) })))));
      const buttons = [...html.matchAll(/<button\b([^>]*)>([\s\S]*?)<\/button>/g)];
      const action = buttons.find(([, , content]) => content.replace(/<[^>]+>/g, "").replace(/\s+/g, " ").trim() === "Thanh toán");
      if (items.length) {
        assert.ok(action, "non-empty carts expose checkout in the order summary");
        assert.ok(action[1].includes("disabled"), "availability must be checked before submitting");
      } else assert.ok(!action || action[1].includes("disabled"), "an empty cart cannot submit checkout");
      assert.ok(html.includes('id="cart-checkout-help"'), "the page explains why checkout is unavailable");
    }
  } finally { await server.close(); }
});

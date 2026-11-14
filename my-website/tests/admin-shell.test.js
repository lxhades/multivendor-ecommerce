import test from "node:test";
import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { MemoryRouter, Outlet, Route, Routes } from "react-router-dom";
import { createServer } from "vite";

test("admin option is limited to ADMIN and the management layout blocks other roles and unverified sessions", async () => {
  const server = await createServer({ root: fileURLToPath(new URL("../", import.meta.url)), server: { middlewareMode: true, hmr: false, ws: false, open: false }, appType: "custom", logLevel: "error" });
  try {
    const AccountMenu = (await server.ssrLoadModule("/src/components/auth/AccountMenu.jsx")).default;
    const AdminLayout = (await server.ssrLoadModule("/src/components/admin/AdminLayout.jsx")).default;
    for (const role of ["ADMIN", "BUYER", "SELLER"]) {
      const auth = { user: { userId: "u1", email: "account@example.com", role }, checking: false, signingOut: false };
      const menu = renderToStaticMarkup(React.createElement(MemoryRouter, null, React.createElement(AccountMenu, { auth })));
      assert.equal(menu.includes('href="/admin"'), role === "ADMIN");
      const html = renderToStaticMarkup(React.createElement(MemoryRouter, { initialEntries: ["/admin"] }, React.createElement(Routes, null,
        React.createElement(Route, { element: React.createElement(Outlet, { context: { auth, cart: {} } }) },
          React.createElement(Route, { path: "/admin", element: React.createElement(AdminLayout) },
            React.createElement(Route, { index: true, element: React.createElement("p", null, "Nội dung quản trị") }))))));
      assert.equal(html.includes("Nội dung quản trị"), role === "ADMIN");
      assert.equal(html.includes("Điều hướng quản trị"), role === "ADMIN");
      if (role !== "ADMIN") assert.match(html, /Không có quyền truy cập/);
    }
    for (const auth of [{ checking: true, user: null }, { sessionError: "Không xác minh được phiên", user: null }]) {
      const html = renderToStaticMarkup(React.createElement(MemoryRouter, null, React.createElement(Routes, null,
        React.createElement(Route, { element: React.createElement(Outlet, { context: { auth } }) },
          React.createElement(Route, { path: "*", element: React.createElement(AdminLayout) })))));
      assert.ok(!html.includes("Điều hướng quản trị"));
      assert.ok(!html.includes("Nội dung quản trị"));
    }
  } finally { await server.close(); }
});

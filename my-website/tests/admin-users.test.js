import test from "node:test";
import assert from "node:assert/strict";
import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { fileURLToPath } from "node:url";
import { createServer } from "vite";
import axiosClient from "../src/api/axiosClient.js";
import { getAdminUsers, updateAdminUser, setAdminUserStatus, deleteAdminUser } from "../src/api/auth/adminUserApi.js";
import { adminUserContact, validateAdminUser } from "../src/components/admin/adminUserModel.js";

test("admin user writes send contact only, explicit target status and DELETE to the correct user", async () => {
  const original = axiosClient.defaults.adapter;
  const calls = [];
  axiosClient.defaults.adapter = async (config) => { calls.push(config); return { data: { data: {} }, status: 200, headers: {}, config }; };
  try {
    await getAdminUsers({ keyword: "buyer", page: 1, size: 10 });
    await updateAdminUser("u1", { email: "buyer@example.com", phone: "0901234567" });
    await setAdminUserStatus("u1", "LOCKED");
    await deleteAdminUser("u1");
    assert.deepEqual(calls[0].params, { keyword: "buyer", page: 1, size: 10 });
    assert.equal(calls[1].method, "put");
    assert.deepEqual(JSON.parse(calls[1].data), { email: "buyer@example.com", phone: "0901234567" });
    assert.equal(calls[2].url, "/api/users/admin/u1/status");
    assert.equal(calls[2].method, "patch");
    assert.deepEqual(JSON.parse(calls[2].data), { status: "LOCKED" });
    assert.equal(calls[3].method, "delete");
    assert.equal(calls[3].url, "/api/users/admin/u1");
  } finally { axiosClient.defaults.adapter = original; }
});
test("admin contacts validate phone/email and retain backend LOCKED status", () => {
  const form = new FormData(); form.set("email", " buyer@example.com "); form.set("phone", "0901234567");
  assert.deepEqual(adminUserContact(form), { email: "buyer@example.com", phone: "0901234567" });
  form.set("phone", "123"); assert.throws(() => adminUserContact(form));
  assert.equal(validateAdminUser({ userId: "u1", email: "x@example.com", phone: "0901234567", role: "BUYER", status: "LOCKED" }).status, "LOCKED");
  assert.throws(() => validateAdminUser({ userId: "u1", email: "x@example.com", phone: "0901234567", role: "BUYER", status: "BANNED" }));
});
test("table exposes edit/ban/delete but prevents banning or deleting the current admin", async () => {
  const server = await createServer({ root: fileURLToPath(new URL("../", import.meta.url)), server: { middlewareMode: true, hmr: false, ws: false, open: false }, appType: "custom", logLevel: "error" });
  try {
    const Table = (await server.ssrLoadModule("/src/components/admin/AdminUsersTable.jsx")).default;
    const html = renderToStaticMarkup(React.createElement(Table, { adminId: "admin-1", users: [{ userId: "admin-1", email: "admin@example.com", phone: "0901234567", role: "ADMIN", status: "ACTIVE" }, { userId: "buyer-1", email: "buyer@example.com", phone: "0901234567", role: "BUYER", status: "LOCKED" }] }));
    assert.match(html, /Bỏ cấm/);
    assert.match(html, /Tài khoản của bạn/);
    const rows = [...html.matchAll(/<tr[^>]*>([\s\S]*?)<\/tr>/g)].map((match) => match[1]);
    const self = rows.find((row) => row.includes("admin@example.com"));
    const buttons = [...self.matchAll(/<button([^>]*)>([^<]*)<\/button>/g)];
    assert.ok(buttons.find(([, attrs, label]) => label === "Sửa" && !attrs.includes("disabled")));
    assert.ok(buttons.find(([, attrs, label]) => label === "Cấm" && attrs.includes("disabled")));
    assert.ok(buttons.find(([, attrs, label]) => label === "Xóa" && attrs.includes("disabled")));
  } finally { await server.close(); }
});

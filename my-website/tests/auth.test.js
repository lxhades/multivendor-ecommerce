import test from "node:test";
import assert from "node:assert/strict";
import { readAuthInput, validateProfile, validateTokens, authErrorMessage } from "../src/components/auth/authHelpers.js";
import axiosClient from "../src/api/axiosClient.js";
import { login, registerBuyer, getCurrentUser } from "../src/api/auth/authApi.js";
import { saveSession } from "../src/api/auth/authSession.js";

function form(values) {
  const data = new FormData();
  for (const [key, value] of Object.entries(values)) data.set(key, value);
  return data;
}

test("login trims email but preserves password and accepts a reset password shorter than eight characters", () => {
  assert.deepEqual(readAuthInput(form({ email: " buyer@example.com ", password: " abc123 " })), { email: "buyer@example.com", password: " abc123 " });
  assert.throws(() => readAuthInput(form({ email: "bad-email", password: "password" })));
});

test("registration checks phone/password confirmation and sends only the backend fields", () => {
  const input = { email: "buyer@example.com", password: "Password123", passwordConfirm: "Password123", phone: "0901234567" };
  assert.deepEqual(readAuthInput(form(input), true), { email: input.email, password: input.password, phone: input.phone });
  assert.throws(() => readAuthInput(form({ ...input, passwordConfirm: "different" }), true), /không khớp/);
  assert.throws(() => readAuthInput(form({ ...input, phone: "123" }), true), /điện thoại/);
  assert.throws(() => readAuthInput(form({ ...input, password: "short", passwordConfirm: "short" }), true), /8 đến 20/);
});

test("a session requires complete tokens and an active profile with matching buyer/seller/admin identity", () => {
  const tokens = { userId: "u1", accessToken: "access", refreshToken: "refresh" };
  assert.equal(validateTokens(tokens), tokens);
  assert.throws(() => validateTokens({ ...tokens, refreshToken: "" }));
  for (const role of ["BUYER", "SELLER", "ADMIN"]) assert.equal(validateProfile({ userId: "u1", status: "ACTIVE", role }, tokens).role, role);
  assert.throws(() => validateProfile({ userId: "other", status: "ACTIVE", role: "BUYER" }, tokens));
  assert.throws(() => validateProfile({ userId: "u1", status: "LOCKED", role: "BUYER" }, tokens));
  assert.throws(() => validateProfile({ userId: "u1", status: "ACTIVE", role: "UNKNOWN" }, tokens));
});

test("authentication errors use backend details while server failures get a readable message", () => {
  assert.equal(authErrorMessage({ response: { status: 400, data: { details: ["Email đã tồn tại."] } } }), "Email đã tồn tại.");
  assert.match(authErrorMessage({ response: { status: 500, data: { message: "internal stack" } } }), /Máy chủ/);
});

test("login/register omit previous bearer token; profile verification uses the newly issued token", async () => {
  const original = axiosClient.defaults.adapter;
  const requests = [];
  saveSession({ userId: "old-user", accessToken: "old-access", refreshToken: "old-refresh" });
  axiosClient.defaults.adapter = async (config) => {
    requests.push(config);
    return { data: { data: { userId: "u1" } }, status: 200, headers: {}, config };
  };
  try {
    await login({ email: "buyer@example.com", password: "secret" });
    await registerBuyer({ email: "buyer@example.com", phone: "0901234567", password: "Password123" });
    await getCurrentUser({ headers: { Authorization: "Bearer new-access" } });
    assert.deepEqual(requests.map((request) => request.url), ["/auth/login", "/auth/register", "/auth/me"]);
    assert.equal(requests[0].headers.get("Authorization"), undefined);
    assert.equal(requests[1].headers.get("Authorization"), undefined);
    assert.equal(requests[2].headers.get("Authorization"), "Bearer new-access");
    assert.equal(JSON.parse(requests[1].data).phone, "0901234567");
  } finally { axiosClient.defaults.adapter = original; saveSession(null); }
});

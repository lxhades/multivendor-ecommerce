import test from "node:test";
import assert from "node:assert/strict";
import { defaultAddressChoice, shippingAddressFromForm, makeCheckoutIntent, saveCheckoutIntent, readCheckoutIntent, clearCheckoutIntent, validateCheckout, validateCheckoutOrders } from "../src/components/checkout/checkoutModel.js";
import { normalizeCart, setCartItem, removePurchasedItems } from "../src/components/cart/cartModel.js";
import { createCheckout } from "../src/api/order/checkoutApi.js";
import axiosClient from "../src/api/axiosClient.js";

const line = { productId: "p1", variantId: "v1", productName: "Áo thun", variantName: "Xanh", unitPrice: 120000, quantity: 2, revision: "submitted-revision" };
function shippingForm(values = {}) {
  const form = new FormData();
  for (const [key, value] of Object.entries({ receiver: " Nguyễn An ", phone: "0901234567", address: " Hà Nội ", ...values })) form.set(key, value);
  return form;
}
function storage() {
  const values = new Map();
  return { getItem: (key) => values.get(key) ?? null, setItem: (key, value) => values.set(key, value), removeItem: (key) => values.delete(key) };
}

test("checkout selects the default address, supports alternate JSON default flag and falls back safely", () => {
  assert.equal(defaultAddressChoice([{ addressId: 1 }, { addressId: 2, isDefault: true }]), "2");
  assert.equal(defaultAddressChoice([{ addressId: 3, default: true }]), "3");
  assert.equal(defaultAddressChoice([{ addressId: 4 }]), "4");
  assert.equal(defaultAddressChoice([]), "new");
});

test("recipient fields serialize predictably and invalid delivery details are rejected", () => {
  assert.equal(shippingAddressFromForm(shippingForm()), "Nguyễn An\n0901234567\nHà Nội");
  assert.throws(() => shippingAddressFromForm(shippingForm({ phone: "123" })));
  assert.throws(() => shippingAddressFromForm(shippingForm({ address: " " })));
  assert.throws(() => shippingAddressFromForm(shippingForm({ receiver: "An\nOther" })));
});

test("checkout intent forces COD, excludes client price from request and snapshots original revisions", () => {
  const intent = makeCheckoutIntent("buyer-1", [line], "address");
  assert.equal(intent.input.paymentMethod, "COD");
  assert.deepEqual(intent.input.items, [{ variantId: "v1", quantity: 2 }]);
  assert.equal(intent.lines[0].revision, "submitted-revision");
  assert.notEqual(intent.lines[0], line);
  assert.throws(() => makeCheckoutIntent("buyer-1", [line, line], "address"));
  assert.throws(() => makeCheckoutIntent("buyer-1", [], "address"));
});

test("recovery persists the same key/payload under the buyer and never silently resets corrupt data", () => {
  const original = globalThis.sessionStorage;
  const mock = storage(); globalThis.sessionStorage = mock;
  try {
    const intent = makeCheckoutIntent("buyer-1", [line], "address");
    saveCheckoutIntent(intent);
    assert.deepEqual(readCheckoutIntent("buyer-1").intent, intent);
    assert.equal(readCheckoutIntent("other-buyer").intent, null);
    const key = `market-lane-checkout-v1:${encodeURIComponent("buyer-1")}`;
    mock.setItem(key, "{invalid");
    assert.ok(readCheckoutIntent("buyer-1").error);
    mock.setItem(key, JSON.stringify({ ...intent, input: { ...intent.input, items: [{ variantId: "v1", quantity: 99 }] } }));
    assert.ok(readCheckoutIntent("buyer-1").error);
    clearCheckoutIntent("buyer-1");
    assert.equal(readCheckoutIntent("buyer-1").intent, null);
  } finally { globalThis.sessionStorage = original; }
});

test("completed checkout removes only unchanged purchased variants, including removal/re-add races", () => {
  const initial = normalizeCart([line, { ...line, variantId: "v2", revision: "untouched" }]);
  assert.deepEqual(removePurchasedItems(initial, [line]).map((item) => item.variantId), ["v2"]);
  const edited = setCartItem(initial, line, 3, 10);
  assert.equal(removePurchasedItems(edited, [line]).length, 2);
  const editedSameQuantity = setCartItem(initial, line, 2, 10);
  assert.equal(removePurchasedItems(editedSameQuantity, [line]).length, 2);
  const readded = normalizeCart([{ ...line, revision: "new-add" }]);
  assert.equal(removePurchasedItems(readded, [line]).length, 1);
});

test("server checkout must match buyer/key/COD; order totals must belong to that checkout", () => {
  const intent = { buyerId: "buyer-1", key: "same-key" };
  const checkout = { id: "checkout-1", buyerId: "buyer-1", idempotencyKey: "same-key", paymentMethod: "COD", status: "COMPLETED" };
  const order = { checkoutId: "checkout-1", buyerId: "buyer-1", paymentMethod: "COD", subtotal: 240000, shippingFee: 0, totalAmount: 240000, items: [line] };
  assert.equal(validateCheckout(checkout, intent), checkout);
  assert.deepEqual(validateCheckoutOrders([order], checkout), [order]);
  assert.throws(() => validateCheckout({ ...checkout, buyerId: "other" }, intent));
  assert.throws(() => validateCheckout({ ...checkout, idempotencyKey: "wrong" }, intent));
  assert.throws(() => validateCheckoutOrders([{ ...order, totalAmount: 1 }], checkout));
  assert.throws(() => validateCheckoutOrders([{ ...order, items: undefined }], checkout));
});

test("checkout retries send exactly the same Idempotency-Key and request payload", async () => {
  const original = axiosClient.defaults.adapter;
  const requests = [];
  axiosClient.defaults.adapter = async (config) => { requests.push(config); return { data: { data: {} }, status: 201, headers: {}, config }; };
  try {
    const intent = makeCheckoutIntent("buyer-1", [line], "address");
    await createCheckout(intent.input, intent.key);
    await createCheckout(intent.input, intent.key);
    assert.equal(requests[0].headers.get("Idempotency-Key"), requests[1].headers.get("Idempotency-Key"));
    assert.equal(requests[0].data, requests[1].data);
    assert.equal(JSON.parse(requests[0].data).paymentMethod, "COD");
  } finally { axiosClient.defaults.adapter = original; }
});

import test from "node:test";
import assert from "node:assert/strict";
import { toast, toastConfig, getToast, subscribeToast, getToastHost, registerToastHost } from "../src/components/common/toast.js";

test("one shared toast displays the latest action and supports success/error", () => {
  toast.dismiss();
  const first = toast.success("Đã thêm vào giỏ.");
  assert.equal(getToast().type, "success");
  const second = toast.error("Không đủ tồn kho.");
  assert.notEqual(first, second);
  assert.deepEqual(getToast(), { id: second, type: "error", message: "Không đủ tồn kho." });
  toast.dismiss();
});

test("an old auto-close callback cannot dismiss a newer notification", () => {
  const old = toast.success("Cập nhật lần đầu.");
  const current = toast.success("Cập nhật mới.");
  toast.dismiss(old);
  assert.equal(getToast().id, current);
  toast.dismiss(current);
  assert.equal(getToast(), null);
});

test("toast subscriptions are removed cleanly and do not publish blank messages", () => {
  toast.dismiss();
  let events = 0;
  const unsubscribe = subscribeToast(() => events++);
  assert.equal(toast.success("   "), null);
  assert.equal(events, 0);
  toast.success("  Đăng nhập thành công.  ");
  assert.equal(getToast().message, "Đăng nhập thành công.");
  assert.equal(events, 1);
  unsubscribe();
  toast.error("Lỗi kết nối.");
  assert.equal(events, 1);
  toast.dismiss();
});

test("toast durations are configured centrally with more reading time for errors", () => {
  assert.ok(toastConfig.successDurationMs > 0);
  assert.ok(toastConfig.errorDurationMs >= toastConfig.successDurationMs);
  assert.ok(Object.isFrozen(toastConfig));
});


test("success toast keeps the same notification while moving into and out of a native dialog", () => {
  const dialog = { name: "checkout-dialog" };
  const release = registerToastHost(dialog);
  try {
    const id = toast.success("Đặt hàng thành công. Bạn thanh toán khi nhận hàng.");
    assert.equal(getToastHost(), dialog);
    assert.equal(getToast().id, id);
    assert.equal(getToast().type, "success");
    release();
    assert.equal(getToastHost(), null);
    assert.equal(getToast().id, id);
  } finally { release(); toast.dismiss(); }
});

test("an old dialog cleanup cannot remove the active toast host of a newer dialog", () => {
  const first = registerToastHost({ name: "first" });
  const active = { name: "second" };
  const second = registerToastHost(active);
  first();
  assert.equal(getToastHost(), active);
  second();
  assert.equal(getToastHost(), null);
});

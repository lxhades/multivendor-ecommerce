import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useRef, useState } from "react";
import { changePassword } from "../../api/auth/authApi.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { toast } from "../common/toast.js";

export default function ChangePasswordForm() {
  const [pending, setPending] = useState(false);
  const requestRef = useRef(null);
  const lock = useRef(false);
  useEffect(() => () => requestRef.current?.abort(), []);
  async function submit(event) {
    event.preventDefault();
    if (lock.current) return;
    const form = event.currentTarget;
    const data = new FormData(form);
    const oldPassword = String(data.get("oldPassword") ?? "");
    const newPassword = String(data.get("newPassword") ?? "");
    if ([oldPassword, newPassword].some((value) => !value.trim() || value.length < 8 || value.length > 20)) {
      toast.error("Mật khẩu hiện tại và mật khẩu mới phải từ 8 đến 20 ký tự."); return;
    }
    if (newPassword !== data.get("confirmation")) { toast.error("Mật khẩu xác nhận không khớp."); return; }
    if (newPassword === oldPassword) { toast.error("Mật khẩu mới phải khác mật khẩu hiện tại."); return; }
    const request = new AbortController(); requestRef.current = request;
    lock.current = true; setPending(true);
    try {
      await changePassword({ oldPassword, newPassword }, { signal: request.signal });
      if (!request.signal.aborted) { form.reset(); toast.success("Đổi mật khẩu thành công."); }
    } catch (failure) {
      if (!request.signal.aborted) toast.error(authErrorMessage(failure, "Không đổi được mật khẩu."));
    } finally {
      lock.current = false;
      if (!request.signal.aborted) setPending(false);
      requestRef.current = null;
    }
  }
  return <section aria-labelledby="password-title" className="rounded-2xl border border-border bg-card p-5 sm:p-7">
    <h2 id="password-title" className="text-lg font-bold">Đổi mật khẩu</h2>
    <p className="mt-1 text-sm text-muted-foreground">Mật khẩu gồm 8–20 ký tự.</p>
    <form onSubmit={submit} className="mt-5" aria-busy={pending}>
      <fieldset disabled={pending} className="grid gap-4 sm:max-w-lg">
        {[["oldPassword", "Mật khẩu hiện tại", "current-password"], ["newPassword", "Mật khẩu mới", "new-password"], ["confirmation", "Xác nhận mật khẩu mới", "new-password"]].map(([name, label, autoComplete]) =>
          <label key={name} className="grid gap-2 text-sm font-semibold text-foreground">{label}
            <Input type="password" name={name} autoComplete={autoComplete} required minLength={8} maxLength={20} className="min-h-12 w-full rounded-lg border border-input px-3 disabled:bg-background" />
          </label>)}
        <Button variant="default" type="submit" className="min-h-11 w-fit rounded-lg bg-primary px-5 text-sm font-bold text-white hover:bg-primary/90 disabled:opacity-50">{pending ? "Đang cập nhật…" : "Đổi mật khẩu"}</Button>
      </fieldset>
    </form>
  </section>;
}

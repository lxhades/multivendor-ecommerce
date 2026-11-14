import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useRef, useState } from "react";
import { forgotPassword } from "../../api/auth/authApi.js";
import { authErrorMessage } from "./authHelpers.js";
import { toast } from "../common/toast.js";

export default function ForgotPasswordForm({ initialEmail = "", onBack }) {
  const [pending, setPending] = useState(false);
  const [sent, setSent] = useState(false);
  const requestRef = useRef(null);
  const lock = useRef(false);
  useEffect(() => () => requestRef.current?.abort(), []);
  async function submit(event) {
    event.preventDefault();
    if (lock.current || sent) return;
    const email = String(new FormData(event.currentTarget).get("email") ?? "").trim();
    if (email.length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { toast.error("Nhập email hợp lệ."); return; }
    const request = new AbortController(); requestRef.current = request;
    lock.current = true; setPending(true);
    try {
      await forgotPassword(email, { signal: request.signal });
      if (!request.signal.aborted) { setSent(true); toast.success("Yêu cầu đặt lại mật khẩu đã được xử lý. Hãy kiểm tra email."); }
    } catch (failure) { if (!request.signal.aborted) toast.error(authErrorMessage(failure, "Không gửi được yêu cầu đặt lại mật khẩu.")); }
    finally { lock.current = false; if (!request.signal.aborted) setPending(false); requestRef.current = null; }
  }
  return <div className="grid gap-4">
    <p className="text-sm leading-7 text-muted-foreground">Nhập email đã đăng ký. Hệ thống sẽ đặt lại mật khẩu và gửi thông tin đăng nhập mới qua email.</p>
    <form onSubmit={submit} aria-busy={pending} className="grid gap-4"><fieldset disabled={pending || sent} className="grid gap-4"><label className="grid gap-2 text-sm font-semibold">Email<Input type="email" name="email" defaultValue={initialEmail} autoComplete="email" required maxLength={100} className="min-h-12 min-w-0 rounded-lg border border-input px-3" /></label><Button variant="default" type="submit" className="min-h-12 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">{pending ? "Đang gửi yêu cầu…" : "Đặt lại mật khẩu"}</Button></fieldset></form>
    {sent && <p role="status" className="rounded-xl bg-green-50 p-4 text-sm text-green-800">Hãy kiểm tra hộp thư và thư rác, sau đó đăng nhập bằng mật khẩu mới. Không cần gửi lại yêu cầu ngay.</p>}
    <Button variant="outline" type="button" disabled={pending} onClick={onBack} className="min-h-11 text-sm font-semibold text-primary underline">Quay lại đăng nhập</Button>
  </div>;
}

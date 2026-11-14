import { Field, FieldGroup, FieldLabel, FieldSet } from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "../common/toast.js";
import { useEffect, useId, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { registerBuyer } from "../../api/auth/authApi.js";
import { authErrorMessage, readAuthInput } from "./authHelpers.js";

const authFormStyles = {
  form:"grid gap-5",
  group:"gap-5",
  password:"relative block",
  passwordInput:"min-h-12 pr-16",
  input:"min-h-12",
  reveal:"absolute inset-y-1 right-1 min-h-0 px-2 text-xs",
  submit:"mt-1 min-h-12 w-full",
  forgot:"min-h-11 w-fit justify-self-end text-sm",
  note:"text-center text-sm leading-7 text-muted-foreground",
  link:"inline-flex min-h-11 items-center font-semibold text-primary hover:underline",
};

export default function AuthForm({ register = false, initialEmail = "", onLogin, onRegistered, returnToCheckout = false, onForgotPassword }) {
  const fieldId = useId();
  const [pending, setPending] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const controller = useRef(null);
  const lock = useRef(false);
  useEffect(() => () => controller.current?.abort(), []);

  async function submit(event) {
    event.preventDefault();
    if (lock.current) return;
    let input;
    try { input = readAuthInput(new FormData(event.currentTarget), register); }
    catch (failure) { toast.error(failure.message); return; }
    lock.current = true;
    setPending(true);
    const request = new AbortController();
    controller.current = request;
    try {
      if (register) {
        await registerBuyer(input, { signal: request.signal });
        if (!request.signal.aborted) {
          toast.success("Đăng ký thành công. Bạn có thể đăng nhập bằng tài khoản vừa tạo.");
          onRegistered(input.email);
        }
      } else await onLogin(input, { signal: request.signal });
    } catch (failure) {
      if (!request.signal.aborted) toast.error(authErrorMessage(failure));
    } finally {
      lock.current = false;
      if (!request.signal.aborted) setPending(false);
      controller.current = null;
    }
  }

  return <form onSubmit={submit} aria-busy={pending} className={authFormStyles.form}>
    <FieldSet disabled={pending}><FieldGroup className={authFormStyles.group}>
      <Field><FieldLabel htmlFor={`${fieldId}-email`}>Email</FieldLabel><Input id={`${fieldId}-email`} className={authFormStyles.input} name="email" type="email" autoComplete="email" required maxLength={100} defaultValue={initialEmail} placeholder="ban@example.com" /></Field>
      {register && <Field><FieldLabel htmlFor={`${fieldId}-phone`}>Số điện thoại</FieldLabel><Input id={`${fieldId}-phone`} className={authFormStyles.input} name="phone" type="tel" autoComplete="tel-national" inputMode="tel" required pattern="0[0-9]{9}" maxLength={10} title="10 chữ số, bắt đầu bằng 0" placeholder="Nhập số điện thoại của bạn" /></Field>}
      <Field><FieldLabel htmlFor={`${fieldId}-password`}>Mật khẩu</FieldLabel><span className={authFormStyles.password}><Input id={`${fieldId}-password`} className={authFormStyles.passwordInput} name="password" type={showPassword ? "text" : "password"} autoComplete={register ? "new-password" : "current-password"} required minLength={register ? 8 : undefined} maxLength={register ? 20 : undefined} placeholder={register ? "Từ 8 đến 20 ký tự" : "Nhập mật khẩu"} /><Button type="button" variant="ghost" className={authFormStyles.reveal} aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"} aria-pressed={showPassword} onClick={() => setShowPassword((value) => !value)}>{showPassword ? "Ẩn" : "Hiện"}</Button></span></Field>
      {register && <Field><FieldLabel htmlFor={`${fieldId}-confirm`}>Xác nhận mật khẩu</FieldLabel><Input id={`${fieldId}-confirm`} className={authFormStyles.input} name="passwordConfirm" type={showPassword ? "text" : "password"} autoComplete="new-password" required minLength={8} maxLength={20} placeholder="Nhập lại mật khẩu" /></Field>}
      <Button type="submit" className={authFormStyles.submit}>{pending ? "Đang xử lý…" : register ? "Tạo tài khoản" : "Đăng nhập"}</Button>
    </FieldGroup></FieldSet>
    {!register && onForgotPassword && <Button type="button" variant="link" onClick={(event) => onForgotPassword(event.currentTarget.form.elements.email.value)} className={authFormStyles.forgot}>Quên mật khẩu?</Button>}
    <p className={authFormStyles.note}>{register ? "Đã có tài khoản? " : "Chưa có tài khoản? "}<Link className={authFormStyles.link} to={register ? "/login" : "/register"} state={returnToCheckout ? { returnToCheckout:true } : null}>{register ? "Đăng nhập" : "Đăng ký ngay"}</Link></p>
  </form>;
}

import { Button } from "@/components/ui/button";
import { useState } from "react";
import ForgotPasswordForm from "../../components/auth/ForgotPasswordForm.jsx";
import { Navigate, useLocation } from "react-router-dom";
import AuthLayout from "../../components/auth/AuthLayout";
import AuthForm from "../../components/auth/AuthForm";

const loginPageStyles = {
  description: "text-sm text-muted-foreground",
  container: "grid gap-3 text-sm",
  errorDescription: "text-red-700",
  button: "min-h-11 rounded-lg border border-input px-4",
  button2: "min-h-11 rounded-lg px-4 text-primary underline",
};

export default function LoginPage({ auth }) {
  const { state } = useLocation();
  const [forgotEmail, setForgotEmail] = useState(null);
  if (auth.user) return <Navigate to={state?.returnToCheckout ? "/cart?checkout=1" : state?.returnToProfile ? "/profile" : state?.returnToOrders ? "/orders" : state?.returnToSeller ? "/seller" : state?.returnToAdmin ? "/admin" : "/"} replace />;
  if (forgotEmail !== null) return <AuthLayout title="Quên mật khẩu" description="Khôi phục quyền truy cập tài khoản qua email."><ForgotPasswordForm initialEmail={forgotEmail} onBack={() => setForgotEmail(null)} /></AuthLayout>;
  return <AuthLayout title="Đăng nhập" description="Chào mừng bạn trở lại. Đăng nhập để tiếp tục mua sắm.">
    {auth.checking || auth.signingOut ? <p role="status" className={loginPageStyles.description}>Đang xác minh phiên đăng nhập…</p> : auth.sessionError ?
      <div role="alert" className={loginPageStyles.container}><p className={loginPageStyles.errorDescription}>{auth.sessionError}</p>
        <Button variant="outline" type="button" onClick={auth.retry} className={loginPageStyles.button}>Thử lại</Button>
        <Button variant="outline" type="button" disabled={auth.signingOut} onClick={() => void auth.signOut()} className={loginPageStyles.button2}>Đăng nhập lại</Button>
      </div> : <>
        <AuthForm key={state?.email ?? "login"} initialEmail={state?.email} returnToCheckout={Boolean(state?.returnToCheckout)} onLogin={auth.signIn} onForgotPassword={setForgotEmail} />
      </>}
  </AuthLayout>;
}

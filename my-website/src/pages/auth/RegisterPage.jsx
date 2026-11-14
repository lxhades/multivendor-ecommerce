import { Button } from "@/components/ui/button";
import { Navigate, useNavigate, useLocation } from "react-router-dom";
import AuthLayout from "../../components/auth/AuthLayout";
import AuthForm from "../../components/auth/AuthForm";

const registerPageStyles = {
  description: "text-sm text-muted-foreground",
  container: "grid gap-3 text-sm",
  errorDescription: "text-red-700",
  button: "min-h-11 rounded-lg border border-input px-4",
  button2: "min-h-11 rounded-lg px-4 text-primary underline",
};

export default function RegisterPage({ auth }) {
  const navigate = useNavigate();
  const { state } = useLocation();
  if (auth.user) return <Navigate to={state?.returnToCheckout ? "/cart?checkout=1" : "/"} replace />;
  return <AuthLayout title="Tạo tài khoản" description="Đăng ký tài khoản người mua để bắt đầu cùng Market Lane.">
    {auth.checking || auth.signingOut ? <p role="status" className={registerPageStyles.description}>Đang xác minh phiên đăng nhập…</p> : auth.sessionError ?
      <div role="alert" className={registerPageStyles.container}><p className={registerPageStyles.errorDescription}>{auth.sessionError}</p>
        <Button variant="outline" type="button" onClick={auth.retry} className={registerPageStyles.button}>Thử lại</Button>
        <Button variant="outline" type="button" disabled={auth.signingOut} onClick={() => void auth.signOut()} className={registerPageStyles.button2}>Đăng xuất phiên cũ</Button>
      </div> : <AuthForm register returnToCheckout={Boolean(state?.returnToCheckout)} onRegistered={(email) => navigate("/login", { replace: true, state: { registered: true, email, returnToCheckout: Boolean(state?.returnToCheckout) } })} />}
  </AuthLayout>;
}

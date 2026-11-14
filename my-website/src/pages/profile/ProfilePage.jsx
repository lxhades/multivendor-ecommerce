import { Button } from "@/components/ui/button";
import { Link, Navigate, useOutletContext } from "react-router-dom";
import ChangePasswordForm from "../../components/profile/ChangePasswordForm.jsx";
import ProfileInfo from "../../components/profile/ProfileInfo.jsx";
import ProfileAddresses from "../../components/profile/ProfileAddresses.jsx";

const profilePageStyles = {
  description: "mx-auto max-w-5xl px-4 py-12 text-sm text-muted-foreground",
  page: "mx-auto grid max-w-5xl justify-items-start gap-3 px-4 py-12 text-sm",
  errorDescription: "text-red-700",
  button: "min-h-11 rounded-lg border border-input px-4",
  button2: "min-h-11 text-primary underline",
  page2: "mx-auto grid w-full max-w-5xl gap-8 px-4 py-10 sm:px-6 lg:px-8",
  container: "flex flex-wrap items-start justify-between gap-3",
  description2: "text-sm font-semibold text-primary",
  heading: "mt-1 text-2xl font-extrabold text-foreground sm:text-3xl",
  description3: "mt-2 text-sm text-muted-foreground",
  link: "inline-flex min-h-11 items-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground hover:border-primary/60 hover:text-primary",
};

export default function ProfilePage() {
  const { auth } = useOutletContext();
  if (auth.checking || auth.signingOut) return <p role="status" className={profilePageStyles.description}>Đang tải hồ sơ cá nhân…</p>;
  if (auth.sessionError) return <div role="alert" className={profilePageStyles.page}>
    <p className={profilePageStyles.errorDescription}>{auth.sessionError}</p>
    <Button variant="outline" type="button" onClick={auth.retry} className={profilePageStyles.button}>Thử lại</Button>
    <Button variant="outline" type="button" onClick={() => void auth.signOut()} className={profilePageStyles.button2}>Đăng nhập lại</Button>
  </div>;
  if (!auth.user) return <Navigate to="/login" state={{ returnToProfile: true }} replace />;
  return <div className={profilePageStyles.page2}>
    <div className={profilePageStyles.container}>
      <div>
        <p className={profilePageStyles.description2}>Tài khoản của bạn</p>
        <h1 className={profilePageStyles.heading}>Hồ sơ cá nhân</h1>
        <p className={profilePageStyles.description3}>Thông tin tài khoản và địa chỉ nhận hàng của bạn.</p>
      </div>
      <Link to="/" className={profilePageStyles.link}>Tiếp tục mua sắm</Link>
    </div>
    <ProfileInfo user={auth.user} />
    <ProfileAddresses key={auth.user.userId} userId={auth.user.userId} />
    <ChangePasswordForm key={`password:${auth.user.userId}`} />
  </div>;
}

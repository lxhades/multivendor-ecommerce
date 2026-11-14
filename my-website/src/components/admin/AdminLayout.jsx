import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Link, Navigate, Outlet, useOutletContext } from "react-router-dom";
import AdminSidebar from "./AdminSidebar.jsx";

const adminLayoutStyles = {
  page:"mx-auto grid w-full max-w-7xl gap-8 px-4 py-8 sm:px-6 lg:px-8 lg:py-10",
  header:"flex flex-wrap items-end justify-between gap-5 border-b border-border pb-7",
  title:"mt-4 text-3xl font-extrabold tracking-tight sm:text-4xl",
  description:"mt-3 text-sm leading-7 text-muted-foreground",
  account:"max-w-full break-all rounded-full border border-border bg-card px-4 py-2.5 text-xs text-muted-foreground",
  columns:"grid min-w-0 items-start gap-7 lg:grid-cols-[250px_minmax(0,1fr)]",
  content:"min-w-0",
};

export default function AdminLayout() {
  const context = useOutletContext();
  const { auth } = context;
  if (auth.checking || auth.signingOut) return <p role="status" className="mx-auto max-w-7xl px-4 py-12 text-sm text-muted-foreground">Đang xác minh quyền quản trị…</p>;
  if (auth.sessionError) return <div role="alert" className="mx-auto grid max-w-7xl justify-items-start gap-3 px-4 py-12 text-sm"><p className="text-red-700">{auth.sessionError}</p><Button variant="outline" type="button" onClick={auth.retry} className="min-h-11 rounded-lg border border-input px-4">Thử lại</Button><Button variant="outline" type="button" onClick={() => void auth.signOut()} className="min-h-11 text-primary underline">Đăng nhập lại</Button></div>;
  if (!auth.user) return <Navigate to="/login" state={{ returnToAdmin: true }} replace />;
  if (auth.user.role !== "ADMIN") return <section className="mx-auto grid max-w-7xl justify-items-start gap-3 px-4 py-12"><h1 className="text-2xl font-bold">Không có quyền truy cập</h1><p className="text-sm text-muted-foreground">Khu vực này chỉ dành cho tài khoản quản trị viên.</p><Link to="/" className="inline-flex min-h-11 items-center rounded-lg border border-border bg-card px-4 text-sm font-semibold">Về trang chủ</Link></section>;
  return <div className={adminLayoutStyles.page}><header className={adminLayoutStyles.header}><div><Badge variant="outline">Khu vực quản trị</Badge><h1 className={adminLayoutStyles.title}>Quản lý khu chợ.</h1><p className={adminLayoutStyles.description}>Từ tài khoản, cửa hàng đến từng đơn mua trên Market Lane.</p></div><p className={adminLayoutStyles.account}>{auth.user.email}</p></header><div className={adminLayoutStyles.columns}><AdminSidebar /><div className={adminLayoutStyles.content}><Outlet context={context} /></div></div></div>;
}

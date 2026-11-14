import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Link, Navigate, Outlet, useOutletContext } from "react-router-dom";
import SellerSidebar from "./SellerSidebar.jsx";

const sellerLayoutStyles = {
  page: "mx-auto grid w-full max-w-7xl gap-8 px-4 py-8 sm:px-6 lg:px-8 lg:py-10",
  header: "flex flex-wrap items-end justify-between gap-5 border-b border-border pb-7",
  title: "mt-4 text-3xl font-extrabold tracking-tight sm:text-4xl",
  description: "mt-3 text-sm leading-7 text-muted-foreground",
  account: "max-w-full break-all rounded-full border border-border bg-card px-4 py-2.5 text-xs text-muted-foreground",
  columns: "grid min-w-0 items-start gap-7 lg:grid-cols-[250px_minmax(0,1fr)]",
  content: "min-w-0",
};

export default function SellerLayout() {
  const context = useOutletContext();
  const { auth } = context;
  if (auth.checking || auth.signingOut) return <p role="status" className="mx-auto max-w-7xl px-4 py-12 text-sm text-muted-foreground">Đang xác minh tài khoản người bán…</p>;
  if (auth.sessionError) return <div role="alert" className="mx-auto grid max-w-7xl justify-items-start gap-3 px-4 py-12 text-sm">
    <p className="text-red-700">{auth.sessionError}</p>
    <Button variant="outline" type="button" onClick={auth.retry} className="min-h-11 rounded-lg border border-input px-4">Thử lại</Button>
    <Button variant="outline" type="button" onClick={() => void auth.signOut()} className="min-h-11 text-primary underline">Đăng nhập lại</Button>
  </div>;
  if (!auth.user) return <Navigate to="/login" state={{ returnToSeller: true }} replace />;
  if (auth.user.role !== "SELLER") return <section className="mx-auto grid max-w-7xl justify-items-start gap-3 px-4 py-12">
    <h1 className="text-2xl font-bold">Không có quyền truy cập</h1>
    <p className="text-sm text-muted-foreground">Khu vực quản lý cửa hàng dành cho tài khoản người bán.</p>
    <Link to="/" className="inline-flex min-h-11 items-center rounded-lg border border-border bg-card px-4 text-sm font-semibold">Về trang chủ</Link>
  </section>;
  return <div className={sellerLayoutStyles.page}>
    <header className={sellerLayoutStyles.header}><div><Badge variant="outline">Kênh người bán</Badge><h1 className={sellerLayoutStyles.title}>Góc cửa hàng của bạn.</h1><p className={sellerLayoutStyles.description}>Một nơi cho sản phẩm, đơn hàng và những kế hoạch tiếp theo.</p></div><p className={sellerLayoutStyles.account}>{auth.user.email}</p></header>
    <div className={sellerLayoutStyles.columns}><SellerSidebar /><div className={sellerLayoutStyles.content}><Outlet context={context} /></div></div>
  </div>;
}

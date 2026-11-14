import { Link } from "react-router-dom";
import { UsersRound, Store, Shapes, PackageCheck, ReceiptText, ChartNoAxesCombined, ArrowUpRight } from "lucide-react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
const sections = [
  ["/admin/users","Người dùng","Tra cứu tài khoản, sửa thông tin và quản lý trạng thái truy cập.",UsersRound],
  ["/admin/shops","Cửa hàng","Xem hồ sơ, sản phẩm và điều chỉnh trạng thái hoạt động của shop.",Store],
  ["/admin/categories","Danh mục","Sắp xếp danh mục cha/con, cập nhật hình ảnh và trạng thái hiển thị.",Shapes],
  ["/admin/products","Duyệt sản phẩm","Kiểm tra sản phẩm, biến thể và quyết định duyệt hoặc từ chối.",PackageCheck],
  ["/admin/orders","Đơn hàng","Tìm kiếm, lọc và xem thông tin đơn hàng trên toàn hệ thống.",ReceiptText],
  ["/admin/reports","Đối soát doanh thu","Xem doanh thu theo kỳ, cửa hàng và các đơn cấu thành số liệu.",ChartNoAxesCombined],
];
const adminHomePageStyles = {
  badge: "w-fit",
  icon2: "size-5",
  cardDescription: "sr-only",
  arrowUpRight: "size-4",

  page:"grid min-w-0 gap-7",
  intro:"grid gap-3 rounded-3xl border border-border bg-card p-6 sm:p-8",
  title:"text-2xl font-extrabold tracking-tight",
  description:"max-w-2xl text-sm leading-7 text-muted-foreground",
  grid:"grid gap-4 sm:grid-cols-2 xl:gap-5",
  card:"group h-full gap-5 rounded-2xl shadow-none transition-colors hover:border-primary/50",
  header:"gap-3",
  icon:"grid size-11 place-items-center rounded-2xl bg-accent text-primary",
  cardTitle:"text-xl font-bold tracking-tight",
  content:"flex-1 text-sm leading-7 text-muted-foreground",
  footer:"justify-between border-t border-border/60 pt-4 text-sm font-semibold text-primary",
};
export default function AdminHomePage() {
  return <section className={adminHomePageStyles.page}><div className={adminHomePageStyles.intro}><Badge variant="secondary" className={adminHomePageStyles.badge}>Admin Studio</Badge><h2 className={adminHomePageStyles.title}>Mọi hoạt động, cùng một góc nhìn.</h2><p className={adminHomePageStyles.description}>Chọn một khu vực để bắt đầu. Dữ liệu tài khoản, cửa hàng, đơn hàng và doanh thu được tải tại từng trang quản lý.</p></div><div className={adminHomePageStyles.grid}>{sections.map(([to,title,description,Icon]) => <Link key={to} to={to}><Card className={adminHomePageStyles.card}><CardHeader className={adminHomePageStyles.header}><span className={adminHomePageStyles.icon}><Icon aria-hidden="true" className={adminHomePageStyles.icon2} /></span><CardTitle className={adminHomePageStyles.cardTitle}><h3>{title}</h3></CardTitle><CardDescription className={adminHomePageStyles.cardDescription}>Mở khu vực {title.toLowerCase()}</CardDescription></CardHeader><CardContent className={adminHomePageStyles.content}>{description}</CardContent><CardFooter className={adminHomePageStyles.footer}>Mở quản lý <ArrowUpRight className={adminHomePageStyles.arrowUpRight} aria-hidden="true" /></CardFooter></Card></Link>)}</div></section>;
}

import { NavLink } from "react-router-dom";
import { LayoutDashboard, UsersRound, Store, Shapes, PackageCheck, ReceiptText, ChartNoAxesCombined, ShieldCheck } from "lucide-react";
import { cn } from "@/lib/utils";
const links = [
  ["/admin", "Tổng quan", LayoutDashboard, true],
  ["/admin/users", "Người dùng", UsersRound],
  ["/admin/shops", "Cửa hàng", Store],
  ["/admin/categories", "Danh mục", Shapes],
  ["/admin/products", "Duyệt sản phẩm", PackageCheck],
  ["/admin/orders", "Đơn hàng", ReceiptText],
  ["/admin/reports", "Đối soát doanh thu", ChartNoAxesCombined],
];
const adminSidebarStyles = {
  root:"self-start overflow-hidden rounded-3xl bg-night p-4 text-white lg:sticky lg:top-28",
  heading:"flex items-center gap-3 border-b border-white/10 px-3 pb-5 pt-2",
  mark:"grid size-9 place-items-center rounded-xl bg-pumpkin text-night",
  title:"text-sm font-bold",
  caption:"mt-1 text-[11px] text-white/45",
  navigation:"mt-4 grid gap-2 sm:grid-cols-2 lg:grid-cols-1",
  link:"flex min-h-12 items-center gap-3 rounded-xl px-3 text-sm font-semibold transition focus-visible:outline-2 focus-visible:outline-pumpkin",
  active:"bg-pumpkin text-night",
  idle:"text-white/60 hover:bg-white/10 hover:text-white",
  icon:"size-4 shrink-0",
  note:"mt-5 hidden border-t border-white/10 px-3 pt-5 text-xs leading-6 text-white/40 lg:block",
};
export default function AdminSidebar() {
  return <aside className={adminSidebarStyles.root}><div className={adminSidebarStyles.heading}><span className={adminSidebarStyles.mark}><ShieldCheck className="size-5" aria-hidden="true" /></span><div><p className={adminSidebarStyles.title}>Admin Studio</p><p className={adminSidebarStyles.caption}>Market Lane · Quản trị hệ thống</p></div></div><nav aria-label="Điều hướng quản trị" className={adminSidebarStyles.navigation}>{links.map(([to,label,Icon,end]) => <NavLink key={to} to={to} end={Boolean(end)} className={({isActive}) => cn(adminSidebarStyles.link,isActive ? adminSidebarStyles.active : adminSidebarStyles.idle)}><Icon className={adminSidebarStyles.icon} aria-hidden="true" />{label}</NavLink>)}</nav><p className={adminSidebarStyles.note}>Theo dõi hoạt động, xét duyệt nội dung và quản lý khu chợ từ một nơi.</p></aside>;
}

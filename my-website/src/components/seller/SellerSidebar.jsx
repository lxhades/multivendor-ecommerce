import { NavLink } from "react-router-dom";
import { Store, Package, ReceiptText, ChartNoAxesCombined, Ghost } from "lucide-react";
import { cn } from "@/lib/utils";
const sections = [
  ["/seller/shop", "Thông tin cửa hàng", Store],
  ["/seller/products", "Sản phẩm & tồn kho", Package],
  ["/seller/orders", "Đơn hàng", ReceiptText],
  ["/seller/reports", "Thống kê", ChartNoAxesCombined],
];
const sidebarStyles = {
  root: "self-start overflow-hidden rounded-3xl bg-night p-4 text-white lg:sticky lg:top-28",
  heading: "flex items-center gap-3 border-b border-white/10 px-3 pb-5 pt-2",
  mark: "grid size-9 place-items-center rounded-xl bg-pumpkin text-night",
  title: "text-sm font-bold",
  caption: "mt-1 text-[11px] text-white/45",
  navigation: "mt-4 grid gap-2 sm:grid-cols-2 lg:grid-cols-1",
  link: "flex min-h-12 items-center gap-3 rounded-xl px-3 text-sm font-semibold transition focus-visible:outline-2 focus-visible:outline-pumpkin",
  active: "bg-pumpkin text-night",
  idle: "text-white/60 hover:bg-white/10 hover:text-white",
  note: "mt-5 hidden border-t border-white/10 px-3 pt-5 text-xs leading-6 text-white/40 lg:block",
};
export default function SellerSidebar() {
  return <aside className={sidebarStyles.root}><div className={sidebarStyles.heading}><span className={sidebarStyles.mark}><Ghost className="size-5" aria-hidden="true" /></span><div><p className={sidebarStyles.title}>Seller Studio</p><p className={sidebarStyles.caption}>Market Lane · Halloween edition</p></div></div><nav aria-label="Điều hướng quản lý cửa hàng" className={sidebarStyles.navigation}>{sections.map(([to,label,Icon]) => <NavLink key={to} to={to} className={({isActive}) => cn(sidebarStyles.link,isActive ? sidebarStyles.active : sidebarStyles.idle)}><Icon className="size-4 shrink-0" aria-hidden="true" />{label}</NavLink>)}</nav><p className={sidebarStyles.note}>Chăm chút cửa hàng, cập nhật sản phẩm và theo dõi từng đơn mua.</p></aside>;
}

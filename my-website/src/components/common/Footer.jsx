import { Link } from "react-router-dom";
import { Ghost, ArrowUpRight, ShoppingBag } from "lucide-react";
import { Button } from "@/components/ui/button";

const footerStyles = {
  root: "mt-16 bg-night text-white",
  inner: "mx-auto grid max-w-7xl gap-10 px-4 py-12 sm:px-6 md:grid-cols-[1.5fr_1fr_1fr] lg:px-8 lg:py-16",
  brand: "flex items-center gap-3 text-2xl font-extrabold tracking-tight",
  mark: "grid size-10 place-items-center rounded-xl bg-pumpkin text-night",
  copy: "mt-5 max-w-sm text-sm leading-7 text-white/55",
  heading: "mb-4 text-sm font-semibold text-white/85",
  links: "grid content-start justify-items-start gap-2 text-sm text-white/60",
  link: "inline-flex min-h-10 items-center gap-2 transition hover:text-white",
  bottom: "mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4 border-t border-white/10 px-4 py-5 text-xs text-white/45 sm:px-6 lg:px-8",
};
export default function Footer() {
  return <footer id="site-footer" className={footerStyles.root}><div className={footerStyles.inner}>
    <div><Link to="/" className={footerStyles.brand}><span className={footerStyles.mark}><ShoppingBag aria-hidden="true" className="size-5" /></span>Market Lane.</Link><p className={footerStyles.copy}>Một nơi để khám phá những cửa hàng mới, chọn món đồ yêu thích và mua sắm theo cách của bạn.</p></div>
    <nav aria-label="Khám phá" className={footerStyles.links}><h2 className={footerStyles.heading}>Dạo quanh khu chợ</h2><Link to="/search" className={footerStyles.link}>Tất cả sản phẩm <ArrowUpRight aria-hidden="true" className="size-3.5" /></Link><Link to="/shops" className={footerStyles.link}>Các cửa hàng</Link><Link to="/orders" className={footerStyles.link}>Đơn hàng của tôi</Link><Link to="/profile" className={footerStyles.link}>Tài khoản & địa chỉ</Link></nav>
    <div><h2 className={footerStyles.heading}>Một mùa mua sắm khác biệt</h2><p className={footerStyles.copy}>Chọn đúng phân loại, kiểm tra thông tin và xác nhận trước khi đặt hàng. Một chút Halloween, thật nhiều lựa chọn.</p><Button asChild variant="secondary" className="mt-4"><a href="#main-content">Về đầu trang <Ghost data-icon="inline-end" aria-hidden="true" /></a></Button></div>
  </div><div className={footerStyles.bottom}><p>© {new Date().getFullYear()} Market Lane.</p><p>Halloween edition · Made for everyday discoveries</p></div></footer>;
}

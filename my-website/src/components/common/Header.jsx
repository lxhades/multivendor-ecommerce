import { Ghost, ShoppingBag, Store, ArrowUpRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import HeaderSearch from "../search/HeaderSearch.jsx";
import AccountMenu from "../auth/AccountMenu";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";

const headerStyles = {
  skip: "sr-only focus:fixed focus:left-3 focus:top-3 focus:z-[100] focus:rounded-lg focus:bg-card focus:p-4 focus:text-foreground",
  ribbon: "bg-night text-white",
  ribbonInner: "mx-auto flex max-w-7xl items-center justify-between gap-3 px-4 py-2.5 text-xs sm:px-6 lg:px-8",
  season: "flex items-center gap-2 text-white/85",
  ribbonLink: "flex items-center gap-1.5 text-white/70 transition hover:text-white",
  header: "sticky top-0 z-40 border-b border-border bg-card/95 backdrop-blur-xl",
  inner: "mx-auto grid max-w-7xl grid-cols-[1fr_auto] items-center gap-4 px-4 py-4 sm:px-6 lg:grid-cols-[auto_minmax(0,1fr)_auto] lg:gap-8 lg:px-8",
  brand: "flex w-fit items-center gap-2.5 rounded-lg",
  mark: "grid size-11 shrink-0 place-items-center rounded-2xl bg-pumpkin text-night -rotate-6",
  brandName: "block text-xl font-extrabold tracking-tight sm:text-2xl",
  brandDot: "text-primary",
  tagline: "mt-0.5 hidden text-[11px] text-muted-foreground sm:block",
  search: "order-3 col-span-2 min-w-0 lg:order-none lg:col-span-1",
  actions: "flex items-center justify-end gap-2",
  shopButton: "hidden lg:inline-flex",
  cartButton: "gap-2 rounded-xl",
};

export default function Header({ auth, cart }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [searchParams] = useSearchParams();
  
  const keyword = searchParams.get("q") ?? "";

  function handleSearch(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const query = String(formData.get("q") ?? "").trim();

    const next = new URLSearchParams();
    if (pathname === "/search" || pathname.startsWith("/search/danh-muc/")) {
      for (const name of ["categoryId", "shopId", "minPrice", "maxPrice", "sort", "size", "includeChildren"]) {
        if (searchParams.has(name)) next.set(name, searchParams.get(name));
      }
    }
    if (query) next.set("q", query);
    else next.delete("q");
    next.delete("page");
    if (next.get("includeChildren") === "true") next.delete("includeChildren");
    const path = pathname.startsWith("/search/danh-muc/") ? pathname : "/search";
    navigate(`${path}${next.size ? `?${next}` : ""}`);
  }

  return <>
    <a href="#main-content" className={headerStyles.skip}>Đến nội dung chính</a>
    <div className={headerStyles.ribbon}><div className={headerStyles.ribbonInner}><span className={headerStyles.season}><Ghost aria-hidden="true" className="size-3.5" /> Halloween tại Market Lane</span><Link to="/shops" className={headerStyles.ribbonLink}>Ghé thăm các cửa hàng <ArrowUpRight aria-hidden="true" className="size-3.5" /></Link></div></div>
    <header className={headerStyles.header}>
      <div className={headerStyles.inner}>
        <Link to="/" aria-label="Market Lane — Trang chủ" className={headerStyles.brand}><span className={headerStyles.mark}><ShoppingBag aria-hidden="true" className="size-6" /></span><span><span className={headerStyles.brandName}>Market Lane<span className={headerStyles.brandDot}>.</span></span><span className={headerStyles.tagline}>Một góc chợ, nhiều điều hay.</span></span></Link>
        <div className={headerStyles.search}><HeaderSearch key={`${pathname}:${keyword}`} initialKeyword={keyword} onSearch={handleSearch} /></div>
        <div className={headerStyles.actions}>
          <Button asChild variant="ghost" className={headerStyles.shopButton}><Link to="/shops"><Store data-icon="inline-start" aria-hidden="true" />Cửa hàng</Link></Button>
          <Button asChild variant="outline" className={headerStyles.cartButton}><Link to="/cart" aria-label={`Giỏ hàng${cart.ready ? `, ${cart.totalQuantity} sản phẩm` : ""}`}><ShoppingBag data-icon="inline-start" aria-hidden="true" /><span className="hidden sm:inline">Giỏ hàng</span>{cart.ready && <Badge variant="secondary" className="min-w-5 justify-center">{cart.totalQuantity > 99 ? "99+" : cart.totalQuantity}</Badge>}</Link></Button>
          <AccountMenu auth={auth} />
        </div>
      </div>
    </header>
  </>;
}

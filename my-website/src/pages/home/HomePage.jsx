import { Link, Navigate, useSearchParams } from "react-router-dom";
import { ArrowUpRight, Ghost, Store, ShoppingBag } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import CategoryList from "../../components/category/CategoryList";
import ProductListPage from "../product/ProductListPage";
import SeasonalEntrance from "../../components/common/SeasonalEntrance.jsx";

const homePageStyles = {
  detail: "size-4",
  detail2: "text-2xl font-bold tracking-tight",
  detail3: "mt-2 max-w-xl text-sm leading-7 opacity-65",

  page: "mx-auto grid w-full max-w-7xl gap-10 px-4 py-6 sm:px-6 lg:gap-14 lg:px-8 lg:py-8",
  hero: "grid overflow-hidden rounded-[2rem] border border-border bg-card lg:min-h-[490px] lg:grid-cols-[1.05fr_1fr]",
  copy: "flex flex-col items-start justify-center gap-6 p-7 sm:p-10 lg:p-12",
  title: "market-display max-w-xl text-5xl sm:text-6xl xl:text-7xl",
  description: "max-w-md text-base leading-7 text-muted-foreground",
  actions: "flex flex-wrap gap-3",
  image: "h-full min-h-64 w-full object-cover sm:min-h-80",
  imageWrap: "relative overflow-hidden bg-pumpkin lg:rounded-l-[4rem]",
  note: "flex flex-wrap items-center gap-6 border-t border-border pt-5 text-xs text-muted-foreground",
  noteItem: "flex items-center gap-2",
  section: "grid gap-6",
  sectionHeader: "flex flex-wrap items-end justify-between gap-4",
  sectionTitle: "text-2xl font-extrabold tracking-tight sm:text-3xl",
  sectionCopy: "mt-2 text-sm text-muted-foreground",
  browse: "flex min-h-11 items-center gap-2 text-sm font-semibold text-primary",
  banner: "flex flex-wrap items-center justify-between gap-6 rounded-3xl bg-secondary px-7 py-8 text-secondary-foreground sm:px-10",
};
export default function HomePage() {
  const [params] = useSearchParams();
  if (["q", "categoryId", "shopId", "minPrice", "maxPrice"].some((name) => params.has(name))) return <Navigate to={`/search?${params}`} replace />;
  return <div className={homePageStyles.page}><SeasonalEntrance>
    <section className={homePageStyles.hero}>
      <div className={homePageStyles.copy} data-season-copy><Badge variant="secondary"><Ghost data-icon="inline-start" aria-hidden="true" />Halloween edition</Badge><h1 className={homePageStyles.title}>Ghé chợ chơi.<br />Rinh điều hay.</h1><p className={homePageStyles.description}>Dạo một vòng Market Lane. Từ món đồ thường ngày đến lựa chọn mới cho mùa Halloween, tìm điều hợp với bạn từ các cửa hàng.</p><div className={homePageStyles.actions}><Button asChild size="lg"><Link to="/search">Khám phá sản phẩm <ArrowUpRight data-icon="inline-end" aria-hidden="true" /></Link></Button><Button asChild variant="outline" size="lg"><Link to="/shops">Ghé các cửa hàng</Link></Button></div><div className={homePageStyles.note}><span className={homePageStyles.noteItem}><Store className={homePageStyles.detail} aria-hidden="true" />Nhiều cửa hàng, một điểm đến</span><span className={homePageStyles.noteItem}><ShoppingBag className={homePageStyles.detail} aria-hidden="true" />Chọn theo cách của bạn</span></div></div>
      <div className={homePageStyles.imageWrap} data-season-art><img src="/halloween-market.svg" alt="Bí ngô Halloween và chú ma nhỏ trên nền cam" width="700" height="620" fetchPriority="high" className={homePageStyles.image} /></div>
    </section>
  </SeasonalEntrance><section className={homePageStyles.section}><div><h2 className={homePageStyles.sectionTitle}>Bạn muốn tìm gì hôm nay?</h2><p className={homePageStyles.sectionCopy}>Bắt đầu từ một danh mục yêu thích.</p></div><CategoryList /></section>
  <section className={homePageStyles.section} aria-label="Khám phá sản phẩm"><div className={homePageStyles.sectionHeader}><div><h2 className={homePageStyles.sectionTitle}>Những món đáng ghé xem</h2><p className={homePageStyles.sectionCopy}>Khám phá sản phẩm đang được bán trên Market Lane.</p></div><Link to="/search" className={homePageStyles.browse}>Xem tất cả <ArrowUpRight className={homePageStyles.detail} aria-hidden="true" /></Link></div><ProductListPage /></section>
  <section className={homePageStyles.banner}><div><h2 className={homePageStyles.detail2}>Mỗi cửa hàng, một màu sắc.</h2><p className={homePageStyles.detail3}>Tìm hiểu cửa hàng và xem thêm những sản phẩm phù hợp với bạn.</p></div><Button asChild size="lg"><Link to="/shops">Dạo quanh cửa hàng <Store data-icon="inline-end" aria-hidden="true" /></Link></Button></section>
  </div>;
}

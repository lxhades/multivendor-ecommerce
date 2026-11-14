import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowUpRight, ImageOff, Store } from "lucide-react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { productPath } from "../common/catalogPaths.js";
import { isVideo, mediaUrls, money } from "./productHelpers.js";
const productCardStyles = {
  card: "group h-full min-w-0 gap-0 overflow-hidden rounded-2xl border-border py-0 shadow-none transition-colors hover:border-primary/50",
  link: "flex h-full flex-col rounded-2xl outline-offset-4",
  imageWrap: "grid aspect-[1/1.05] place-items-center overflow-hidden bg-muted/40 p-4",
  image: "size-full object-contain transition-transform duration-500 motion-safe:group-hover:scale-105",
  empty: "flex flex-col items-center gap-2 text-xs text-muted-foreground",
  header: "gap-2 px-4 pb-2 pt-4",
  shop: "flex items-center gap-1.5 truncate text-[11px]",
  title: "line-clamp-2 min-h-10 text-sm font-semibold leading-5",
  content: "flex-1 px-4 pb-2 text-xs text-muted-foreground",
  footer: "mt-auto flex items-center justify-between gap-2 px-4 pb-4 pt-2",
  price: "text-base font-extrabold tracking-tight text-foreground",
  arrow: "grid size-8 shrink-0 place-items-center rounded-full bg-accent text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground",
};
export default function ProductCard({ product }) {
  const [failed, setFailed] = useState(false);
  const image = mediaUrls(product.mediaJson).find((url) => !isVideo(url));
  return <Card className={productCardStyles.card}><Link to={productPath(product)} className={productCardStyles.link}><div className={productCardStyles.imageWrap}>{image && !failed ? <img src={image} alt={product.name} loading="lazy" decoding="async" onError={() => setFailed(true)} className={productCardStyles.image} /> : <span className={productCardStyles.empty}><ImageOff className="size-7" aria-hidden="true" />Chưa có ảnh</span>}</div><CardHeader className={productCardStyles.header}>{product.shopName && <CardDescription className={productCardStyles.shop}><Store className="size-3 shrink-0" aria-hidden="true" />{product.shopName}</CardDescription>}<CardTitle className={productCardStyles.title}><h3>{product.name}</h3></CardTitle></CardHeader><CardContent className={productCardStyles.content}>{typeof product.soldCount === "number" && product.soldCount > 0 ? `Đã bán ${product.soldCount.toLocaleString("vi-VN")}` : "Xem chi tiết sản phẩm"}</CardContent><CardFooter className={productCardStyles.footer}><p className={productCardStyles.price}>{typeof product.minPrice === "number" ? money.format(product.minPrice) : "Chưa có giá"}</p><span className={productCardStyles.arrow}><ArrowUpRight className="size-4" aria-hidden="true" /></span></CardFooter></Link></Card>;
}

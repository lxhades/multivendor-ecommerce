import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowUpRight, Store } from "lucide-react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { isMediaUrl } from "../product/productHelpers.js";
const statuses = { ACTIVE:"Đang hoạt động",CLOSED:"Đã đóng cửa",SUSPENDED:"Tạm ngưng",BANNED:"Đã khóa" };
const shopCardStyles = {
  card:"group h-full gap-0 overflow-hidden rounded-2xl py-0 shadow-none transition-colors hover:border-primary/40",
  link:"flex h-full flex-col",
  banner:"relative grid h-32 place-items-center overflow-hidden bg-secondary",
  bannerImage:"size-full object-cover",
  header:"relative gap-2 px-5 pb-3 pt-4",
  logo:"-mt-10 mb-2 grid size-16 place-items-center overflow-hidden rounded-2xl border-4 border-card bg-accent text-xl font-bold text-primary",
  image:"size-full object-cover",
  title:"line-clamp-2 text-xl font-extrabold tracking-tight",
  description:"line-clamp-2 text-xs leading-6",
  content:"flex-1 px-5 pb-3",
  footer:"flex items-center justify-between px-5 pb-5 text-xs text-muted-foreground",
};
export default function ShopCard({shop}) {
  const [logoFailed,setLogoFailed]=useState(false),[bannerFailed,setBannerFailed]=useState(false);
  return <Card className={shopCardStyles.card}><Link to={`/shops/${encodeURIComponent(shop.id)}`} className={shopCardStyles.link}><div className={shopCardStyles.banner}>{isMediaUrl(shop.bannerUrl) && !bannerFailed ? <img src={shop.bannerUrl} alt="" loading="lazy" decoding="async" onError={() => setBannerFailed(true)} className={shopCardStyles.bannerImage} /> : <Store className="size-12 text-pumpkin" aria-hidden="true" />}</div><CardHeader className={shopCardStyles.header}><span className={shopCardStyles.logo}>{isMediaUrl(shop.logoUrl) && !logoFailed ? <img src={shop.logoUrl} alt="" loading="lazy" onError={() => setLogoFailed(true)} className={shopCardStyles.image} /> : shop.name?.slice(0,1).toUpperCase()}</span><CardTitle className={shopCardStyles.title}><h2>{shop.name}</h2></CardTitle><CardDescription className={shopCardStyles.description}>{shop.description || shop.address || "Khám phá các sản phẩm của cửa hàng."}</CardDescription></CardHeader><CardContent className={shopCardStyles.content}>{shop.status && <Badge variant="outline">{statuses[shop.status] ?? shop.status}</Badge>}</CardContent><CardFooter className={shopCardStyles.footer}><span>{typeof shop.productCount === "number" ? `${shop.productCount.toLocaleString("vi-VN")} sản phẩm` : "Ghé cửa hàng"}</span><ArrowUpRight className="size-4 text-primary" aria-hidden="true" /></CardFooter></Link></Card>;
}

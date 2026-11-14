import { useState } from "react";
import { Store } from "lucide-react";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { isMediaUrl } from "../product/productHelpers.js";
const shopHeaderStyles = {
  root: "overflow-hidden rounded-3xl border border-border bg-card",
  banner: "relative h-40 overflow-hidden bg-night sm:h-52",
  bannerImage: "size-full object-cover",
  fallback: "absolute inset-0 flex items-center justify-end gap-4 px-8 text-white/25",
  fallbackText: "text-3xl font-extrabold tracking-tight sm:text-5xl",
  content: "relative px-5 pb-6 sm:px-8 sm:pb-8",
  avatar: "-mt-12 mb-5 size-24 border-4 border-card bg-accent sm:-mt-14 sm:size-28",
  avatarFallback: "bg-accent text-3xl font-extrabold text-primary",
  heading: "flex flex-wrap items-center gap-3",
  name: "min-w-0 break-words text-3xl font-extrabold tracking-tight sm:text-4xl",
  description: "mt-3 max-w-3xl whitespace-pre-line break-words text-sm leading-7 text-muted-foreground",
  facts: "mt-7 grid gap-5 border-t border-border pt-6 text-sm sm:grid-cols-2 lg:grid-cols-4",
  label: "text-xs text-muted-foreground",
  value: "mt-2 break-words font-semibold",
};
const statusLabels = { ACTIVE:"Đang hoạt động",SUSPENDED:"Tạm ngưng",CLOSED:"Đã đóng cửa",BANNED:"Đã bị khóa" };
export default function ShopHeader({shop}) {
  const [failedBanner,setFailedBanner] = useState(false);
  const createdAt = shop.createdAt ? new Date(shop.createdAt) : null;
  const facts = [["Địa chỉ",shop.address || "Chưa cập nhật"],["Sản phẩm",typeof shop.productCount === "number" ? shop.productCount.toLocaleString("vi-VN") : "Chưa cập nhật"],["Đánh giá",typeof shop.ratingAvg === "number" && shop.ratingCount > 0 ? `${shop.ratingAvg.toLocaleString("vi-VN")}/5 · ${shop.ratingCount.toLocaleString("vi-VN")} đánh giá` : "Chưa có đánh giá"],["Tham gia từ",createdAt && Number.isFinite(createdAt.getTime()) ? createdAt.toLocaleDateString("vi-VN") : "Chưa cập nhật"]];
  return <section aria-labelledby="shop-name" className={shopHeaderStyles.root}><div className={shopHeaderStyles.banner}>{isMediaUrl(shop.bannerUrl) && !failedBanner ? <img src={shop.bannerUrl} alt={`Banner ${shop.name}`} onError={() => setFailedBanner(true)} className={shopHeaderStyles.bannerImage} /> : <div aria-hidden="true" className={shopHeaderStyles.fallback}><Store className="size-16 text-pumpkin/60" /><span className={shopHeaderStyles.fallbackText}>Market Lane.</span></div>}</div><div className={shopHeaderStyles.content}><Avatar className={shopHeaderStyles.avatar}>{isMediaUrl(shop.logoUrl) && <AvatarImage src={shop.logoUrl} alt={`Logo ${shop.name}`} />}<AvatarFallback className={shopHeaderStyles.avatarFallback}>{shop.name?.charAt(0)?.toUpperCase() || "S"}</AvatarFallback></Avatar><div className={shopHeaderStyles.heading}><h1 id="shop-name" className={shopHeaderStyles.name}>{shop.name}</h1>{shop.status && <Badge variant={shop.status === "ACTIVE" ? "secondary" : "outline"}>{statusLabels[shop.status] ?? shop.status}</Badge>}</div><p className={shopHeaderStyles.description}>{shop.description || "Cửa hàng chưa cập nhật phần giới thiệu."}</p><dl className={shopHeaderStyles.facts}>{facts.map(([label,value]) => <div key={label}><dt className={shopHeaderStyles.label}>{label}</dt><dd className={shopHeaderStyles.value}>{value}</dd></div>)}</dl></div></section>;
}

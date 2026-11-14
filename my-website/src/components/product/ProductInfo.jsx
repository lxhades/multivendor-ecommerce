import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import CategorySearchLink from "../category/CategorySearchLink.jsx";
import AddToCart from "../cart/AddToCart";
import { useState } from "react";
import ProductGallery from "./ProductGallery";
import ProductShop from "./ProductShop";
import VariantStock from "./VariantStock";
import { isMediaUrl, mediaUrls, money, variantAttributes, variantName } from "./productHelpers.js";

const productInfoStyles = {
  columns:"grid min-w-0 items-start gap-7 lg:grid-cols-[1fr_1fr] lg:gap-10",
  card:"min-w-0 gap-5 rounded-3xl py-7 shadow-none",
  title:"break-words text-3xl font-extrabold leading-tight tracking-tight sm:text-4xl",
  content:"grid gap-5",
  price:"rounded-2xl bg-accent px-5 py-5 text-3xl font-extrabold tracking-tight text-accent-foreground",
  variants:"grid min-w-0 gap-3",
  legend:"mb-3 text-sm font-semibold",
  variantOptions:"flex flex-wrap gap-2",
  variantButton:"h-auto min-h-11 max-w-full whitespace-normal break-words text-left",
  sku:"break-words text-xs text-muted-foreground",
  attributes:"grid gap-3 rounded-xl bg-background p-4 text-sm",
  attribute:"grid grid-cols-[minmax(0,1fr)_minmax(0,2fr)] gap-3",
  unavailable:"rounded-xl bg-muted p-4 text-sm text-muted-foreground",
  descriptionCard:"rounded-3xl shadow-none",
  description:"whitespace-pre-wrap break-words text-sm leading-8 text-muted-foreground",
};

export default function ProductInfo({ product }) {
  // 1. KHU VỰC XỬ LÝ LOGIC & STATE  
  // Lọc ra các biến thể (variants) đang hoạt động và chưa bị xóa
  const variants = Array.isArray(product.variants) ? product.variants.filter((item) => item.active && !item.deletedAt) : [];
  
  // State: Lưu ID biến thể đang chọn (mặc định lấy biến thể đầu tiên) và ảnh đang chọn
  const [chosen, setChosen] = useState(variants[0]?.id ?? "");
  const [selectedMedia, setSelectedMedia] = useState(null);
  
  // Lấy ra object biến thể tương ứng với ID đang chọn
  const variant = variants.find((item) => item.id === chosen);
  
  // Tổng hợp danh sách ảnh: Ảnh gốc của sản phẩm + Ảnh của các biến thể (Dùng Set để loại bỏ ảnh trùng)
  const media = [...new Set([...mediaUrls(product.mediaJson), ...variants.map((item) => item.imageUrl).filter(isMediaUrl)])];
  
  // Xác định ảnh lớn đang hiển thị: Ưu tiên ảnh user click -> ảnh của biến thể đang chọn -> ảnh đầu tiên của sản phẩm
  const activeUrl = selectedMedia ?? (isMediaUrl(variant?.imageUrl) ? variant.imageUrl : media[0]);
  
  // Lấy thuộc tính và giá của biến thể (Nếu không có biến thể thì lấy giá min của sản phẩm)
  const attributes = variantAttributes(variant);
  const price = variant?.price ?? product.minPrice;

  return <>
    <div className={productInfoStyles.columns}><ProductGallery media={media} activeUrl={activeUrl} onSelect={setSelectedMedia} name={product.name} /><Card className={productInfoStyles.card}><CardHeader><Badge variant="outline" className="w-fit">Chi tiết sản phẩm</Badge><CardTitle className={productInfoStyles.title}><h1>{product.name}</h1></CardTitle><CardDescription>{typeof product.soldCount === "number" && product.soldCount >= 0 ? `Đã bán ${product.soldCount.toLocaleString("vi-VN")}` : "Chọn phân loại phù hợp với bạn"}</CardDescription></CardHeader><CardContent className={productInfoStyles.content}><p className={productInfoStyles.price}>{typeof price === "number" && Number.isFinite(price) ? money.format(price) : "Chưa có giá"}</p>{variants.length ? <><fieldset className={productInfoStyles.variants}><legend className={productInfoStyles.legend}>Chọn phân loại</legend><div className={productInfoStyles.variantOptions}>{variants.map((item) => <Button key={item.id} type="button" variant={chosen === item.id ? "default" : "outline"} aria-pressed={chosen === item.id} onClick={() => { setChosen(item.id); setSelectedMedia(null); }} className={productInfoStyles.variantButton}>{variantName(item)}</Button>)}</div></fieldset>{variant && <><p className={productInfoStyles.sku}>SKU: {variant.sku || "Chưa có"}</p>{attributes.length > 0 && <dl className={productInfoStyles.attributes}>{attributes.map(([label,value]) => <div key={label} className={productInfoStyles.attribute}><dt className="text-muted-foreground">{label}</dt><dd className="break-words font-semibold">{String(value)}</dd></div>)}</dl>}<VariantStock key={variant.id} variantId={variant.id}>{(availableQuantity) => <AddToCart product={product} variant={variant} availableQuantity={availableQuantity} />}</VariantStock></>}</> : <p className={productInfoStyles.unavailable}>Hiện chưa có phân loại đang bán.</p>}</CardContent><CardFooter>{product.categoryId && <CategorySearchLink key={product.categoryId} categoryId={product.categoryId} />}</CardFooter></Card></div>
    {product.shopId && <ProductShop key={product.shopId} shopId={product.shopId} />}
    <Card className={productInfoStyles.descriptionCard}><CardHeader><CardTitle><h2>Mô tả sản phẩm</h2></CardTitle></CardHeader><CardContent><p className={productInfoStyles.description}>{product.description?.trim() || "Sản phẩm chưa có mô tả."}</p></CardContent></Card>
  </>;
}

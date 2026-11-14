import { Button } from "@/components/ui/button";
import { useState } from "react";
import { Link } from "react-router-dom";
import { isVideo, mediaUrls, money } from "../product/productHelpers.js";
import { orderDate } from "../order/orderHelpers.js";

export default function AdminPendingProductsTable({ products, shops, onOpen }) {
  return <div className="overflow-x-auto rounded-2xl border border-border bg-card"><table className="w-full min-w-[1000px] text-left text-sm"><caption className="sr-only">Hàng đợi duyệt sản phẩm</caption><thead className="bg-background text-xs text-muted-foreground"><tr>{["Ảnh", "Sản phẩm / ID", "Cửa hàng", "Giá từ", "Biến thể", "Cập nhật", "Thao tác"].map((label) => <th key={label} scope="col" className="px-4 py-4">{label}</th>)}</tr></thead>
    <tbody className="divide-y divide-border">{products.map((product) => <tr key={product.id} className="align-top hover:bg-background/50"><td className="px-4 py-4"><ProductImage key={product.mediaJson || product.id} product={product} /></td><td className="max-w-64 px-4 py-4"><p className="break-words font-semibold">{product.name}</p><p className="mt-1 break-words text-xs text-muted-foreground">{product.slug}</p><p className="mt-2 break-all text-xs text-muted-foreground">{product.id}</p></td><td className="max-w-52 px-4 py-4"><Link to={`/shops/${encodeURIComponent(product.shopId)}`} className="break-words font-semibold text-primary underline">{shops[product.shopId]?.shop?.name || (shops[product.shopId]?.error ? "Chưa tải được tên shop" : "Đang tải tên shop…")}</Link><p className="mt-2 break-all text-xs text-muted-foreground">{product.shopId}</p></td><td className="whitespace-nowrap px-4 py-4 text-primary">{typeof product.minPrice === "number" ? money.format(product.minPrice) : "Chưa có giá"}</td><td className="px-4 py-4">{product.variants.filter((variant) => !variant.deletedAt).length}<p className="mt-1 text-xs text-muted-foreground">{product.variants.filter((variant) => !variant.deletedAt && variant.active).length} đang bật</p></td><td className="px-4 py-4 text-xs text-muted-foreground">{orderDate(product.updatedAt)}</td><td className="px-4 py-4"><Button variant="outline" type="button" onClick={() => onOpen(product.id)} className="min-h-11 whitespace-nowrap rounded-lg border border-primary/25 px-3 text-xs font-semibold text-primary">Chi tiết / Duyệt</Button></td></tr>)}</tbody>
  </table></div>;
}
function ProductImage({ product }) {
  const [failed, setFailed] = useState(false);
  const url = mediaUrls(product.mediaJson).find((item) => !isVideo(item)) || product.variants.find((item) => item.imageUrl)?.imageUrl;
  return <div className="grid size-16 place-items-center overflow-hidden rounded-lg bg-muted">{url && !failed ? <img src={url} alt={product.name} loading="lazy" onError={() => setFailed(true)} className="size-full object-contain" /> : <span className="text-xs text-muted-foreground">Chưa có ảnh</span>}</div>;
}

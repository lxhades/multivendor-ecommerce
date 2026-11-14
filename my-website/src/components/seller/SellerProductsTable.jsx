import { Button } from "@/components/ui/button";
import { useState } from "react";
import { isVideo, mediaUrls, money } from "../product/productHelpers.js";
import { approvalStatuses, productStatuses } from "./sellerProductHelpers.js";

// Định nghĩa các cột của bảng
const COLUMNS = ["Ảnh", "ID / Tên sản phẩm", "Giá từ", "Đã bán", "Biến thể", "Trạng thái", "Thao tác"];

// Hàm phụ trợ map màu cho từng loại trạng thái
const getBadgeStyle = (type, status) => {
  // Trạng thái bán (Product)
  if (type === "product") {
    if (status === "ACTIVE") return "bg-green-50 text-green-700 border-green-200";
    if (status === "OUT_OF_STOCK") return "bg-red-50 text-red-700 border-red-200";
    return "bg-muted text-muted-foreground border-border";
  }
  // Trạng thái duyệt (Approval)
  if (type === "approval") {
    if (status === "APPROVED") return "bg-violet/10 text-violet border-violet/20";
    if (status === "PENDING") return "bg-amber-50 text-amber-700 border-amber-200";
    if (status === "REJECTED") return "bg-red-50 text-red-700 border-red-200";
    return "bg-muted text-muted-foreground border-border";
  }
  return "bg-background text-foreground border-border";
};

export default function SellerProductsTable({ products, onOpen }) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-input bg-card shadow-sm">
      <table className="w-full min-w-[880px] text-left text-sm">
        <caption className="sr-only">
          Sản phẩm của cửa hàng và thao tác xem chi tiết
        </caption>
        
        {/* HEADER */}
        <thead className="border-b border-input bg-background text-xs text-muted-foreground">
          <tr>
            {COLUMNS.map((label) => (
              <th key={label} scope="col" className="px-4 py-4 font-semibold whitespace-nowrap">
                {label}
              </th>
            ))}
          </tr>
        </thead>

        {/* BODY */}
        <tbody className="divide-y divide-border">
          {products.map((product) => {
            // Lấy số lượng biến thể đang active
            const activeVariants = product.variants.filter((v) => !v.deletedAt).length;

            return (
              <tr key={product.id} className="align-top hover:bg-background/80 transition-colors">
                {/* 1. Cột Ảnh */}
                <td className="px-4 py-4">
                  <ProductImage key={product.mediaJson ?? product.id} product={product} />
                </td>

                {/* 2. Cột Tên & ID */}
                <td className="max-w-80 px-4 py-4">
                  <p className="break-words font-semibold text-foreground line-clamp-2">
                    {product.name}
                  </p>
                  <p className="mt-2 break-all text-xs text-muted-foreground">
                    ID: {product.id}
                  </p>
                  <p className="mt-1 break-words text-xs text-muted-foreground">
                    Slug: {product.slug}
                  </p>
                </td>

                {/* 3. Cột Giá */}
                <td className="whitespace-nowrap px-4 py-4 font-semibold text-primary">
                  {typeof product.minPrice === "number" ? money.format(product.minPrice) : "Chưa có giá"}
                </td>

                {/* 4. Cột Đã bán */}
                <td className="whitespace-nowrap px-4 py-4 font-medium text-foreground">
                  {(product.soldCount ?? 0).toLocaleString("vi-VN")}
                </td>

                {/* 5. Cột Biến thể */}
                <td className="whitespace-nowrap px-4 py-4 font-medium text-foreground">
                  {activeVariants}
                </td>

                {/* 6. Cột Trạng thái (Thêm Badge màu) */}
                <td className="px-4 py-4">
                  <div className="flex flex-col items-start gap-1.5">
                    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-semibold ${getBadgeStyle("product", product.status)}`}>
                      {productStatuses[product.status] ?? product.status}
                    </span>
                    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-semibold ${getBadgeStyle("approval", product.approvalStatus)}`}>
                      {approvalStatuses[product.approvalStatus] ?? product.approvalStatus}
                    </span>
                  </div>
                </td>

                {/* 7. Cột Thao tác */}
                <td className="px-4 py-4">
                  <Button variant="outline" 
                    type="button" 
                    onClick={() => onOpen(product.id)} 
                    className="min-h-10 whitespace-nowrap rounded-lg border border-primary/25 bg-accent px-4 font-semibold text-primary hover:bg-accent transition-colors"
                  >
                    Chi tiết
                  </Button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

// Sub-component: Hiển thị ảnh sản phẩm
function ProductImage({ product }) {
  const [failed, setFailed] = useState(false);
  const image = mediaUrls(product.mediaJson).find((url) => !isVideo(url));

  return (
    <div className="grid size-16 place-items-center overflow-hidden rounded-lg bg-muted border border-border">
      {image && !failed ? (
        <img 
          src={image} 
          alt={product.name} 
          loading="lazy" 
          onError={() => setFailed(true)} 
          className="size-full object-contain p-1" 
        />
      ) : (
        <span className="text-[10px] text-center text-muted-foreground font-medium">Chưa có<br/>ảnh</span>
      )}
    </div>
  );
}
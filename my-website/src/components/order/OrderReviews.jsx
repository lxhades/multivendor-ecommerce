import { Button } from "@/components/ui/button";
import { productPath } from "../common/catalogPaths.js";
import { useState } from "react";
import { Link } from "react-router-dom";
import OrderReviewForm from "./OrderReviewForm.jsx";

export default function OrderReviews({ order, reviewed, pending, onBusyChange, onComplete }) {
  const [selected, setSelected] = useState(null);
  const products = [...new Map(order.items.filter((item) => item.productId).map((item) => [item.productId, item])).values()];
  if (order.orderStatus !== "DELIVERED") return null;
  function complete(productId, outcome) { onComplete(productId, outcome); setSelected(null); }
  return <section aria-labelledby="order-review-title" className="border-t border-border/60 pt-5">
    <h3 id="order-review-title" className="font-bold">Đánh giá sản phẩm đã nhận</h3>
    <p className="mt-2 text-xs leading-6 text-muted-foreground">Mỗi sản phẩm chỉ được đánh giá một lần trong đơn này, kể cả khi mua nhiều phân loại. Backend kiểm tra quyền đánh giá khi gửi.</p>
    <ul className="mt-3 grid gap-3">{products.map((product) => <li key={product.productId} className="rounded-xl border border-border p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="min-w-0"><p className="break-words text-sm font-semibold">{product.productName || "Sản phẩm"}</p>{reviewed[product.productId] && <p role="status" className="mt-1 text-xs font-semibold text-green-700">{reviewed[product.productId] === "created" ? "Đã gửi đánh giá" : "Đã đánh giá sản phẩm này trong đơn hàng"}</p>}</div>
        {reviewed[product.productId] ? <Link to={productPath(product)} className="inline-flex min-h-11 items-center text-sm font-semibold text-primary underline">Xem đánh giá sản phẩm</Link>
          : <Button variant="outline" type="button" disabled={pending || selected === product.productId} onClick={() => setSelected(product.productId)} className="min-h-11 rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary disabled:opacity-50">Đánh giá</Button>}
      </div>
      {selected === product.productId && !reviewed[product.productId] && <OrderReviewForm key={product.productId} orderId={order.id} product={product} onBusyChange={onBusyChange} onComplete={complete} onCancel={() => setSelected(null)} />}
    </li>)}</ul>
  </section>;
}

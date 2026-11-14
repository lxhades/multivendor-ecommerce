import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getShop } from "../../api/shop/shopApi.js";
import { isMediaUrl } from "./productHelpers.js";

export default function ProductShop({ shopId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [failedLogo, setFailedLogo] = useState(false);
  useEffect(() => {
    const controller = new AbortController();
    getShop(shopId, { signal: controller.signal, skipAuth: true }).then((shop) => {
      if (controller.signal.aborted) return;
      if (shop?.id !== shopId) throw new Error("Thông tin cửa hàng không khớp.");
      setResult({ shop });
    }).catch(() => { if (!controller.signal.aborted) setResult({ error: true }); });
    return () => controller.abort();
  }, [shopId, attempt]);

  return <section className="rounded-2xl border border-border bg-card p-5 sm:p-6" aria-label="Cửa hàng bán sản phẩm">
    <h2 className="mb-4 text-lg font-bold text-foreground">Thông tin cửa hàng</h2>
    {!result ? <p role="status" className="text-sm text-muted-foreground">Đang tải cửa hàng…</p> : result.error ?
      <div role="alert" className="text-sm text-muted-foreground">Chưa tải được thông tin cửa hàng.
        <Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="ml-2 min-h-11 rounded px-2 font-semibold text-primary underline">Thử lại</Button>
      </div> : <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex min-w-0 items-center gap-3">
          <div className="grid size-14 shrink-0 place-items-center overflow-hidden rounded-xl border border-border/60 bg-background">
            {isMediaUrl(result.shop.logoUrl) && !failedLogo ? <img src={result.shop.logoUrl} alt="" loading="lazy" className="size-full object-contain" onError={() => setFailedLogo(true)} />
              : <span aria-hidden="true" className="text-xl font-bold text-primary">{result.shop.name?.charAt(0)?.toUpperCase() || "M"}</span>}
          </div>
          <div className="min-w-0"><p className="font-bold break-words text-foreground">{result.shop.name}</p>
            {typeof result.shop.ratingAvg === "number" && result.shop.ratingCount > 0 && <p className="mt-1 text-sm text-muted-foreground">{result.shop.ratingAvg.toLocaleString("vi-VN")}/5 · {result.shop.ratingCount.toLocaleString("vi-VN")} đánh giá</p>}
          </div>
        </div>
        <Link to={`/shops/${encodeURIComponent(shopId)}`} className="inline-flex min-h-11 items-center rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary hover:bg-accent">Xem cửa hàng</Link>
      </div>}
  </section>;
}

import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { getAvailableStock } from "../../api/inventory/inventoryApi.js";

export default function VariantStock({ variantId, children }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    getAvailableStock(variantId, { signal: controller.signal, skipAuth: true }).then((data) => {
      if (controller.signal.aborted) return;
      if (data?.variantId !== variantId || !Number.isSafeInteger(data.availableQuantity) || data.availableQuantity < 0) throw new Error("Tồn kho không hợp lệ.");
      setResult({ quantity: data.availableQuantity });
    }).catch(() => { if (!controller.signal.aborted) setResult({ error: true }); });
    return () => controller.abort();
  }, [variantId, attempt]);

  if (!result) return <p role="status" className="text-sm text-muted-foreground">Đang kiểm tra tồn kho…</p>;
  if (result.error) return <div role="alert" className="text-sm text-muted-foreground">Chưa kiểm tra được tồn kho.
    <Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="ml-2 min-h-11 rounded px-2 font-semibold text-primary underline">Thử lại</Button>
  </div>;
  return <>
    <div className="flex flex-wrap items-center gap-3">
      <p role="status" className={`w-fit rounded-full px-3 py-1.5 text-sm font-semibold ${result.quantity ? "bg-green-50 text-green-800" : "bg-muted text-muted-foreground"}`}>
        {result.quantity ? `Còn ${result.quantity.toLocaleString("vi-VN")} sản phẩm` : "Tạm hết hàng"}
      </p>
      <Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="min-h-11 rounded px-2 text-xs text-muted-foreground underline">Làm mới tồn kho</Button>
    </div>
    {children?.(result.quantity)}
  </>;
}

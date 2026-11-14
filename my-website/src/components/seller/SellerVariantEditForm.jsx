import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useState } from "react";
import { toast } from "../common/toast.js";
import SellerShopImageField from "./SellerShopImageField.jsx";

const field = "min-h-11 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm";
function readAttributes(text) {
  try {
    const value = JSON.parse(text || "{}");
    if (!value || Array.isArray(value) || typeof value !== "object" || Object.values(value).some((item) => item !== null && !["string", "number", "boolean"].includes(typeof item))) return null;
    return Object.entries(value).map(([key, value]) => ({ id: crypto.randomUUID(), key, text: String(value ?? ""), original: value }));
  } catch { return null; }
}
const emptyVariant = { sku: "", price: 0, attributesJson: "{}", active: true, imageUrl: null };
export default function SellerVariantEditForm({ variant = emptyVariant, pending, onConfirm, onCancel, creating = false }) {
  const [attributes, setAttributes] = useState(() => readAttributes(variant.attributesJson));
  const [image, setImage] = useState(null);
  function update(id, name, value) { setAttributes((current) => current.map((row) => row.id === id ? { ...row, [name]: value } : row)); }
  function submit(event) {
    event.preventDefault();
    if (pending) return;
    const form = new FormData(event.currentTarget);
    const sku = String(form.get("sku") ?? "").trim();
    const priceText = String(form.get("price") ?? "").trim();
    const price = Number(priceText);
    if (!sku || !priceText || !Number.isFinite(price) || price < 0 || price > Number.MAX_SAFE_INTEGER) { toast.error("Nhập SKU và giá hợp lệ không âm."); return; }
    let attributesJson = variant.attributesJson;
    if (attributes) {
      const filled = attributes.filter((row) => row.key.trim() || row.text.trim());
      const keys = filled.map((row) => row.key.trim());
      if (filled.some((row) => !row.key.trim() || !row.text.trim()) || new Set(keys).size !== keys.length) { toast.error("Nhập đủ tên/giá trị thuộc tính; tên thuộc tính không được trùng."); return; }
      attributesJson = JSON.stringify(Object.fromEntries(filled.map((row) => [row.key.trim(), row.text === String(row.original ?? "") ? row.original : row.text.trim()])));
    }
    onConfirm({ sku, price, attributesJson, active: form.get("active") === "on", imageUrl: variant.imageUrl || null }, image);
  }
  return <form onSubmit={submit} className="grid gap-4 rounded-xl border border-primary/25 bg-accent/30 p-4">
    <h3 className="font-bold">{creating ? "Thêm biến thể mới" : `Sửa biến thể ${variant.variantName || variant.sku}`}</h3>
    <fieldset disabled={pending} className="grid gap-4">
      <label className="grid gap-2 text-sm font-semibold">SKU<Input name="sku" defaultValue={variant.sku} required className={field} /></label>
      <label className="grid gap-2 text-sm font-semibold">Đơn giá (VND)<Input type="number" name="price" defaultValue={variant.price} min="0" max={Number.MAX_SAFE_INTEGER} step="0.01" required className={field} /></label>
      <label className="flex min-h-11 items-center gap-2 text-sm"><input type="checkbox" name="active" defaultChecked={variant.active} className="size-4 accent-primary" />Cho phép bán biến thể</label>
      <div className="grid gap-3"><h4 className="text-sm font-semibold">Thuộc tính</h4>
        {attributes ? <>{attributes.map((row) => <div key={row.id} className="grid gap-2 sm:grid-cols-[1fr_1fr_auto]">
          <Input aria-label="Tên thuộc tính" placeholder="Màu sắc" value={row.key} onChange={(event) => update(row.id, "key", event.target.value)} className={field} />
          <Input aria-label="Giá trị thuộc tính" placeholder="Xanh" value={row.text} onChange={(event) => update(row.id, "text", event.target.value)} className={field} />
          <Button variant="outline" type="button" onClick={() => setAttributes((current) => current.filter((item) => item.id !== row.id))} className="min-h-11 px-2 text-sm text-red-700">Bỏ thuộc tính</Button>
        </div>)}<Button variant="outline" type="button" onClick={() => setAttributes((current) => [...current, { id: crypto.randomUUID(), key: "", text: "", original: "" }])} className="min-h-11 w-fit text-sm font-semibold text-primary">+ Thêm thuộc tính</Button></> : <p className="text-xs text-amber-800">Thuộc tính hiện tại có cấu trúc đặc biệt, sẽ được giữ nguyên khi lưu.</p>}
      </div>
      <SellerShopImageField label="Ảnh biến thể" currentUrl={variant.imageUrl} selection={image} onSelect={setImage} pending={pending} />
      <div className="flex flex-wrap gap-3"><Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">{creating ? "Thêm biến thể" : "Lưu biến thể"}</Button><Button variant="outline" type="button" onClick={onCancel} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Hủy chỉnh sửa</Button></div>
    </fieldset>
  </form>;
}

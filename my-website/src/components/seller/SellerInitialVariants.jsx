import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import SellerShopImageField from "./SellerShopImageField.jsx";
import { newVariantDraft } from "./productCreationModel.js";

const field = "min-h-11 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm";
export default function SellerInitialVariants({ variants, onChange, pending }) {
  function update(id, changes) { onChange(variants.map((variant) => variant.id === id ? { ...variant, ...changes } : variant)); }
  return <section className="grid min-w-0 gap-4 border-t border-border pt-4" aria-label="Biến thể đi kèm sản phẩm mới">
    <h4 className="font-bold">Biến thể sản phẩm (bắt buộc)</h4><p className="text-xs text-muted-foreground">Ít nhất một biến thể. Sản phẩm và các biến thể sẽ được tạo cùng một lần xác nhận.</p>
    {variants.map((variant, index) => <div key={variant.id} className="grid gap-4 rounded-xl border border-border bg-card p-4">
      <div className="flex flex-wrap items-center justify-between gap-3"><h5 className="text-sm font-bold">Biến thể {index + 1}</h5><Button variant="outline" type="button" disabled={pending || variants.length === 1} onClick={() => onChange(variants.filter((item) => item.id !== variant.id))} className="min-h-11 px-2 text-xs font-semibold text-red-700 disabled:opacity-40">Bỏ biến thể</Button></div>
      <div className="grid gap-3 sm:grid-cols-2"><label className="grid gap-2 text-sm font-semibold">SKU<Input value={variant.sku} onChange={(event) => update(variant.id, { sku: event.target.value })} required className={field} placeholder="AO-XANH-L" /></label><label className="grid gap-2 text-sm font-semibold">Giá (VND)<Input type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="0.01" value={variant.price} onChange={(event) => update(variant.id, { price: event.target.value })} required className={field} /></label></div>
      <label className="flex min-h-11 items-center gap-2 text-sm"><input type="checkbox" checked={variant.active} onChange={(event) => update(variant.id, { active: event.target.checked })} className="size-4 accent-primary" />Cho phép bán biến thể</label>
      <div className="grid gap-2"><p className="text-sm font-semibold">Thuộc tính</p>{variant.attributes.map((row) => <div key={row.id} className="grid gap-2 sm:grid-cols-[1fr_1fr_auto]">
        <Input aria-label={`Tên thuộc tính biến thể ${index + 1}`} value={row.key} placeholder="Màu sắc" onChange={(event) => update(variant.id, { attributes: variant.attributes.map((item) => item.id === row.id ? { ...item, key: event.target.value } : item) })} className={field} />
        <Input aria-label={`Giá trị thuộc tính biến thể ${index + 1}`} value={row.value} placeholder="Xanh" onChange={(event) => update(variant.id, { attributes: variant.attributes.map((item) => item.id === row.id ? { ...item, value: event.target.value } : item) })} className={field} />
        <Button variant="outline" type="button" onClick={() => update(variant.id, { attributes: variant.attributes.filter((item) => item.id !== row.id) })} className="min-h-11 px-2 text-xs text-red-700">Bỏ thuộc tính</Button>
      </div>)}<Button variant="outline" type="button" onClick={() => update(variant.id, { attributes: [...variant.attributes, { id: crypto.randomUUID(), key: "", value: "" }] })} className="min-h-11 w-fit text-sm font-semibold text-primary">+ Thêm thuộc tính</Button></div>
      <SellerShopImageField label={`Ảnh biến thể ${index + 1}`} selection={variant.image} onSelect={(image) => update(variant.id, { image })} pending={pending} />
    </div>)}
    <Button variant="outline" type="button" disabled={pending} onClick={() => onChange([...variants, newVariantDraft()])} className="min-h-11 w-fit rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary">+ Thêm biến thể đi kèm</Button>
  </section>;
}

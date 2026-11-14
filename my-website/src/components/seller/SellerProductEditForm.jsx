import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import SellerInitialVariants from "./SellerInitialVariants.jsx";
import { defaultProductSlug, initialVariantInputs, newVariantDraft } from "./productCreationModel.js";
import { useState } from "react";
import { mediaUrls } from "../product/productHelpers.js";
import SellerProductMediaField from "./SellerProductMediaField.jsx";
import { toast } from "../common/toast.js";

const field = "min-h-12 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm";
export default function SellerProductEditForm({ product, categories, categoriesError, onConfirm, onCancel, pending, creating = false, shopId }) {
  const [name, setName] = useState(product.name ?? "");
  const [variants, setVariants] = useState(() => creating ? [newVariantDraft()] : []);
  const generatedSlug = creating && shopId ? defaultProductSlug(name, shopId) : "";
  const [media, setMedia] = useState(() => mediaUrls(product.mediaJson).map((url) => ({ id: crypto.randomUUID(), url })));
  const [mediaChanged, setMediaChanged] = useState(false);
  function changeMedia(entries) { setMedia(entries); setMediaChanged(true); }
  function submit(event) {
    event.preventDefault();
    if (pending) return;
    const form = new FormData(event.currentTarget);
    const input = Object.fromEntries(["name", "slug", "description", "categoryId"].map((name) => [name, String(form.get(name) ?? "").trim()]));
    try {
      if (!input.name || !input.categoryId || (!creating && !input.slug)) throw new Error("Vui lòng nhập tên, slug và chọn danh mục.");
      if (creating) input.slug = defaultProductSlug(input.name, shopId);
      const inputs = creating ? initialVariantInputs(variants) : [];
      const images = creating ? variants.filter((variant) => variant.image).map((variant) => ({ sku: variant.sku.trim(), selection: variant.image })) : [];
      onConfirm({ ...input, mediaJson: product.mediaJson, variants: inputs }, mediaChanged ? media : null, images);
    } catch (failure) { toast.error(failure.message); }
  }
  return <form onSubmit={submit} className="grid gap-4 rounded-xl border border-primary/25 bg-accent/30 p-4">
    <h3 className="font-bold">{creating ? "Thông tin sản phẩm mới" : "Sửa thông tin sản phẩm"}</h3>
    <fieldset disabled={pending} className="grid gap-4">
      <label className="grid gap-2 text-sm font-semibold">Tên sản phẩm<Input name="name" value={name} onChange={(event) => setName(event.target.value)} required className={field} /></label>
      <label className="grid gap-2 text-sm font-semibold">Slug{creating ? <Input name="slug" value={generatedSlug} readOnly className={field + " bg-background"} /> : <Input name="slug" defaultValue={product.slug} required className={field} />}{creating && <span className="break-all text-xs font-normal text-muted-foreground">Tự tạo từ tên sản phẩm + ID cửa hàng.</span>}</label>
      <label className="grid gap-2 text-sm font-semibold">Danh mục<select name="categoryId" defaultValue={product.categoryId || ""} required className={field}>
        <option value="" disabled>Chọn danh mục</option>
        {product.categoryId && !categories.some((category) => category.id === product.categoryId) && <option value={product.categoryId}>Danh mục hiện tại ({product.categoryId})</option>}
        {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
      </select></label>
      {categoriesError && <p className="text-xs text-amber-800">Chưa tải được danh mục; bạn vẫn có thể giữ danh mục hiện tại.</p>}
      <label className="grid gap-2 text-sm font-semibold">Mô tả<Textarea name="description" rows={5} defaultValue={product.description ?? ""} className={field + " py-3"} /></label>
      {creating && <SellerInitialVariants variants={variants} onChange={setVariants} pending={pending} />}
      <SellerProductMediaField entries={media} pending={pending} onChange={changeMedia} />
      <div className="flex flex-wrap gap-3"><Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">{creating ? "Tạo sản phẩm" : "Lưu sản phẩm"}</Button><Button variant="outline" type="button" onClick={onCancel} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Hủy chỉnh sửa</Button></div>
    </fieldset>
  </form>;
}

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useState } from "react";
import { catalogSlug } from "../common/catalogPaths.js";
import { toast } from "../common/toast.js";
import SellerShopImageField from "../seller/SellerShopImageField.jsx";
import { categoryFormInput, unavailableCategoryParents } from "./adminCategoryModel.js";

const field = "min-h-12 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm";
export default function AdminCategoryForm({ category, categories, pending, onConfirm, onCancel, editingId = category?.id }) {
  const [name, setName] = useState(category?.name ?? "");
  const [slug, setSlug] = useState(category?.slug ?? "");
  const [slugEdited, setSlugEdited] = useState(Boolean(category));
  const [image, setImage] = useState(null);
  const [removeImage, setRemoveImage] = useState(false);
  const excluded = unavailableCategoryParents(categories, editingId);
  const parents = categories.filter((item) => !excluded.has(item.id));
  function changeName(value) { setName(value); if (!slugEdited) setSlug(value.trim() ? catalogSlug(value).slice(0, 100).replace(/-+$/, "") : ""); }
  function submit(event) {
    event.preventDefault();
    if (pending) return;
    try {
      const input = categoryFormInput(new FormData(event.currentTarget));
      if (input.parentId && !parents.some((item) => item.id === input.parentId)) throw new Error("Danh mục cha không hợp lệ hoặc tạo vòng lặp.");
      if (categories.some((item) => item.id !== editingId && item.slug === input.slug)) throw new Error("Slug đã được dùng cho danh mục khác.");
      onConfirm({ ...input, imageUrl: removeImage ? null : category?.imageUrl || null }, image);
    } catch (failure) { toast.error(failure.message); }
  }
  return <form onSubmit={submit} className="grid gap-4"><fieldset disabled={pending} className="grid gap-4">
    <label className="grid gap-2 text-sm font-semibold">Tên danh mục<Input name="name" value={name} onChange={(event) => changeName(event.target.value)} required maxLength={100} className={field} /></label>
    <label className="grid gap-2 text-sm font-semibold">Slug<Input name="slug" value={slug} onChange={(event) => { setSlug(event.target.value); setSlugEdited(true); }} required pattern="[a-z0-9]+(-[a-z0-9]+)*" maxLength={100} className={field} /><span className="text-xs font-normal leading-6 text-muted-foreground">Dùng trong URL danh mục. Đổi slug sẽ làm URL slug cũ không còn khớp; slug phải duy nhất.</span></label>
    <Button variant="outline" type="button" onClick={() => { setSlugEdited(false); setSlug(name.trim() ? catalogSlug(name).slice(0, 100).replace(/-+$/, "") : ""); }} className="min-h-11 w-fit text-xs font-semibold text-primary">Tạo slug từ tên</Button>
    <label className="grid gap-2 text-sm font-semibold">Danh mục cha<select name="parentId" defaultValue={category?.parentId ?? ""} className={field}><option value="">Không có — Danh mục gốc</option>{parents.map((item) => <option key={item.id} value={item.id}>{item.name}{item.status === "INACTIVE" ? " (đã ẩn)" : ""}</option>)}</select></label>
    <label className="grid gap-2 text-sm font-semibold">Thứ tự hiển thị<Input name="displayOrder" type="number" min="0" max="2147483647" step="1" required defaultValue={category?.displayOrder ?? 0} className={field} /></label>
    <label className="grid gap-2 text-sm font-semibold">Mô tả<Textarea name="description" maxLength={1000} rows={4} defaultValue={category?.description ?? ""} className={field + " py-3"} /></label>
    <SellerShopImageField label="Ảnh danh mục" currentUrl={removeImage ? null : category?.imageUrl} selection={image} pending={pending} onSelect={(selection) => { setImage(selection); if (selection) setRemoveImage(false); }} />
    {category?.imageUrl && <label className="flex min-h-11 items-center gap-2 text-sm"><input type="checkbox" checked={removeImage} onChange={(event) => { setRemoveImage(event.target.checked); if (event.target.checked) setImage(null); }} />Gỡ ảnh hiện tại khi lưu</label>}
    <p className="text-xs text-muted-foreground">Ảnh JPG/PNG/WebP tối đa 10 MB; chỉ upload sau xác nhận lưu.</p>
    <div className="flex flex-wrap gap-3"><Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">{category ? "Lưu thay đổi" : "Tạo danh mục"}</Button><Button variant="outline" type="button" onClick={onCancel} className="min-h-11 rounded-lg border border-input px-4 text-sm">Hủy</Button></div>
  </fieldset></form>;
}

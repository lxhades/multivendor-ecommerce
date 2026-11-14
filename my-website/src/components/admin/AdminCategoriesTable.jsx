import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { isMediaUrl } from "../product/productHelpers.js";
import { categoryStatuses } from "./adminCategoryModel.js";

export default function AdminCategoriesTable({ categories, allCategories, onOpen }) {
  const names = new Map(allCategories.map((category) => [category.id, category.name]));
  return <div className="overflow-x-auto rounded-2xl border border-border bg-card"><table className="w-full min-w-[950px] text-left text-sm"><caption className="sr-only">Quản lý danh mục</caption><thead className="bg-background text-xs text-muted-foreground"><tr>{["Ảnh", "Danh mục / ID", "Danh mục cha", "Thứ tự", "Trạng thái", "Thao tác"].map((label) => <th key={label} scope="col" className="px-4 py-4">{label}</th>)}</tr></thead>
    <tbody className="divide-y divide-border">{categories.map((category) => <tr key={category.id} className="align-top hover:bg-background/50"><td className="px-4 py-4"><CategoryImage key={category.imageUrl || category.id} category={category} /></td><td className="max-w-64 px-4 py-4"><p className="break-words font-semibold">{category.name}</p><p className="mt-1 break-words text-xs text-muted-foreground">{category.slug}</p><p className="mt-2 break-all text-xs text-muted-foreground">{category.id}</p></td><td className="max-w-52 px-4 py-4"><p className="break-words">{category.parentId ? names.get(category.parentId) || category.parentName || "Không còn trong danh sách" : "Danh mục gốc"}</p><p className="mt-1 text-xs text-muted-foreground">{allCategories.filter((item) => item.parentId === category.id).length} danh mục con trực tiếp</p></td><td className="px-4 py-4">{category.displayOrder}</td><td className="px-4 py-4"><Badge variant="outline" className={`whitespace-nowrap rounded-full px-2 py-1 text-xs font-semibold ${category.status === "ACTIVE" ? "bg-green-50 text-green-700" : "bg-muted text-muted-foreground"}`}>{categoryStatuses[category.status]}</Badge></td><td className="px-4 py-4"><div className="flex flex-wrap gap-2"><Button variant="outline" type="button" onClick={() => onOpen(category.id, "edit")} className="min-h-11 rounded-lg border px-3 text-xs font-semibold">Sửa / Chi tiết</Button><Button variant="outline" type="button" onClick={() => onOpen(category.id, category.status === "ACTIVE" ? "hide" : "activate")} className="min-h-11 rounded-lg border border-primary/25 px-3 text-xs font-semibold text-primary">{category.status === "ACTIVE" ? "Ẩn" : "Kích hoạt"}</Button></div></td></tr>)}</tbody>
  </table></div>;
}
function CategoryImage({ category }) {
  const [failed, setFailed] = useState(false);
  return <div className="grid size-16 place-items-center overflow-hidden rounded-lg bg-accent">{isMediaUrl(category.imageUrl) && !failed ? <img src={category.imageUrl} alt={category.name} onError={() => setFailed(true)} loading="lazy" className="size-full object-contain" /> : <span className="text-xl font-bold text-primary">{category.name.charAt(0)}</span>}</div>;
}

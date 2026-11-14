import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "../common/toast.js";
import { readSearchFilters, searchParamsFor } from "./searchModel.js";

const field = "min-h-11 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm";
export default function SearchFilters({ filters, categories, categoriesError, onRetryCategories, onChange }) {
  function apply(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const categoryId = String(form.get("categoryId") ?? "");
    const params = searchParamsFor(filters, { page: 1, categoryId, minPrice: String(form.get("minPrice") ?? ""), maxPrice: String(form.get("maxPrice") ?? ""), includeChildren: form.get("includeChildren") === "on" });
    try { readSearchFilters(params); onChange(params); }
    catch (failure) { toast.error(failure.message); }
  }
  return <aside className="self-start rounded-2xl border border-border bg-card p-4">
    <h2 className="font-bold">Bộ lọc sản phẩm</h2>
    <form onSubmit={apply} className="mt-4 grid gap-4">
      <label className="grid gap-2 text-sm font-semibold">Danh mục<select name="categoryId" defaultValue={filters.categoryId} className={field}><option value="">Tất cả danh mục</option>{filters.categoryId && !categories.some((item) => item.id === filters.categoryId) && <option value={filters.categoryId}>Danh mục đang chọn</option>}{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
      <label className="flex min-h-11 items-center gap-2 text-xs text-muted-foreground"><input type="checkbox" name="includeChildren" defaultChecked={!filters.categoryId || filters.includeChildren} className="size-4 accent-primary" />Gồm danh mục con</label>
      {categoriesError && <div role="alert" className="text-xs text-red-700">Chưa tải được danh mục.<Button variant="outline" type="button" onClick={onRetryCategories} className="min-h-11 px-2 underline">Thử lại</Button></div>}
      <label className="grid gap-2 text-sm font-semibold">Giá từ (VND)<Input name="minPrice" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" defaultValue={filters.minPrice} placeholder="Không giới hạn" className={field} /></label>
      <label className="grid gap-2 text-sm font-semibold">Giá đến (VND)<Input name="maxPrice" type="number" min="0" max={Number.MAX_SAFE_INTEGER} step="1" defaultValue={filters.maxPrice} placeholder="Không giới hạn" className={field} /></label>
      <p className="text-xs leading-6 text-muted-foreground">Khoảng giá áp dụng theo giá thấp nhất của sản phẩm.</p>
      <Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">Áp dụng bộ lọc</Button>
      <Button variant="outline" type="button" onClick={() => onChange(searchParamsFor(filters, { categoryId: "", minPrice: "", maxPrice: "", includeChildren: false, page: 1 }))} className="min-h-11 rounded-lg border border-input px-3 text-sm font-semibold">Xóa bộ lọc danh mục/giá</Button>
    </form>
  </aside>;
}

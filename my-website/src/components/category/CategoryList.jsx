import { Button } from "@/components/ui/button";
import { categorySearchPath } from "../common/catalogPaths.js";
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getCategories } from "../../api/category/categoryApi.js";
import CategoryCard from "./CategoryCard";

export default function CategoryList() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const selectedId = params.get("categoryId");

  useEffect(() => {
    const controller = new AbortController();
    getCategories({ signal: controller.signal, skipAuth: true }).then((data) => {
      if (controller.signal.aborted) return;
      if (!Array.isArray(data)) throw new Error("Phản hồi danh mục không hợp lệ.");
      const categories = data.filter((category) => category.status === "ACTIVE")
        .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0) || a.name.localeCompare(b.name, "vi"));
      setResult({ categories });
    }).catch((error) => {
      if (!controller.signal.aborted) setResult({ error: error.response?.data?.message || "Không tải được danh mục. Vui lòng thử lại." });
    });
    return () => controller.abort();
  }, [attempt]);

  function selectCategory(id) {
    const next = new URLSearchParams();
    if (params.get("q")) next.set("q", params.get("q"));
    if (id) { next.set("categoryId", id); next.set("includeChildren", "true"); }
    const category = id ? result?.categories?.find((item) => item.id === id) : null;
    if (id && !category?.slug) return;
    navigate(categorySearchPath(category, next));
  }

  return <section id="categories" aria-labelledby="categories-heading" className="grid min-w-0 scroll-mt-6 gap-4">
    <div className="flex flex-wrap items-center justify-between gap-3">
      <h2 id="categories-heading" className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">Mua sắm theo danh mục</h2>
      {selectedId && <Button variant="outline" type="button" onClick={() => selectCategory(null)} className="min-h-11 rounded-lg border border-primary/25 bg-card px-3 text-sm font-semibold text-primary hover:bg-accent">Tất cả sản phẩm</Button>}
    </div>
    {!result && <p role="status" className="rounded-xl border border-border bg-card p-5 text-sm text-muted-foreground">Đang tải danh mục…</p>}
    {result?.error && <div role="alert" className="rounded-xl border border-red-200 bg-card p-4 text-sm text-red-700">
      <p>{result.error}</p>
      <Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="mt-2 min-h-11 rounded-lg border border-red-200 px-3 font-semibold">Thử lại</Button>
    </div>}
    {result?.categories?.length === 0 && <p className="rounded-xl border border-border bg-card p-5 text-sm text-muted-foreground">Chưa có danh mục đang hoạt động.</p>}
    {result?.categories?.length > 0 && <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
      {result.categories.map((category) => <li key={category.id} className="min-w-0 [&>button]:w-full">
        <CategoryCard category={category} selected={selectedId === category.id} onSelect={selectCategory} />
      </li>)}
    </ul>}
    {selectedId && <p role="status" className="text-sm text-muted-foreground">Đang lọc theo danh mục{result?.categories?.find((category) => category.id === selectedId)?.name ? `: ${result.categories.find((category) => category.id === selectedId).name}` : " đã chọn"}.</p>}
  </section>;
}

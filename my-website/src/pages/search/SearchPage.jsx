import { Button } from "@/components/ui/button";
import { categorySearchPath } from "../../components/common/catalogPaths.js";
import { toast } from "../../components/common/toast.js";
import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useLocation, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { getCategories } from "../../api/category/categoryApi.js";
import { getShop } from "../../api/shop/shopApi.js";
import SearchFilters from "../../components/search/SearchFilters.jsx";
import SearchResults from "../../components/search/SearchResults.jsx";
import { readSearchFilters, resolveCategorySearch, searchParamsFor, searchSorts } from "../../components/search/searchModel.js";

const searchPageStyles = {
  description: "mx-auto max-w-7xl px-4 py-10 text-sm text-muted-foreground",
  section: "mx-auto grid max-w-7xl justify-items-start gap-3 px-4 py-10",
  heading: "text-2xl font-bold",
  errorDescription: "text-sm text-red-700",
  button: "min-h-11 rounded-lg border px-4 text-sm",
  link: "inline-flex min-h-11 items-center rounded-lg border border-primary/25 bg-card px-4 text-sm font-semibold text-primary",
  page: "mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:px-8",
  navigation: "text-sm text-muted-foreground",
  link2: "inline-flex min-h-11 items-center font-semibold hover:text-primary",
  labelText: "mx-2",
  heading2: "break-words text-2xl font-extrabold sm:text-3xl",
  description2: "mt-2 text-sm text-muted-foreground",
  container: "flex flex-wrap items-center gap-2 text-xs",
  button2: "min-h-11 rounded-full border border-border bg-card px-3",
  button3: "min-h-11 rounded-full border border-primary/25 bg-accent px-3 text-accent-foreground",
  container2: "grid min-w-0 items-start gap-5 lg:grid-cols-[240px_minmax(0,1fr)]",
  section2: "grid min-w-0 gap-5",
  container3: "flex flex-wrap items-center justify-between gap-3 rounded-xl border border-border bg-card p-3",
  label: "flex flex-wrap items-center gap-2 text-xs font-semibold",
  select: "min-h-11 rounded-lg border border-input bg-card px-3 text-sm",
  label2: "flex items-center gap-2 text-xs font-semibold",
  container4: "flex flex-wrap items-center gap-2",
  link3: "inline-flex min-h-11 items-center rounded-full border border-primary/25 bg-accent px-3 text-accent-foreground",
};

export default function SearchPage() {
  const [params] = useSearchParams();
  const { categorySlug } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const queryKey = `${categorySlug ?? ""}?${params}`;
  const [categories, setCategories] = useState([]);
  const [categoryReady, setCategoryReady] = useState(false);
  const [categoriesError, setCategoriesError] = useState(false);
  const [categoryAttempt, setCategoryAttempt] = useState(0);
  const parsed = useMemo(() => {
    try {
      if (categorySlug || params.get("categoryId")) {
        if (!categoryReady) return { loading: true };
        if (categoriesError) throw new Error("Chưa tải được danh mục để xác định URL. Vui lòng thử lại.");
      }
      return resolveCategorySearch(params, categorySlug, categories);
    } catch (failure) { return { error: failure.message }; }
  }, [params, categorySlug, categories, categoryReady, categoriesError]);
  useEffect(() => {
    const request = new AbortController();
    getCategories({ signal: request.signal, skipAuth: true }).then((items) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(items)) throw new Error("Danh mục không hợp lệ");
      setCategories(items.filter((item) => item.status === "ACTIVE").sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0) || a.name.localeCompare(b.name, "vi")));
      setCategoriesError(false);
      setCategoryReady(true);
    }).catch(() => { if (!request.signal.aborted) { setCategoriesError(true); setCategoryReady(true); } });
    return () => request.abort();
  }, [categoryAttempt]);
  useEffect(() => { window.scrollTo({ top: 0, left: 0, behavior: "instant" }); }, [queryKey]);
  if (parsed.loading) return <p role="status" className={searchPageStyles.description}>Đang xác định danh mục…</p>;
  if (parsed.error) return <section className={searchPageStyles.section}><h1 className={searchPageStyles.heading}>Không thể tìm kiếm với URL này</h1><p role="alert" className={searchPageStyles.errorDescription}>{parsed.error}</p>{categoriesError && <Button variant="outline" type="button" onClick={() => { setCategoryReady(false); setCategoriesError(false); setCategoryAttempt((value) => value + 1); }} className={searchPageStyles.button}>Thử tải lại danh mục</Button>}<Link to="/search" className={searchPageStyles.link}>Bắt đầu tìm kiếm mới</Link></section>;
  if (parsed.canonical !== `${location.pathname}${location.search}`) return <Navigate to={`${parsed.canonical}${location.hash}`} replace />;
  const { filters } = parsed;
  function change(next) {
    try {
      const selected = readSearchFilters(next);
      const category = selected.categoryId ? categories.find((item) => item.id === selected.categoryId) : null;
      if (selected.categoryId && !category?.slug) throw new Error("Chưa xác định được slug danh mục. Hãy tải lại danh mục.");
      navigate(categorySearchPath(category, next));
    } catch (failure) { toast.error(failure.message); }
  }
  const categoryName = categories.find((category) => category.id === filters.categoryId)?.name;
  return <div className={searchPageStyles.page}>
    <nav aria-label="Điều hướng tìm kiếm" className={searchPageStyles.navigation}><Link to="/" className={searchPageStyles.link2}>Trang chủ</Link><span aria-hidden="true" className={searchPageStyles.labelText}>/</span><span>Tìm kiếm</span></nav>
    <div><h1 className={searchPageStyles.heading2}>{filters.q ? `Kết quả cho “${filters.q}”` : "Khám phá sản phẩm"}</h1><p className={searchPageStyles.description2}>Tìm sản phẩm theo tên hoặc SKU, kết hợp danh mục và khoảng giá.</p></div>
    <div className={searchPageStyles.container}>
      {filters.q && <Button variant="outline" type="button" onClick={() => change(searchParamsFor(filters, { q: "", sort: "NEWEST", page: 1 }))} className={searchPageStyles.button2}>Bỏ từ khóa ×</Button>}
      {filters.categoryId && <Button variant="outline" type="button" onClick={() => change(searchParamsFor(filters, { categoryId: "", page: 1 }))} className={searchPageStyles.button3}>{categoryName || "Danh mục đã chọn"} ×</Button>}
      {(filters.minPrice !== "" || filters.maxPrice !== "") && <Button variant="outline" type="button" onClick={() => change(searchParamsFor(filters, { minPrice: "", maxPrice: "", page: 1 }))} className={searchPageStyles.button2}>Bỏ khoảng giá ×</Button>}
      {filters.shopId && <ShopScope key={filters.shopId} shopId={filters.shopId} onRemove={() => change(searchParamsFor(filters, { shopId: "", page: 1 }))} />}
    </div>
    <div className={searchPageStyles.container2}>
      <SearchFilters key={queryKey} filters={filters} categories={categories} categoriesError={categoriesError} onRetryCategories={() => setCategoryAttempt((value) => value + 1)} onChange={change} />
      <section aria-label="Kết quả tìm kiếm sản phẩm" className={searchPageStyles.section2}>
        <div className={searchPageStyles.container3}>
          <label className={searchPageStyles.label}>Sắp xếp<select value={filters.sort} onChange={(event) => change(searchParamsFor(filters, { sort: event.target.value, page: 1 }))} className={searchPageStyles.select}>{Object.entries(searchSorts).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <label className={searchPageStyles.label2}>Mỗi trang<select value={filters.size} onChange={(event) => change(searchParamsFor(filters, { size: Number(event.target.value), page: 1 }))} className={searchPageStyles.select}>{[...new Set([12, 24, 48, filters.size])].sort((a, b) => a - b).map((size) => <option key={size} value={size}>{size}</option>)}</select></label>
        </div>
        <SearchResults key={queryKey} filters={filters} onChange={change} />
      </section>
    </div>
  </div>;
}
function ShopScope({ shopId, onRemove }) {
  const [name, setName] = useState("");
  useEffect(() => {
    const request = new AbortController();
    getShop(shopId, { signal: request.signal, skipAuth: true }).then((shop) => { if (!request.signal.aborted && shop?.id === shopId) setName(shop.name); }).catch(() => {});
    return () => request.abort();
  }, [shopId]);
  return <div className={searchPageStyles.container4}><Link to={`/shops/${encodeURIComponent(shopId)}`} className={searchPageStyles.link3}>Trong shop: {name || "Cửa hàng đã chọn"}</Link><Button variant="outline" type="button" onClick={onRemove} className={searchPageStyles.button2}>Tìm ở mọi shop ×</Button></div>;
}

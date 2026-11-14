import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getPendingProducts } from "../../api/product/adminProductApi.js";
import { getAdminShop } from "../../api/shop/adminShopApi.js";
import { getCategories } from "../../api/category/categoryApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { searchText } from "../../components/seller/sellerProductHelpers.js";
import { validateModerationProduct } from "../../components/admin/adminProductModel.js";
import AdminPendingProductsTable from "../../components/admin/AdminPendingProductsTable.jsx";
import AdminProductModerationDialog from "../../components/admin/AdminProductModerationDialog.jsx";

const adminProductsPageStyles = {
  container: "grid min-w-0 gap-5",
  header: "flex flex-wrap items-center justify-between gap-3",
  title: "text-2xl font-extrabold tracking-tight",
  description: "mt-2 text-sm text-muted-foreground",
  button: "min-h-11 rounded-lg border bg-card px-4 text-sm disabled:opacity-50",
  filters: "grid gap-3 rounded-xl border bg-card p-4 sm:grid-cols-[1fr_auto]",
  label: "grid gap-2 text-sm",
  input: "min-h-12 min-w-0 rounded-lg border px-3",
  select: "min-h-12 rounded-lg border bg-card px-3",
  container2: "text-sm text-muted-foreground",
  description2: "mt-1",
  description3: "mt-1 text-amber-800",
  description4: "rounded-xl border bg-card p-6",
  errorContainer: "rounded-xl bg-red-50 p-5 text-sm text-red-700",
  button2: "min-h-11 underline",
  description5: "rounded-xl border bg-card p-8 text-center text-sm text-muted-foreground",
  navigation: "flex items-center justify-center gap-3 text-sm",
  button3: "min-h-11 rounded-lg border bg-card px-3 disabled:opacity-50",
};

export default function AdminProductsPage() {
  const { auth } = useOutletContext();
  return <AdminProducts key={auth.user.userId} adminId={auth.user.userId} />;
}
function AdminProducts({ adminId }) {
  const [result, setResult] = useState({ loading: true, products: [] });
  const [attempt, setAttempt] = useState(0), [shops, setShops] = useState({});
  const [categories, setCategories] = useState([]), [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0), [size, setSize] = useState(20), [selected, setSelected] = useState(null);
  useEffect(() => {
    const request = new AbortController();
    getPendingProducts({ signal: request.signal }).then((data) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(data)) throw new Error("Danh sách sản phẩm chờ duyệt không hợp lệ.");
      const products = data.map((product) => validateModerationProduct(product));
      if (new Set(products.map((product) => product.id)).size !== products.length) throw new Error("Danh sách sản phẩm trùng ID.");
      products.sort((a, b) => String(b.updatedAt ?? "").localeCompare(String(a.updatedAt ?? "")));
      setResult({ loading: false, products: products.filter((product) => product.approvalStatus === "PENDING" && !product.deletedAt) });
    }).catch((failure) => { if (!request.signal.aborted) setResult({ loading: false, products: [], error: authErrorMessage(failure, "Không tải được hàng đợi duyệt.") }); });
    return () => request.abort();
  }, [attempt]);
  useEffect(() => {
    const request = new AbortController();
    getCategories({ signal: request.signal }).then((data) => { if (!request.signal.aborted && Array.isArray(data)) setCategories(data); }).catch(() => { /* Category ID remains visible if the catalog is unavailable. */ });
    return () => request.abort();
  }, []);
  useEffect(() => {
    if (result.loading || result.error) return;
    const request = new AbortController();
    const ids = [...new Set(result.products.map((product) => product.shopId))];
    let cursor = 0;
    async function worker() {
      while (cursor < ids.length && !request.signal.aborted) {
        const id = ids[cursor++];
        try {
          const shop = await getAdminShop(id, { signal: request.signal });
          if (request.signal.aborted) return;
          if (shop?.id !== id || !shop.name) throw new Error("Thông tin shop không hợp lệ.");
          setShops((current) => ({ ...current, [id]: { shop } }));
        } catch (failure) {
          if (!request.signal.aborted) setShops((current) => ({ ...current, [id]: { error: authErrorMessage(failure, "Không tải được tên shop.") } }));
        }
      }
    }
    for (let index = 0; index < Math.min(4, ids.length); index++) void worker();
    return () => request.abort();
  }, [result]);
  function refresh() { setResult({ loading: true, products: [] }); setShops({}); setPage(0); setAttempt((value) => value + 1); }
  const term = searchText(keyword);
  const filtered = result.products.filter((product) => searchText([product.name, product.id, product.slug, product.shopId, shops[product.shopId]?.shop?.name, ...product.variants.flatMap((variant) => [variant.sku, variant.variantName])].join(" ")).includes(term));
  const pages = Math.ceil(filtered.length / size), current = Math.min(page, Math.max(0, pages - 1));
  const shopIds = [...new Set(result.products.map((product) => product.shopId))];
  return <div className={adminProductsPageStyles.container}>
    <header className={adminProductsPageStyles.header}><div><h2 className={adminProductsPageStyles.title}>Duyệt sản phẩm</h2><p className={adminProductsPageStyles.description}>Sản phẩm chờ duyệt từ các cửa hàng, kèm đầy đủ biến thể.</p></div><Button variant="outline" type="button" onClick={refresh} disabled={result.loading || Boolean(selected)} className={adminProductsPageStyles.button}>Tải lại</Button></header>
    <fieldset disabled={Boolean(selected)} className={adminProductsPageStyles.filters}><label className={adminProductsPageStyles.label}>Tìm sản phẩm / cửa hàng<Input type="search" value={keyword} onChange={(event) => { setKeyword(event.target.value); setPage(0); }} placeholder="Tên sản phẩm, SKU, ID hoặc tên cửa hàng…" maxLength={150} className={adminProductsPageStyles.input} /></label><label className={adminProductsPageStyles.label}>Mỗi trang<select value={size} onChange={(event) => { setSize(Number(event.target.value)); setPage(0); }} className={adminProductsPageStyles.select}>{[10, 20, 50].map((value) => <option key={value} value={value}>{value}</option>)}</select></label></fieldset>
    {!result.loading && !result.error && <div className={adminProductsPageStyles.container2}><p>{filtered.length}/{result.products.length} sản phẩm chờ duyệt. Phân trang trên toàn bộ hàng đợi đã tải.</p>{shopIds.some((id) => !shops[id]) && <p role="status" className={adminProductsPageStyles.description2}>Đang tải tên cửa hàng để bổ sung tìm kiếm…</p>}{shopIds.some((id) => shops[id]?.error) && <p className={adminProductsPageStyles.description3}>Một số tên cửa hàng chưa tải được. Có thể xem chi tiết để thử lại hoặc tải lại hàng đợi.</p>}</div>}
    {result.loading ? <p role="status" className={adminProductsPageStyles.description4}>Đang tải hàng đợi…</p> : result.error ? <div role="alert" className={adminProductsPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={adminProductsPageStyles.button2}>Thử lại</Button></div> : !filtered.length ? <p className={adminProductsPageStyles.description5}>{keyword ? "Không có sản phẩm phù hợp." : "Không có sản phẩm chờ duyệt."}</p> : <AdminPendingProductsTable products={filtered.slice(current * size, (current + 1) * size)} shops={shops} onOpen={setSelected} />}
    {pages > 1 && <nav aria-label="Phân trang sản phẩm chờ duyệt" className={adminProductsPageStyles.navigation}><Button variant="outline" type="button" disabled={current === 0 || Boolean(selected)} onClick={() => setPage(current - 1)} className={adminProductsPageStyles.button3}>Trang trước</Button><span>{current + 1}/{pages}</span><Button variant="outline" type="button" disabled={current + 1 >= pages || Boolean(selected)} onClick={() => setPage(current + 1)} className={adminProductsPageStyles.button3}>Trang sau</Button></nav>}
    {selected && <AdminProductModerationDialog key={selected} productId={selected} adminId={adminId} categories={categories} onClose={() => setSelected(null)} onChanged={refresh} />}
  </div>;
}

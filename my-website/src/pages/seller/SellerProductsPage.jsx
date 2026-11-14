import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import SellerCreateProductDialog from "../../components/seller/SellerCreateProductDialog.jsx";
import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { getMyShop } from "../../api/shop/sellerShopApi.js";
import { getSellerProducts } from "../../api/product/sellerProductApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { searchText, validateSellerProduct } from "../../components/seller/sellerProductHelpers.js";
import SellerProductsTable from "../../components/seller/SellerProductsTable.jsx";
import SellerProductDialog from "../../components/seller/SellerProductDialog.jsx";

const sellerProductsPageStyles = {
  container: "grid min-w-0 gap-5",
  container2: "flex flex-wrap items-center justify-between gap-3",
  title: "text-2xl font-extrabold tracking-tight",
  description: "mt-1 text-sm text-muted-foreground",
  button: "min-h-11 rounded-lg border border-border bg-card px-4 text-sm font-semibold disabled:opacity-50",
  primaryButton: "min-h-11 w-fit rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50",
  form: "flex min-w-0 gap-2",
  input: "min-h-12 min-w-0 flex-1 rounded-lg border border-input bg-card px-3 text-sm",
  primaryButton2: "min-h-12 shrink-0 rounded-lg bg-primary px-4 text-sm font-bold text-white",
  button2: "min-h-12 shrink-0 rounded-lg border border-border bg-card px-3 text-sm",
  description2: "rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground",
  container3: "grid justify-items-start gap-3 rounded-xl border border-primary/25 bg-accent p-5 text-sm",
  link: "inline-flex min-h-11 items-center font-semibold text-primary underline",
  errorContainer: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button3: "min-h-11 rounded-lg border border-red-200 px-4",
  description3: "text-sm text-muted-foreground",
  description4: "rounded-xl border border-border bg-card p-8 text-center text-sm text-muted-foreground",
};

export default function SellerProductsPage() {
  const { auth } = useOutletContext();
  return <SellerProducts key={auth.user.userId} sellerId={auth.user.userId} />;
}
function SellerProducts({ sellerId }) {
  const [shop, setShop] = useState(null);
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [query, setQuery] = useState("");
  const [keyword, setKeyword] = useState("");
  const [selected, setSelected] = useState(null);
  const [creating, setCreating] = useState(false);
  useEffect(() => {
    const request = new AbortController();
    async function load() {
      try {
        const currentShop = await getMyShop({ signal: request.signal });
        if (request.signal.aborted) return;
        if (!currentShop?.id || currentShop.sellerId !== sellerId) throw new Error("Thông tin cửa hàng không khớp tài khoản.");
        setShop(currentShop);
        const products = await getSellerProducts(currentShop.id, { signal: request.signal });
        if (request.signal.aborted) return;
        if (!Array.isArray(products)) throw new Error("Danh sách sản phẩm không hợp lệ.");
        setResult({ products: products.map((product) => validateSellerProduct(product, currentShop.id)).filter((product) => !product.deletedAt) });
      } catch (failure) {
        if (request.signal.aborted) return;
        if (failure.response?.status === 404 && (failure.response.data?.message === "Không tìm thấy shop" || failure.response.data?.error === "SHOP_NOT_FOUND")) setResult({ missingShop: true });
        else setResult({ error: authErrorMessage(failure, "Không tải được sản phẩm của cửa hàng.") });
      }
    }
    void load();
    return () => request.abort();
  }, [sellerId, attempt]);
  function refresh() { setResult(null); setAttempt((value) => value + 1); }
  function search(event) { event.preventDefault(); setKeyword(query); }
  const term = searchText(keyword);
  const products = result?.products?.filter((product) => searchText([product.id, product.name, product.slug, ...product.variants.filter((variant) => !variant.deletedAt).map((variant) => variant.sku)].join(" ")).includes(term)) ?? [];
  return <div className={sellerProductsPageStyles.container}>
    <div className={sellerProductsPageStyles.container2}><div><h2 className={sellerProductsPageStyles.title}>Quản lý sản phẩm</h2><p className={sellerProductsPageStyles.description}>Sản phẩm và biến thể của cửa hàng {shop?.name ?? "của bạn"}.</p></div><Button variant="outline" type="button" disabled={!result || Boolean(selected) || creating} onClick={refresh} className={sellerProductsPageStyles.button}>Tải lại</Button></div>
    {shop && <Button variant="default" type="button" disabled={Boolean(selected) || creating || !result?.products} onClick={() => setCreating(true)} className={sellerProductsPageStyles.primaryButton}>+ Tạo sản phẩm</Button>}
    <form role="search" onSubmit={search} className={sellerProductsPageStyles.form}>
      <Input type="search" aria-label="Tìm sản phẩm theo tên, ID, slug hoặc SKU" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm tên sản phẩm, ID, slug hoặc SKU…" className={sellerProductsPageStyles.input} />
      <Button variant="default" type="submit" className={sellerProductsPageStyles.primaryButton2}>Tìm kiếm</Button>
      {keyword && <Button variant="outline" type="button" onClick={() => { setQuery(""); setKeyword(""); }} className={sellerProductsPageStyles.button2}>Bỏ lọc</Button>}
    </form>
    {!result ? <p role="status" className={sellerProductsPageStyles.description2}>Đang tải sản phẩm…</p>
      : result.missingShop ? <div className={sellerProductsPageStyles.container3}><p>Bạn chưa có cửa hàng để quản lý sản phẩm.</p><Link to="/seller/shop" className={sellerProductsPageStyles.link}>Tạo cửa hàng</Link></div>
      : result.error ? <div role="alert" className={sellerProductsPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={sellerProductsPageStyles.button3}>Thử lại</Button></div>
      : <><p className={sellerProductsPageStyles.description3}>{products.length}/{result.products.length} sản phẩm · Số lượng đã bán lấy từ dữ liệu sản phẩm, không phải tồn kho.</p>
        {!products.length ? <p className={sellerProductsPageStyles.description4}>{keyword ? "Không tìm thấy sản phẩm phù hợp." : "Cửa hàng chưa có sản phẩm."}</p> : <SellerProductsTable products={products} onOpen={setSelected} />}</>}
    {creating && shop && <SellerCreateProductDialog shopId={shop.id} sellerId={sellerId} onClose={() => setCreating(false)} onChanged={refresh} onCreated={(product) => { setCreating(false); setSelected(product.id); refresh(); }} />}
    {selected && shop && <SellerProductDialog key={`${shop.id}:${selected}`} shopId={shop.id} productId={selected} sellerId={sellerId} onClose={() => setSelected(null)} onChanged={refresh} />}
  </div>;
}

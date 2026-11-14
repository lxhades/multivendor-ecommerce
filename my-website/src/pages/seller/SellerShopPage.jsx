import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { getMyShop } from "../../api/shop/sellerShopApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import SellerShopForm from "../../components/seller/SellerShopForm.jsx";
import ShopHeader from "../../components/shop/ShopHeader.jsx";

const sellerShopPageStyles = {
  container: "grid gap-6",
  container2: "flex flex-wrap items-center justify-between gap-3",
  title: "text-2xl font-extrabold tracking-tight",
  link: "inline-flex min-h-11 items-center rounded-lg border border-primary/25 bg-card px-4 text-sm font-semibold text-primary",
  description: "rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground",
  errorContainer: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button: "min-h-11 rounded-lg border border-red-200 px-4",
  description2: "rounded-xl border border-primary/25 bg-accent p-4 text-sm text-accent-foreground",
  description3: "text-xs text-muted-foreground",
  button2: "min-h-11 w-fit rounded-lg border border-border bg-card px-4 text-sm font-semibold",
};

export default function SellerShopPage() {
  const { auth } = useOutletContext();
  return <SellerShopInfo key={auth.user.userId} sellerId={auth.user.userId} />;
}

function SellerShopInfo({ sellerId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [version, setVersion] = useState(0);
  const [saving, setSaving] = useState(false);
  useEffect(() => {
    const request = new AbortController();
    getMyShop({ signal: request.signal }).then((shop) => {
      if (request.signal.aborted) return;
      if (!shop?.id || shop.sellerId !== sellerId) throw new Error("Thông tin cửa hàng không khớp tài khoản.");
      setResult({ shop });
    }).catch((failure) => {
      if (request.signal.aborted) return;
      if (failure.response?.status === 404 && (failure.response.data?.message === "Không tìm thấy shop" || failure.response.data?.error === "SHOP_NOT_FOUND")) setResult({ shop: null });
      else setResult({ error: authErrorMessage(failure, "Không tải được thông tin cửa hàng.") });
    });
    return () => request.abort();
  }, [sellerId, attempt]);
  function refresh() { if (saving) return; setResult(null); setAttempt((value) => value + 1); }
  function saved(shop) { setResult({ shop }); setVersion((value) => value + 1); }
  return <div className={sellerShopPageStyles.container}>
    <div className={sellerShopPageStyles.container2}><h2 className={sellerShopPageStyles.title}>Thông tin cửa hàng</h2>{result?.shop && <Link to={`/shops/${encodeURIComponent(result.shop.id)}`} className={sellerShopPageStyles.link}>Xem trang cửa hàng</Link>}</div>
    {!result ? <p role="status" className={sellerShopPageStyles.description}>Đang tải cửa hàng của bạn…</p>
      : result.error ? <div role="alert" className={sellerShopPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={sellerShopPageStyles.button}>Thử lại</Button></div>
      : <>
        {result.shop ? <ShopHeader key={`${result.shop.id}:${version}`} shop={result.shop} /> : <p className={sellerShopPageStyles.description2}>Tài khoản của bạn chưa có cửa hàng. Điền thông tin bên dưới để tạo mới.</p>}
        <SellerShopForm key={`${result.shop?.id ?? "new"}:${version}`} shop={result.shop} sellerId={sellerId} onSaved={saved} onBusyChange={setSaving} />
        <p className={sellerShopPageStyles.description3}>Muốn lấy lại dữ liệu từ server? Tải lại sẽ bỏ nội dung chưa lưu.</p>
        <Button variant="outline" type="button" onClick={refresh} disabled={saving} className={sellerShopPageStyles.button2}>Tải lại thông tin cửa hàng</Button>
      </>}
  </div>;
}

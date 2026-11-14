import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Link, Navigate, useOutletContext } from "react-router-dom";
import { getBuyerOrders } from "../../api/order/orderApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import OrderCard from "../../components/order/OrderCard.jsx";
import OrderDetailDialog from "../../components/order/OrderDetailDialog.jsx";
import { orderStatuses, validateBuyerOrder } from "../../components/order/orderHelpers.js";

const ordersPageStyles = {
  description: "mx-auto max-w-5xl px-4 py-12 text-sm text-muted-foreground",
  page: "mx-auto grid max-w-5xl justify-items-start gap-3 px-4 py-12 text-sm",
  errorDescription: "text-red-700",
  button: "min-h-11 rounded-lg border border-input px-4",
  button2: "min-h-11 text-primary underline",
  page2: "mx-auto grid w-full max-w-5xl gap-8 px-4 py-10 sm:px-6 lg:px-8",
  container: "flex flex-wrap items-center justify-between gap-3",
  description2: "text-sm font-semibold text-primary",
  heading: "mt-1 text-2xl font-extrabold sm:text-3xl",
  description3: "mt-2 text-sm text-muted-foreground",
  link: "inline-flex min-h-11 items-center rounded-lg border border-border bg-card px-4 text-sm font-semibold",
  label: "flex flex-wrap items-center gap-3 text-sm font-semibold",
  select: "min-h-11 rounded-lg border border-input bg-card px-3",
  button3: "min-h-11 rounded-lg border border-border bg-card px-4 text-sm font-semibold disabled:opacity-50",
  description4: "py-8 text-sm text-muted-foreground",
  errorContainer: "grid justify-items-start gap-3 rounded-xl border border-red-200 bg-card p-5 text-sm text-red-700",
  button4: "min-h-11 rounded-lg border border-red-200 px-4",
  description5: "rounded-2xl border border-border bg-card p-8 text-center text-sm text-muted-foreground",
  ul: "grid gap-4",
};

export default function OrdersPage() {
  const { auth } = useOutletContext();
  if (auth.checking || auth.signingOut) return <p role="status" className={ordersPageStyles.description}>Đang xác minh tài khoản…</p>;
  if (auth.sessionError) return <div role="alert" className={ordersPageStyles.page}>
    <p className={ordersPageStyles.errorDescription}>{auth.sessionError}</p>
    <Button variant="outline" type="button" onClick={auth.retry} className={ordersPageStyles.button}>Thử lại</Button>
    <Button variant="outline" type="button" onClick={() => void auth.signOut()} className={ordersPageStyles.button2}>Đăng nhập lại</Button>
  </div>;
  if (!auth.user) return <Navigate to="/login" state={{ returnToOrders: true }} replace />;
  return <BuyerOrders key={auth.user.userId} buyerId={auth.user.userId} />;
}

function BuyerOrders({ buyerId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [selected, setSelected] = useState(null);
  const [status, setStatus] = useState("");
  useEffect(() => {
    const request = new AbortController();
    getBuyerOrders({ signal: request.signal }).then((orders) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(orders)) throw new Error("Danh sách đơn hàng không hợp lệ.");
      const checked = orders.map((order) => validateBuyerOrder(order, buyerId));
      setResult({ orders: checked.toSorted((a, b) => (Date.parse(b.createdAt) || 0) - (Date.parse(a.createdAt) || 0)) });
    }).catch((failure) => {
      if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được danh sách đơn hàng.") });
    });
    return () => request.abort();
  }, [buyerId, attempt]);
  function refresh() { setResult(null); setAttempt((value) => value + 1); }
  const orders = result?.orders?.filter((order) => !status || order.orderStatus === status) ?? [];
  return <div className={ordersPageStyles.page2}>
    <div className={ordersPageStyles.container}>
      <div><p className={ordersPageStyles.description2}>Tài khoản của bạn</p><h1 className={ordersPageStyles.heading}>Đơn hàng của tôi</h1><p className={ordersPageStyles.description3}>Chọn một đơn hàng để xem chi tiết và các thao tác khả dụng.</p></div>
      <Link to="/" className={ordersPageStyles.link}>Tiếp tục mua sắm</Link>
    </div>
    <div className={ordersPageStyles.container}>
      <label className={ordersPageStyles.label}>Trạng thái
        <select value={status} onChange={(event) => setStatus(event.target.value)} className={ordersPageStyles.select}><option value="">Tất cả</option>{Object.entries(orderStatuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
      </label>
      <Button variant="outline" type="button" onClick={refresh} disabled={!result} className={ordersPageStyles.button3}>Tải lại</Button>
    </div>
    {!result ? <p role="status" className={ordersPageStyles.description4}>Đang tải đơn hàng…</p> : result.error ? <div role="alert" className={ordersPageStyles.errorContainer}><p>{result.error}</p><Button variant="outline" type="button" onClick={refresh} className={ordersPageStyles.button4}>Thử lại</Button></div>
      : !orders.length ? <p className={ordersPageStyles.description5}>{result.orders.length ? "Không có đơn hàng ở trạng thái này." : "Bạn chưa có đơn hàng nào."}</p>
      : <ul className={ordersPageStyles.ul}>{orders.map((order) => <OrderCard key={order.id} order={order} onOpen={setSelected} />)}</ul>}
    {selected && <OrderDetailDialog key={selected} orderId={selected} buyerId={buyerId} onClose={() => setSelected(null)} onChanged={refresh} />}
  </div>;
}

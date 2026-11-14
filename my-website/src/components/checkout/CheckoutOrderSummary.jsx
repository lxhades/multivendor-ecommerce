import { money } from "../product/productHelpers.js";

export default function CheckoutOrderSummary({ lines, orders }) {
  const subtotal = orders?.length ? orders.reduce((sum, order) => sum + order.subtotal, 0) : lines.reduce((sum, line) => sum + line.unitPrice * line.quantity, 0);
  const shipping = orders?.length ? orders.reduce((sum, order) => sum + order.shippingFee, 0) : 0;
  const total = orders?.length ? orders.reduce((sum, order) => sum + order.totalAmount, 0) : subtotal + shipping;
  const displayLines = orders?.length ? orders.flatMap((order) => order.items.map((item) => ({ ...item, variantName: item.variantName || lines.find((line) => line.variantId === item.variantId)?.variantName }))) : lines;
  return <section className="grid gap-4 border-t border-border/60 pt-5">
    <h3 className="font-bold text-foreground">Thông tin đơn hàng</h3>
    <ul className="divide-y divide-border">
      {displayLines.map((line) => <li key={line.id ?? line.variantId} className="grid gap-3 py-4">
        <div><p className="text-sm font-semibold break-words text-foreground">{line.productName}</p><p className="mt-1 text-xs break-words text-muted-foreground">{line.variantName}</p></div>
        <dl className="grid grid-cols-3 gap-2 text-xs sm:text-sm">
          <div><dt className="text-muted-foreground">Số lượng</dt><dd className="mt-1 font-medium">{line.quantity}</dd></div>
          <div><dt className="text-muted-foreground">Đơn giá</dt><dd className="mt-1 font-medium">{money.format(line.unitPrice)}</dd></div>
          <div className="text-right"><dt className="text-muted-foreground">Thành tiền</dt><dd className="mt-1 font-bold text-foreground">{money.format(line.unitPrice * line.quantity)}</dd></div>
        </dl>
      </li>)}
    </ul>
    <dl className="grid gap-3 rounded-xl bg-background p-4 text-sm">
      <div className="flex justify-between gap-3"><dt>Tiền hàng</dt><dd>{money.format(subtotal)}</dd></div>
      <div className="flex justify-between gap-3"><dt>Phí vận chuyển</dt><dd>{money.format(shipping)}</dd></div>
      <div className="flex flex-wrap justify-between gap-3 border-t border-border pt-3 font-bold"><dt>{orders?.length ? "Tổng tiền đơn hàng" : "Tổng tạm tính"}</dt><dd className="text-xl text-primary">{money.format(total)}</dd></div>
    </dl>
    {!orders?.length && <p className="text-xs text-muted-foreground">Giá và tồn kho được xác nhận lại khi đặt hàng.</p>}
    {orders?.length > 0 && <p className="text-xs text-muted-foreground">Đơn hàng của bạn được chia thành {orders.length} đơn theo cửa hàng.</p>}
  </section>;
}

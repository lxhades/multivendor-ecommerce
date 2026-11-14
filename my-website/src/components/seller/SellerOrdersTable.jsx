import { Button } from "@/components/ui/button";
import { money } from "../product/productHelpers.js";
import { orderDate, orderStatuses, paymentStatuses } from "../order/orderHelpers.js";

export default function SellerOrdersTable({ orders, onOpen }) {
  return <div className="overflow-x-auto rounded-2xl border border-border bg-card"><table className="w-full min-w-[960px] text-left text-sm">
    <caption className="sr-only">Đơn hàng của cửa hàng</caption>
    <thead className="border-b border-border bg-background text-xs text-muted-foreground"><tr>{["Mã đơn / Ngày đặt", "Người nhận", "Sản phẩm", "Tổng tiền", "Trạng thái", "Thao tác"].map((label) => <th key={label} scope="col" className="px-4 py-4 font-semibold">{label}</th>)}</tr></thead>
    <tbody className="divide-y divide-border">{orders.map((order) => <tr key={order.id} className="align-top hover:bg-background/50">
      <td className="max-w-52 px-4 py-4"><p className="break-all font-semibold">{order.id}</p><p className="mt-2 text-xs text-muted-foreground">{orderDate(order.createdAt)}</p></td>
      <td className="max-w-52 px-4 py-4"><p className="line-clamp-3 whitespace-pre-line break-words text-xs leading-6 text-muted-foreground">{order.shippingAddress || "Chưa có thông tin"}</p></td>
      <td className="max-w-60 px-4 py-4"><p className="break-words">{order.items[0]?.productName || "Sản phẩm"}</p><p className="mt-1 text-xs text-muted-foreground">{order.items.length} mặt hàng · {order.items.reduce((sum, item) => sum + item.quantity, 0)} sản phẩm</p></td>
      <td className="whitespace-nowrap px-4 py-4"><p className="font-semibold text-primary">{money.format(order.totalAmount)}</p><p className="mt-1 text-xs text-muted-foreground">{order.paymentMethod}</p></td>
      <td className="px-4 py-4"><p className={`font-semibold ${order.orderStatus === "CANCELLED" ? "text-red-700" : order.orderStatus === "DELIVERED" ? "text-green-700" : "text-foreground"}`}>{orderStatuses[order.orderStatus] ?? order.orderStatus}</p><p className="mt-1 text-xs text-muted-foreground">{paymentStatuses[order.paymentStatus] ?? order.paymentStatus}</p></td>
      <td className="px-4 py-4"><Button variant="outline" type="button" onClick={() => onOpen(order.id)} className="min-h-11 whitespace-nowrap rounded-lg border border-primary/25 px-3 font-semibold text-primary hover:bg-accent">Chi tiết</Button></td>
    </tr>)}</tbody>
  </table></div>;
}

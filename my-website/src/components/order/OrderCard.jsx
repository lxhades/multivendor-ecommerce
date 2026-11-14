import { Package, ArrowUpRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { money } from "../product/productHelpers.js";
import { orderDate, orderStatuses, paymentStatuses } from "./orderHelpers.js";
const orderCardStyles = {
  card:"group grid w-full gap-5 rounded-2xl border border-border bg-card p-5 text-left transition-colors hover:border-primary/50 sm:p-6",
  header:"flex w-full flex-wrap items-start justify-between gap-3",
  identity:"flex min-w-0 items-start gap-3",
  icon:"grid size-10 shrink-0 place-items-center rounded-xl bg-accent text-primary",
  id:"break-all text-sm font-bold",
  date:"mt-1.5 text-xs text-muted-foreground",
  lines:"grid gap-2 rounded-xl bg-background p-4 text-sm",
  line:"break-words leading-6",
  note:"text-xs text-muted-foreground",
  footer:"flex w-full flex-wrap items-center justify-between gap-3 border-t border-border pt-4",
  total:"text-right",
  amount:"text-lg font-extrabold",
  detail:"mt-1 flex items-center justify-end gap-1 text-xs font-semibold text-primary",
};
export default function OrderCard({order,onOpen}) {
  return <li><button type="button" onClick={() => onOpen(order.id)} className={orderCardStyles.card}><div className={orderCardStyles.header}><div className={orderCardStyles.identity}><span className={orderCardStyles.icon}><Package className="size-5" aria-hidden="true" /></span><div className="min-w-0"><h2 className={orderCardStyles.id}>Đơn hàng #{order.id}</h2><p className={orderCardStyles.date}>Đặt lúc {orderDate(order.createdAt)}</p></div></div><Badge variant={order.orderStatus === "CANCELLED" ? "destructive" : order.orderStatus === "DELIVERED" ? "secondary" : "outline"}>{orderStatuses[order.orderStatus] ?? order.orderStatus}</Badge></div><div className={orderCardStyles.lines}>{order.items.slice(0,2).map((item,index) => <p key={item.id ?? index} className={orderCardStyles.line}>{item.productName || "Sản phẩm"}{item.variantName ? ` · ${item.variantName}` : ""} × {item.quantity}</p>)}{order.items.length > 2 && <p className={orderCardStyles.note}>Và {order.items.length - 2} mặt hàng khác</p>}</div><div className={orderCardStyles.footer}><p className={orderCardStyles.note}>{order.paymentMethod === "COD" ? "Thanh toán khi nhận hàng" : order.paymentMethod} · {paymentStatuses[order.paymentStatus] ?? order.paymentStatus}</p><div className={orderCardStyles.total}><p className={orderCardStyles.amount}>{money.format(order.totalAmount)}</p><span className={orderCardStyles.detail}>Xem chi tiết <ArrowUpRight className="size-3.5" aria-hidden="true" /></span></div></div></button></li>;
}

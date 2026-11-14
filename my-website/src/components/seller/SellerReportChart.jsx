import { money } from "../product/productHelpers.js";
import { periodLabel } from "./sellerReportHelpers.js";

export default function SellerReportChart({ report, kind }) {
  const demand = kind === "demand";
  const max = Math.max(1, ...report.periods.map((period) => demand ? period.totalOrders : period.revenue));
  return <figure className="min-w-0 rounded-2xl border border-border bg-card p-4 sm:p-5" aria-label={demand ? "Biểu đồ đơn đặt và đơn hủy theo thời gian" : "Biểu đồ doanh thu tiền hàng theo thời gian"}>
    <figcaption className="font-bold">{demand ? "Đơn đặt theo kỳ" : "Doanh thu tiền hàng theo kỳ"}</figcaption>
    <div className="mt-2 flex flex-wrap gap-4 text-xs text-muted-foreground">{demand ? <><span>🟠 Không hủy</span><span>🔴 Đã hủy</span></> : <span>🟠 Tiền hàng của đơn đã giao</span>}<span>Cuộn ngang để xem các kỳ; số liệu đầy đủ ở bảng bên dưới.</span></div>
    <div className="mt-4 overflow-x-auto">
      <div className="flex min-w-full items-end gap-3 border-b border-border px-2 pt-2" aria-hidden="true">
        {report.periods.map((period) => <div key={period.periodStart} className="w-28 shrink-0 text-center" title={`${periodLabel(period, report)}: ${demand ? `${period.totalOrders} đơn, hủy ${period.cancelledOrders}` : money.format(period.revenue)}`}>
          <div className="flex h-40 items-end justify-center"><div className="flex w-10 flex-col justify-end overflow-hidden rounded-t" style={{ height: `${(demand ? period.totalOrders : period.revenue) / max * 100}%` }}>
            {demand ? <><div className="bg-red-500" style={{ height: `${period.totalOrders ? period.cancelledOrders / period.totalOrders * 100 : 0}%` }} /><div className="min-h-0 flex-1 bg-pumpkin" /></> : <div className="h-full bg-pumpkin" />}
          </div></div>
          <p className="mt-2 text-[10px] leading-4 text-muted-foreground">{periodLabel(period, report)}</p><p className="pb-2 pt-1 text-xs font-semibold text-foreground">{demand ? period.totalOrders.toLocaleString("vi-VN") : money.format(period.revenue)}</p>
        </div>)}
      </div>
    </div>
  </figure>;
}

export const reportGroups = { DAY: "Ngày", WEEK: "Tuần", MONTH: "Tháng", YEAR: "Năm" };
export function vietnamToday() {
  const parts = new Intl.DateTimeFormat("en", { timeZone: "Asia/Ho_Chi_Minh", year: "numeric", month: "2-digit", day: "2-digit" }).formatToParts(new Date());
  const value = (type) => parts.find((part) => part.type === type).value;
  return `${value("year")}-${value("month")}-${value("day")}`;
}
export function initialReportRange() { const to = vietnamToday(); return { from: `${to.slice(0, 7)}-01`, to }; }
export function reportDate(value) { return typeof value === "string" ? value.split("-").reverse().join("/") : ""; }
export function periodLabel(period, report) {
  const start = period.periodStart < report.from ? report.from : period.periodStart;
  const end = period.periodEnd > report.to ? report.to : period.periodEnd;
  return start === end ? reportDate(start) : `${reportDate(start)} – ${reportDate(end)}`;
}
export function validateReportRange(from, to) {
  const valid = (value) => /^\d{4}-\d{2}-\d{2}$/.test(value) && Number.isFinite(Date.parse(`${value}T00:00:00Z`)) && new Date(`${value}T00:00:00Z`).toISOString().slice(0, 10) === value;
  if (!valid(from) || !valid(to) || from > to || from < "0001-01-01" || to > "9998-12-31" || (Date.parse(`${to}T00:00:00Z`) - Date.parse(`${from}T00:00:00Z`)) / 86400000 > 3660) {
    throw new Error("Chọn khoảng ngày hợp lệ, từ ngày không sau đến ngày, tối đa 3.661 ngày.");
  }
}
const count = (value) => Number.isSafeInteger(value) && value >= 0;
const amount = (value) => typeof value === "number" && Number.isFinite(value) && value >= 0 && value <= Number.MAX_SAFE_INTEGER;
function demandRow(row) {
  return row && [row.totalOrders, row.nonCancelledOrders, row.cancelledOrders, row.totalQuantity, row.nonCancelledQuantity, row.cancelledQuantity].every(count) &&
    row.totalOrders === row.nonCancelledOrders + row.cancelledOrders && row.totalQuantity === row.nonCancelledQuantity + row.cancelledQuantity &&
    amount(row.cancellationRate) && row.cancellationRate <= 100;
}
function salesRow(row) { return row && [row.deliveredOrders, row.soldQuantity].every(count) && [row.revenue, row.shippingFee, row.totalOrderAmount].every(amount); }
export function validateSellerReport(data, shopId, filters, kind, variantPage) {
  if (!data || data.shopId !== shopId || data.from !== filters.from || data.to !== filters.to || data.groupBy !== filters.groupBy || data.timezone !== "Asia/Ho_Chi_Minh" || !Array.isArray(data.periods)) throw new Error("Báo cáo không khớp cửa hàng hoặc bộ lọc.");
  const check = kind === "demand" ? demandRow : salesRow;
  if (!check(data.summary) || data.periods.some((period) => !check(period) || !/^\d{4}-\d{2}-\d{2}$/.test(period.periodStart) || !/^\d{4}-\d{2}-\d{2}$/.test(period.periodEnd) || period.periodStart > period.periodEnd)) throw new Error("Số liệu báo cáo không hợp lệ.");
  if (kind === "sales" && (data.dateBasis !== "updatedAt" || data.variantPage !== variantPage || data.variantSize !== 20 || !count(data.totalVariants) || !count(data.totalVariantPages) || !Array.isArray(data.bestSellingVariants) || data.bestSellingVariants.some((item) => !item?.productId || !item.variantId || !count(item.soldQuantity) || !amount(item.revenue)))) throw new Error("Dữ liệu doanh thu hoặc phân trang sản phẩm không hợp lệ.");
  return data;
}

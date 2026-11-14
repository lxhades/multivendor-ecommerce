import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "../common/toast.js";

export default function SellerVariantStock({ stock, loading, error, editing, disabled, pending, onStart, onCancel, onConfirm }) {
  if (loading) return <p role="status" className="mt-4 text-xs text-muted-foreground">Đang tải tồn kho…</p>;
  if (error) return <p role="alert" className="mt-4 text-xs text-red-700">Chưa tải được tồn kho. Hãy bấm Tải lại tồn kho phía trên danh sách biến thể.</p>;
  if (!stock) return <p className="mt-4 text-xs text-amber-800">Biến thể chưa có bản ghi tồn kho. Cần khởi tạo kho trước khi điều chỉnh.</p>;
  function submit(event) {
    event.preventDefault();
    if (pending) return;
    const text = String(new FormData(event.currentTarget).get("quantity") ?? "").trim();
    const newQuantity = Number(text);
    if (!/^\d+$/.test(text) || !Number.isSafeInteger(newQuantity) || newQuantity < stock.reservedQuantity) {
      toast.error(`Tồn thực tế phải là số nguyên từ ${stock.reservedQuantity.toLocaleString("vi-VN")} đến ${Number.MAX_SAFE_INTEGER.toLocaleString("vi-VN")}.`); return;
    }
    if (newQuantity === stock.onHandQuantity) { toast.error("Số lượng mới chưa thay đổi."); return; }
    onConfirm(newQuantity);
  }
  return <div className="mt-4 grid gap-3 rounded-xl bg-background p-3">
    <dl className="grid grid-cols-3 gap-3 text-xs sm:text-sm">
      <div><dt className="text-muted-foreground">Tồn thực tế</dt><dd className="mt-1 font-bold">{stock.onHandQuantity.toLocaleString("vi-VN")}</dd></div>
      <div><dt className="text-muted-foreground">Đang giữ chỗ</dt><dd className="mt-1 font-bold text-amber-700">{stock.reservedQuantity.toLocaleString("vi-VN")}</dd></div>
      <div><dt className="text-muted-foreground">Có thể bán</dt><dd className="mt-1 font-bold text-green-700">{stock.availableQuantity.toLocaleString("vi-VN")}</dd></div>
    </dl>
    {editing ? <form key={`${stock.id}:${stock.updatedAt}`} onSubmit={submit} className="grid gap-3 border-t border-border pt-3">
      <fieldset disabled={pending} className="grid gap-3">
        <label className="grid gap-2 text-sm font-semibold">Tồn thực tế mới<Input name="quantity" type="number" inputMode="numeric" min={stock.reservedQuantity} max={Number.MAX_SAFE_INTEGER} step="1" required defaultValue={stock.onHandQuantity} className="min-h-11 min-w-0 rounded-lg border border-input bg-card px-3" /></label>
        <p className="text-xs leading-6 text-muted-foreground">Đây là tổng tồn thực tế sau điều chỉnh. Không thay đổi lượng đang giữ chỗ; tồn mới phải từ {stock.reservedQuantity.toLocaleString("vi-VN")} trở lên.</p>
        <div className="flex flex-wrap gap-3"><Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">Lưu tồn kho</Button><Button variant="outline" type="button" onClick={onCancel} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Hủy</Button></div>
      </fieldset>
    </form> : <Button variant="outline" type="button" disabled={disabled || pending} onClick={onStart} className="min-h-11 w-fit rounded-lg border border-primary/25 bg-card px-3 text-sm font-semibold text-primary disabled:opacity-50">Điều chỉnh tồn kho</Button>}
  </div>;
}

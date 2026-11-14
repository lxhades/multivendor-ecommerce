import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { isDefaultAddress } from "../checkout/checkoutModel.js";
import { toast } from "../common/toast.js";

export default function AddressForm({ address, pending, onSave, onCancel }) {
  function submit(event) {
    event.preventDefault();
    if (pending) return;
    const form = new FormData(event.currentTarget);
    const input = Object.fromEntries(["receiverName", "phone", "location", "detail"].map((name) => [name, String(form.get(name) ?? "").trim()]));
    if (!input.receiverName || input.receiverName.length > 100 || !/^0[0-9]{9}$/.test(input.phone) || !input.location || input.location.length > 255 || !input.detail || input.detail.length > 500) {
      toast.error("Vui lòng nhập đầy đủ thông tin địa chỉ và số điện thoại 10 số bắt đầu bằng 0."); return;
    }
    void onSave({ ...input, isDefault: form.get("isDefault") === "on" });
  }
  return <form onSubmit={submit} aria-busy={pending} className="mt-5 rounded-xl border border-primary/25 bg-accent/30 p-4">
    <h3 className="mb-4 font-bold">{address ? "Sửa địa chỉ" : "Thêm địa chỉ mới"}</h3>
    <fieldset disabled={pending} className="grid gap-4 sm:grid-cols-2">
      <label className="grid gap-2 text-sm font-semibold">Tên người nhận
        <Input autoFocus name="receiverName" autoComplete="shipping name" defaultValue={address?.receiverName ?? ""} required maxLength={100} className="min-h-12 min-w-0 rounded-lg border border-input bg-card px-3" />
      </label>
      <label className="grid gap-2 text-sm font-semibold">Số điện thoại
        <Input name="phone" type="tel" autoComplete="shipping tel" defaultValue={address?.phone ?? ""} required pattern="0[0-9]{9}" maxLength={10} title="10 chữ số bắt đầu bằng 0" className="min-h-12 min-w-0 rounded-lg border border-input bg-card px-3" />
      </label>
      <label className="grid gap-2 text-sm font-semibold sm:col-span-2">Tỉnh/thành phố, quận/huyện, phường/xã
        <Input name="location" defaultValue={address?.location ?? ""} required maxLength={255} className="min-h-12 min-w-0 rounded-lg border border-input bg-card px-3" />
      </label>
      <label className="grid gap-2 text-sm font-semibold sm:col-span-2">Địa chỉ chi tiết (số nhà, tên đường)
        <Textarea name="detail" autoComplete="shipping street-address" defaultValue={address?.detail ?? ""} required maxLength={500} rows={2} className="min-w-0 rounded-lg border border-input bg-card p-3" />
      </label>
      <label className="flex min-h-11 items-center gap-2 text-sm sm:col-span-2"><input type="checkbox" name="isDefault" defaultChecked={address ? isDefaultAddress(address) : false} className="size-4 accent-primary" />Đặt làm địa chỉ mặc định</label>
      <div className="flex flex-wrap gap-3 sm:col-span-2">
        <Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">{pending ? "Đang lưu…" : "Lưu địa chỉ"}</Button>
        <Button variant="outline" type="button" onClick={onCancel} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Hủy</Button>
      </div>
    </fieldset>
  </form>;
}

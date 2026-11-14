import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useEffect, useState } from "react";
import { getAddresses } from "../../api/auth/addressApi.js";
import { defaultAddressChoice, isDefaultAddress } from "./checkoutModel.js";

const field = "min-h-12 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm read-only:bg-background";
const noop = () => {};

export default function CheckoutAddressFields({ buyerId, onReady = noop }) {
  return <UserAddresses key={buyerId} buyerId={buyerId} onReady={onReady} />;
}

function UserAddresses({ buyerId, onReady }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    if (!buyerId) return;
    const request = new AbortController();
    getAddresses(buyerId, { signal: request.signal }).then((addresses) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(addresses) || addresses.some((address) => !address || address.addressId == null)) throw new Error("Phản hồi địa chỉ không hợp lệ.");
      setResult({ addresses });
      onReady(true);
    }).catch((error) => {
      if (request.signal.aborted) return;
      setResult({ addresses: [], error: error.response?.data?.message || "Chưa tải được địa chỉ đã lưu. Bạn vẫn có thể nhập địa chỉ mới." });
      onReady(true);
    });
    return () => request.abort();
  }, [buyerId, attempt, onReady]);

  if (!buyerId) return <p role="alert" className="text-sm text-red-700">Chưa xác định được tài khoản nhận hàng.</p>;
  if (!result) return <p role="status" className="text-sm text-muted-foreground">Đang tải địa chỉ nhận hàng…</p>;
  function retry() { onReady(false); setResult(null); setAttempt((value) => value + 1); }
  return <CheckoutAddressForm addresses={result.addresses} error={result.error} retry={retry} />;
}

export function CheckoutAddressForm({ addresses, error, retry }) {
  const [selected, setSelected] = useState(null);
  const [draft, setDraft] = useState({ receiver: "", phone: "", address: "" });
  const choice = selected === "new" || addresses.some((address) => String(address.addressId) === selected)
    ? selected : defaultAddressChoice(addresses);
  const saved = addresses.find((address) => String(address.addressId) === choice);
  const values = saved ? { receiver: saved.receiverName ?? "", phone: saved.phone ?? "", address: [saved.detail, saved.location].filter(Boolean).join(", ") } : draft;
  function update(event) { setDraft((current) => ({ ...current, [event.target.name]: event.target.value })); }

  return <section className="grid gap-4">
    <h3 className="font-bold text-foreground">Thông tin người nhận</h3>
    {error && <div role="alert" className="text-sm text-amber-800">{error} <Button variant="outline" type="button" onClick={retry} className="min-h-11 rounded px-2 underline">Thử lại</Button></div>}
    {addresses.length > 0 ? <label className="grid gap-2 text-sm font-semibold text-foreground">Địa chỉ nhận hàng
      <select value={choice} onChange={(event) => setSelected(event.target.value)} className={field}>
        {addresses.map((address) => <option key={address.addressId} value={String(address.addressId)}>{address.receiverName} · {address.detail}, {address.location}{isDefaultAddress(address) ? " (Mặc định)" : ""}</option>)}
        <option value="new">Nhập địa chỉ mới</option>
      </select>
    </label> : !error && <p className="text-sm text-muted-foreground">Bạn chưa có địa chỉ đã lưu. Vui lòng nhập thông tin nhận hàng bên dưới.</p>}
    <div className="grid gap-4 sm:grid-cols-2">
      <label className="grid gap-2 text-sm font-semibold text-foreground">Người nhận
        <Input name="receiver" autoComplete="shipping name" className={field} value={values.receiver} onChange={update} readOnly={Boolean(saved)} required maxLength={100} />
      </label>
      <label className="grid gap-2 text-sm font-semibold text-foreground">Số điện thoại
        <Input name="phone" type="tel" autoComplete="shipping tel" className={field} value={values.phone} onChange={update} readOnly={Boolean(saved)} required pattern="0[0-9]{9}" maxLength={10} title="10 chữ số bắt đầu bằng 0" />
      </label>
      <label className="grid gap-2 text-sm font-semibold text-foreground sm:col-span-2">Địa chỉ nhận hàng
        <Textarea name="address" autoComplete="shipping street-address" className={field + " py-3"} rows={2} value={values.address} onChange={update} readOnly={Boolean(saved)} required maxLength={500} />
      </label>
    </div>
    {!saved && <p className="text-xs text-muted-foreground">Địa chỉ mới chỉ dùng cho đơn hàng này.</p>}
  </section>;
}

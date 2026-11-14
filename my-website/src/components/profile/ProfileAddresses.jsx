import { Button } from "@/components/ui/button";
import { useEffect, useRef, useState } from "react";
import { createAddress, updateAddress, deleteAddress, getAddresses } from "../../api/auth/addressApi.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { isDefaultAddress } from "../checkout/checkoutModel.js";

import AddressForm from "./AddressForm.jsx";
import ConfirmDialog from "../common/ConfirmDialog.jsx";
import { toast } from "../common/toast.js";

export default function ProfileAddresses({ userId }) {
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [editor, setEditor] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [pending, setPending] = useState(false);
  const mutation = useRef(null);
  const lock = useRef(false);
  useEffect(() => () => mutation.current?.abort(), []);

  async function mutate(operation, message) {
    if (lock.current) return;
    lock.current = true; setPending(true);
    const request = new AbortController(); mutation.current = request;
    try {
      await operation(request.signal);
      if (request.signal.aborted) return;
      setEditor(null); setDeleting(null);
      toast.success(message);
      setResult(null); setAttempt((value) => value + 1);
    } catch (failure) {
      if (!request.signal.aborted) toast.error(authErrorMessage(failure, "Không cập nhật được địa chỉ. Vui lòng thử lại."));
    } finally {
      lock.current = false;
      if (!request.signal.aborted) setPending(false);
      mutation.current = null;
    }
  }

  function save(input) {
    return mutate((signal) => editor.address
      ? updateAddress(userId, editor.address.addressId, input, { signal })
      : createAddress(userId, input, { signal }), editor.address ? "Cập nhật địa chỉ thành công." : "Thêm địa chỉ thành công.");
  }
  function remove() {
    if (!deleting) return;
    void mutate((signal) => deleteAddress(userId, deleting.addressId, { signal }), "Xóa địa chỉ thành công.");
  }
  useEffect(() => {
    const request = new AbortController();
    getAddresses(userId, { signal: request.signal }).then((addresses) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(addresses) || addresses.some((address) => !address || address.addressId == null)) {
        throw new Error("Dữ liệu địa chỉ không hợp lệ.");
      }
      setResult({ addresses });
    }).catch((failure) => {
      if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được địa chỉ nhận hàng.") });
    });
    return () => request.abort();
  }, [userId, attempt]);

  function retry() { setResult(null); setAttempt((value) => value + 1); }
  const addresses = result?.addresses?.toSorted((a, b) => Number(isDefaultAddress(b)) - Number(isDefaultAddress(a)));
  return <section aria-labelledby="profile-addresses-title" className="rounded-2xl border border-border bg-card p-5 sm:p-7">
    <div className="flex flex-wrap items-center justify-between gap-3">
      <h2 id="profile-addresses-title" className="text-lg font-bold text-foreground">Địa chỉ nhận hàng</h2>
      <Button variant="default" type="button" disabled={pending || !result?.addresses || Boolean(editor)} onClick={() => setEditor({ address: null })} className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">Thêm địa chỉ</Button>
    </div>
    <p className="mt-1 text-sm text-muted-foreground">Các địa chỉ đã lưu trong tài khoản của bạn.</p>
    {editor && <AddressForm key={editor.address?.addressId ?? "new"} address={editor.address} pending={pending} onSave={save} onCancel={() => { if (!lock.current) setEditor(null); }} />}
    <ConfirmDialog isOpen={Boolean(deleting)} title="Xóa địa chỉ nhận hàng?" message={`Bạn muốn xóa địa chỉ của ${deleting?.receiverName ?? "người nhận"}?`} confirmText={pending ? "Đang xóa…" : "Xóa địa chỉ"} busy={pending} onConfirm={remove} onCancel={() => { if (!lock.current) setDeleting(null); }} />
    {!result ? <p role="status" className="mt-5 text-sm text-muted-foreground">Đang tải địa chỉ…</p>
      : result.error ? <div role="alert" className="mt-5 grid justify-items-start gap-2 text-sm text-red-700">
        <p>{result.error}</p>
        <Button variant="outline" type="button" onClick={retry} className="min-h-11 rounded-lg border border-red-200 px-4 font-semibold">Thử lại</Button>
      </div>
      : !addresses.length ? <p className="mt-5 rounded-xl bg-background p-4 text-sm text-muted-foreground">Bạn chưa có địa chỉ nhận hàng đã lưu.</p>
      : <ul className="mt-5 grid gap-4">
        {addresses.map((address) => <li key={address.addressId} className={`rounded-xl border p-4 ${isDefaultAddress(address) ? "border-primary/25 bg-accent/50" : "border-border"}`}>
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="break-words font-semibold text-foreground">{address.receiverName || "Chưa có tên người nhận"}</h3>
            {isDefaultAddress(address) && <span className="rounded-full bg-accent px-2 py-1 text-xs font-semibold text-accent-foreground">Mặc định</span>}
          </div>
          <p className="mt-2 text-sm text-muted-foreground">{address.phone || "Chưa có số điện thoại"}</p>
          <p className="mt-1 whitespace-pre-line break-words text-sm leading-6 text-muted-foreground">{[address.detail, address.location].filter(Boolean).join(", ") || "Chưa có thông tin địa chỉ"}</p>
          <div className="mt-3 flex gap-3">
            <Button variant="outline" type="button" disabled={pending || Boolean(editor)} onClick={() => setEditor({ address })} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm font-semibold disabled:opacity-50">Sửa</Button>
            <Button variant="outline" type="button" disabled={pending || Boolean(editor)} onClick={() => setDeleting(address)} className="min-h-11 rounded-lg border border-red-200 bg-card px-4 text-sm font-semibold text-red-700 disabled:opacity-50">Xóa</Button>
          </div>
        </li>)}
      </ul>}
  </section>;
}

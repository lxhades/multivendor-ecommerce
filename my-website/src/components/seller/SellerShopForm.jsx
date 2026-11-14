import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useEffect, useRef, useState } from "react";
import { createSellerShop, updateSellerShop } from "../../api/shop/sellerShopApi.js";
import { uploadMedia } from "../../api/media/mediaApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { toast } from "../common/toast.js";
import SellerShopImageField from "./SellerShopImageField.jsx";

const inputClass = "min-h-12 w-full min-w-0 rounded-lg border border-input bg-card px-3 text-sm";

export default function SellerShopForm({ shop, sellerId, onSaved, onBusyChange }) {
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState("");
  const [logo, setLogo] = useState(null);
  const [banner, setBanner] = useState(null);
  const [retryCreate, setRetryCreate] = useState(false);
  const requestRef = useRef(null);
  const lock = useRef(false);
  const uploads = useRef(new Map());
  useEffect(() => () => requestRef.current?.abort(), []);

  async function submit(event) {
    event.preventDefault();
    if (lock.current || retryCreate || getSession()?.userId !== sellerId) return;
    const form = new FormData(event.currentTarget);
    const input = Object.fromEntries(["name", "slug", "description", "address", "bankDetails"].map((name) => [name, String(form.get(name) ?? "").trim()]));
    if (!input.name || input.name.length > 100 || !/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(input.slug) || input.slug.length > 100 || !input.address || input.address.length > 255 || input.description.length > 1000 || input.bankDetails.length > 500) {
      toast.error("Kiểm tra tên cửa hàng, slug, địa chỉ và độ dài các trường."); return;
    }
    lock.current = true; setPending(true); setError(""); onBusyChange(true);
    const request = new AbortController(); requestRef.current = request;
    let saving = false;
    try {
      async function imageUrl(selection, existing, label) {
        if (!selection) return existing || null;
        if (uploads.current.has(selection.id)) return uploads.current.get(selection.id);
        setProgress(`Đang tải ${label}…`);
        const url = await uploadMedia(selection.file, { domain: "shops", entityId: shop.id, signal: request.signal });
        if (request.signal.aborted) return null;
        if (url.length > 255) throw new Error("URL ảnh vượt giới hạn 255 ký tự của API shop.");
        uploads.current.set(selection.id, url);
        return url;
      }
      const logoUrl = shop ? await imageUrl(logo, shop.logoUrl, "logo") : null;
      const bannerUrl = shop ? await imageUrl(banner, shop.bannerUrl, "banner") : null;
      if (request.signal.aborted || getSession()?.userId !== sellerId) return;
      setProgress(shop ? "Đang lưu thông tin cửa hàng…" : "Đang tạo cửa hàng…");
      saving = true;
      const payload = { ...input, logoUrl, bannerUrl };
      const saved = shop ? await updateSellerShop(shop.id, payload, { signal: request.signal }) : await createSellerShop(payload, { signal: request.signal });
      if (request.signal.aborted || getSession()?.userId !== sellerId) return;
      if (!saved?.id || saved.sellerId !== sellerId || (shop && saved.id !== shop.id)) throw new Error("Chưa xác minh được kết quả lưu cửa hàng. Hãy tải lại thông tin.");
      toast.success(shop ? "Cập nhật cửa hàng thành công." : "Tạo cửa hàng thành công. Bạn có thể thêm logo và banner.");
      onSaved(saved);
    } catch (failure) {
      if (request.signal.aborted || getSession()?.userId !== sellerId) return;
      let message = authErrorMessage(failure, "Không lưu được cửa hàng. Vui lòng thử lại.");
      if (!shop && saving && (!failure.response || failure.response.status >= 500 || failure.response.status === 409)) {
        setRetryCreate(true);
        message += " Hãy tải lại thông tin cửa hàng trước khi thử tạo mới để kiểm tra kết quả.";
      }
      setError(message); toast.error(message);
    } finally {
      lock.current = false;
      if (!request.signal.aborted) { setPending(false); setProgress(""); onBusyChange(false); }
      requestRef.current = null;
    }
  }

  return <form onSubmit={submit} aria-busy={pending} className="rounded-2xl border border-border bg-card p-5 sm:p-7">
    <h3 className="text-lg font-bold">{shop ? "Chỉnh sửa thông tin cửa hàng" : "Tạo cửa hàng"}</h3>
    <fieldset disabled={pending || retryCreate} className="mt-5 grid gap-5">
      <div className="grid gap-4 lg:grid-cols-2">
        <label className="grid gap-2 text-sm font-semibold">Tên cửa hàng<Input name="name" defaultValue={shop?.name ?? ""} required maxLength={100} className={inputClass} /></label>
        <label className="grid gap-2 text-sm font-semibold">Slug<Input name="slug" defaultValue={shop?.slug ?? ""} required maxLength={100} pattern="[a-z0-9]+(-[a-z0-9]+)*" title="Chỉ chữ thường, số và dấu gạch ngang" placeholder="ten-cua-hang" className={inputClass} /><span className="text-xs font-normal text-muted-foreground">Chỉ chữ thường, số và dấu gạch ngang.</span></label>
      </div>
      <label className="grid gap-2 text-sm font-semibold">Địa chỉ<Input name="address" defaultValue={shop?.address ?? ""} required maxLength={255} className={inputClass} /></label>
      <label className="grid gap-2 text-sm font-semibold">Giới thiệu cửa hàng<Textarea name="description" defaultValue={shop?.description ?? ""} maxLength={1000} rows={4} className={inputClass + " py-3"} /><span className="text-xs font-normal text-muted-foreground">Tối đa 1.000 ký tự.</span></label>
      <label className="grid gap-2 text-sm font-semibold">Thông tin ngân hàng<Textarea name="bankDetails" defaultValue={shop?.bankDetails ?? ""} maxLength={500} rows={2} className={inputClass + " py-3"} /><span className="text-xs font-normal text-muted-foreground">Tối đa 500 ký tự. Thông tin này chỉ hiển thị trong quản lý cửa hàng.</span></label>
      {shop ? <div className="grid min-w-0 gap-5 border-t border-border/60 pt-5 lg:grid-cols-2">
        <SellerShopImageField label="Logo cửa hàng" currentUrl={shop.logoUrl} selection={logo} onSelect={setLogo} pending={pending} round />
        <SellerShopImageField label="Banner cửa hàng" currentUrl={shop.bannerUrl} selection={banner} onSelect={setBanner} pending={pending} />
        <p className="text-xs text-muted-foreground lg:col-span-2">Ảnh JPG, PNG, WebP tối đa 10 MB/tệp. Ảnh mới chỉ được upload khi bấm Lưu thay đổi; bỏ ảnh mới sẽ giữ ảnh hiện tại.</p>
      </div> : <p className="rounded-xl bg-accent p-4 text-sm text-accent-foreground">Tạo cửa hàng trước để có mã shop, sau đó thêm logo và banner.</p>}
      <Button variant="default" type="submit" className="min-h-12 w-fit rounded-lg bg-primary px-5 text-sm font-bold text-white hover:bg-primary/90 disabled:opacity-50">{pending ? "Đang xử lý…" : shop ? "Lưu thay đổi" : "Tạo cửa hàng"}</Button>
    </fieldset>
    {progress && <p role="status" className="mt-4 text-sm text-accent-foreground">{progress}</p>}
    {error && <p role="alert" className="mt-4 text-sm text-red-700">{error}</p>}
  </form>;
}

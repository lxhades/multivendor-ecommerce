import { Button } from "@/components/ui/button";
import { useEffect, useId, useRef, useState } from "react";
import { getCategories } from "../../api/category/categoryApi.js";
import { createSellerProduct, updateSellerProduct, updateSellerVariant } from "../../api/product/sellerProductApi.js";
import { uploadMedia } from "../../api/media/mediaApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { registerToastHost, toast } from "../common/toast.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { validateSellerProduct } from "./sellerProductHelpers.js";
import SellerProductEditForm from "./SellerProductEditForm.jsx";

const emptyProduct = { name: "", slug: "", description: "", categoryId: "", mediaJson: "[]" };
export default function SellerCreateProductDialog({ shopId, sellerId, onClose, onCreated, onChanged }) {
  const dialog = useRef(null);
  const titleId = useId();
  const [categories, setCategories] = useState(null);
  const [categoryError, setCategoryError] = useState("");
  const [attempt, setAttempt] = useState(0);
  const [action, setAction] = useState(null);
  const [pending, setPending] = useState(false);
  const [draftCreated, setDraftCreated] = useState(null);
  const [uncertain, setUncertain] = useState(false);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState("");
  const lock = useRef(false);
  const mutation = useRef(null);
  const created = useRef(null);
  const uploaded = useRef(new Map());
  useEffect(() => {
    const node = dialog.current; node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); mutation.current?.abort(); };
  }, []);
  useEffect(() => {
    const request = new AbortController();
    getCategories({ signal: request.signal, skipAuth: true }).then((items) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(items)) throw new Error("Danh mục không hợp lệ.");
      setCategories(items.filter((item) => item.status === "ACTIVE"));
    }).catch((failure) => { if (!request.signal.aborted) setCategoryError(authErrorMessage(failure, "Không tải được danh mục.")); });
    return () => request.abort();
  }, [attempt]);
  function close() {
    if (lock.current) return;
    if (action && !uncertain && !draftCreated) { setAction(null); return; }
    if (draftCreated) { onCreated(draftCreated); return; }
    onChanged(); onClose();
  }
  async function execute() {
    if (lock.current || !action || uncertain || getSession()?.userId !== sellerId) return;
    lock.current = true; setPending(true); setError("");
    const request = new AbortController(); mutation.current = request;
    let dispatched = false;
    try {
      if (!action.input.variants?.length) throw new Error("Sản phẩm phải có ít nhất một biến thể.");
      const firstCreate = !created.current;
      if (firstCreate) {
        setProgress("Đang tạo sản phẩm và biến thể…");
        dispatched = true;
        const response = await createSellerProduct(shopId, { ...action.input, mediaJson: "[]" }, { signal: request.signal });
        if (request.signal.aborted || getSession()?.userId !== sellerId) return;
        created.current = validateSellerProduct(response, shopId);
        setDraftCreated(created.current);
      }
      let product = created.current;
      const urls = [];
      for (const [index, entry] of (action.media ?? []).entries()) {
        if (request.signal.aborted || getSession()?.userId !== sellerId) return;
        let url = entry.url || uploaded.current.get(entry.id);
        if (!url) {
          setProgress(`Đang tải media ${index + 1}/${action.media.length}: ${entry.file.name}`);
          url = await uploadMedia(entry.file, { domain: "products", entityId: product.id, signal: request.signal });
          if (request.signal.aborted || getSession()?.userId !== sellerId) return;
          uploaded.current.set(entry.id, url);
        }
        urls.push(url);
      }
      if (action.media || !firstCreate) {
        setProgress("Đang lưu thông tin và media…");
        product = validateSellerProduct(await updateSellerProduct(shopId, product.id, { ...action.input, mediaJson: JSON.stringify(urls), variants: [] }, { signal: request.signal }), shopId, product.id);
        if (request.signal.aborted || getSession()?.userId !== sellerId) return;
        created.current = product;
        setDraftCreated(product);
      }
      for (const entry of action.variantImages ?? []) {
        if (request.signal.aborted || getSession()?.userId !== sellerId) return;
        const variant = product.variants.find((item) => item.sku === entry.sku && !item.deletedAt);
        const input = action.input.variants.find((item) => item.sku === entry.sku);
        if (!variant || !input) throw new Error(`Không tìm thấy biến thể ${entry.sku} vừa tạo để lưu ảnh.`);
        let url = uploaded.current.get(entry.selection.id);
        if (!url) {
          setProgress(`Đang tải ảnh biến thể ${entry.sku}…`);
          url = await uploadMedia(entry.selection.file, { domain: "products", entityId: product.id, variantId: variant.id, signal: request.signal });
          if (request.signal.aborted || getSession()?.userId !== sellerId) return;
          uploaded.current.set(entry.selection.id, url);
        }
        setProgress(`Đang lưu ảnh biến thể ${entry.sku}…`);
        const response = await updateSellerVariant(shopId, product.id, variant.id, { ...input, imageUrl: url }, { signal: request.signal });
        if (request.signal.aborted || getSession()?.userId !== sellerId) return;
        product = validateSellerProduct(response, shopId, product.id);
        created.current = product; setDraftCreated(product);
      }
      toast.success(`Đã tạo sản phẩm cùng ${action.input.variants.length} biến thể. Kiểm tra tồn kho và trạng thái duyệt trước khi bán.`);
      onCreated(product);
    } catch (failure) {
      if (request.signal.aborted || getSession()?.userId !== sellerId) return;
      if (!created.current && dispatched && (!failure.response || failure.response.status >= 500)) setUncertain(true);
      const message = created.current
        ? "Sản phẩm nháp đã được tạo nhưng chưa lưu xong media/thông tin. Bạn có thể thử lại trên cùng sản phẩm hoặc mở sản phẩm nháp. " + authErrorMessage(failure)
        : authErrorMessage(failure, "Không tạo được sản phẩm. Vui lòng kiểm tra lại.");
      setError(message); toast.error(message); onChanged();
    } finally {
      lock.current = false;
      if (!request.signal.aborted) { setPending(false); setProgress(""); }
      mutation.current = null;
    }
  }
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-4xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50">
    <div className="sticky top-0 z-10 flex items-center justify-between border-b border-border/60 bg-card px-5 py-3"><h2 id={titleId} className="text-xl font-bold">Tạo sản phẩm</h2><Button variant="outline" type="button" disabled={pending} onClick={close} aria-label="Đóng tạo sản phẩm" className="min-h-11 min-w-11 text-2xl">×</Button></div>
    <div className="grid gap-4 p-5 sm:p-7" aria-busy={pending}>
      <p className="text-xs leading-6 text-muted-foreground">Sản phẩm mới mặc định ngừng bán/chờ duyệt. Bắt buộc ít nhất một biến thể; thông tin sản phẩm và các biến thể được tạo trong cùng yêu cầu. Ảnh/video sẽ được lưu sau khi có mã sản phẩm và biến thể.</p>
      {action && <section role="alertdialog" aria-modal="true" aria-label="Xác nhận tạo sản phẩm" className="grid gap-3 rounded-xl border border-primary/25 bg-accent p-4"><p className="font-bold">{draftCreated ? "Lưu tiếp sản phẩm nháp đã tạo?" : "Xác nhận tạo sản phẩm mới?"}</p><p className="break-words text-sm">{action.input.name} · {action.input.variants.length} biến thể</p>{progress && <p role="status" className="text-sm">{progress}</p>}{error && <p role="alert" className="text-sm text-red-700">{error}</p>}{uncertain && <p className="text-sm text-amber-900">Chưa rõ kết quả tạo. Đóng dialog để tải lại danh sách và kiểm tra trước khi tạo lại.</p>}<div className="flex flex-wrap gap-3"><Button variant="outline" autoFocus type="button" disabled={pending} onClick={close} className="min-h-11 rounded-lg border bg-card px-4 text-sm">{uncertain ? "Đóng và tải lại danh sách" : draftCreated ? "Mở sản phẩm đã tạo" : "Quay lại"}</Button><Button variant="default" type="button" disabled={pending || uncertain} onClick={() => void execute()} className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">{pending ? "Đang xử lý…" : "Xác nhận"}</Button>{draftCreated && <Button variant="outline" type="button" disabled={pending} onClick={() => onCreated(draftCreated)} className="min-h-11 text-sm font-semibold text-primary underline">Mở sản phẩm nháp</Button>}</div></section>}
      <div hidden={Boolean(action)}>{!categories ? categoryError ? <div role="alert" className="grid gap-3 text-sm text-red-700"><p>{categoryError}</p><Button variant="outline" type="button" onClick={() => { setCategoryError(""); setAttempt((value) => value + 1); }} className="min-h-11 rounded-lg border px-4">Thử tải danh mục</Button></div> : <p role="status">Đang tải danh mục…</p> : categories.length ? <SellerProductEditForm creating shopId={shopId} product={emptyProduct} categories={categories} pending={pending} onCancel={() => { onChanged(); onClose(); }} onConfirm={(input, media, variantImages) => { setError(""); setAction({ input, media, variantImages }); }} /> : <p>Chưa có danh mục hoạt động để tạo sản phẩm.</p>}</div>
    </div>
  </dialog>;
}

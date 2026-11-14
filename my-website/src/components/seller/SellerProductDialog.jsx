import { Button } from "@/components/ui/button";
import { getSellerInventory, adjustSellerStock } from "../../api/inventory/inventoryApi.js";
import { validateVariantStock } from "./sellerInventoryModel.js";
import SellerVariantStock from "./SellerVariantStock.jsx";
import { useEffect, useId, useRef, useState } from "react";
import { getSellerProduct, createSellerVariant, updateSellerProductStatus, updateSellerProduct, deleteSellerProduct, updateSellerVariant, deleteSellerVariant } from "../../api/product/sellerProductApi.js";
import { getCategories } from "../../api/category/categoryApi.js";
import { uploadMedia } from "../../api/media/mediaApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { isMediaUrl, isVideo, mediaUrls, money, variantName } from "../product/productHelpers.js";
import { approvalStatuses, productStatuses, validateSellerProduct } from "./sellerProductHelpers.js";
import SellerProductEditForm from "./SellerProductEditForm.jsx";
import SellerVariantEditForm from "./SellerVariantEditForm.jsx";

export default function SellerProductDialog({ shopId, productId, sellerId, onClose, onChanged }) {
  // 1. KHU VỰC STATE & REFS
  const dialog = useRef(null);
  const titleId = useId();
  const confirmId = useId();

  // Data State
  const [result, setResult] = useState(null);
  const [categories, setCategories] = useState([]);
  const [categoriesError, setCategoriesError] = useState(false);
  const [attempt, setAttempt] = useState(0);
  const [inventory, setInventory] = useState(null);
  const [inventoryAttempt, setInventoryAttempt] = useState(0);

  // UI State
  const [editor, setEditor] = useState(null); // Trạng thái đang sửa (product / variant)
  const [action, setAction] = useState(null); // Trạng thái chờ xác nhận xóa/lưu
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState("");

  // Refs cho xử lý bất đồng bộ
  const lock = useRef(false);
  const mutation = useRef(null);
  const uploaded = useRef(new Map()); // Cache các file đã upload để không upload lại

  // 2. EFFECTS (Sự kiện & Gọi API ban đầu)
  useEffect(() => {
    const node = dialog.current;
    node.showModal();
    const release = registerToastHost(node);
    return () => {
      release();
      node.close();
      mutation.current?.abort();
    };
  }, []);

  // Load thông tin sản phẩm
  useEffect(() => {
    const request = new AbortController();
    getSellerProduct(shopId, productId, { signal: request.signal })
      .then((product) => {
        if (!request.signal.aborted) {
          setResult({ product: validateSellerProduct(product, shopId, productId) });
        }
      })
      .catch((failure) => {
        if (!request.signal.aborted) {
          setResult({ error: authErrorMessage(failure, "Không tải được chi tiết sản phẩm.") });
        }
      });
    return () => request.abort();
  }, [shopId, productId, attempt]);

  // Load danh mục
  useEffect(() => {
    const request = new AbortController();
    getCategories({ signal: request.signal, skipAuth: true })
      .then((items) => {
        if (request.signal.aborted) return;
        if (!Array.isArray(items)) throw new Error("Danh mục không hợp lệ");
        setCategories(items.filter((item) => item.status === "ACTIVE"));
      })
      .catch(() => {
        if (!request.signal.aborted) setCategoriesError(true);
      });
    return () => request.abort();
  }, []);

  // Read the shop inventory once for all variants in this product.
  useEffect(() => {
    const request = new AbortController();
    getSellerInventory(shopId, { signal: request.signal }).then((records) => {
      if (request.signal.aborted) return;
      if (!Array.isArray(records) || records.some((stock) => !stock || stock.shopId !== shopId)) throw new Error("Danh sách tồn kho không khớp cửa hàng.");
      const stocks = records.filter((stock) => stock.productId === productId).map((stock) => validateVariantStock(stock, shopId, productId));
      if (new Set(stocks.map((stock) => stock.variantId)).size !== stocks.length) throw new Error("Dữ liệu tồn kho biến thể bị trùng.");
      setInventory({ stocks: Object.fromEntries(stocks.map((stock) => [stock.variantId, stock])) });
    }).catch((failure) => {
      if (!request.signal.aborted) setInventory({ error: authErrorMessage(failure, "Không tải được tồn kho.") });
    });
    return () => request.abort();
  }, [shopId, productId, inventoryAttempt]);
  function refreshInventory() { setInventory(null); setInventoryAttempt((value) => value + 1); }

  // 3. HÀM XỬ LÝ LOGIC
  function close() {
    if (!lock.current) {
      if (action) setAction(null);
      else onClose();
    }
  }

  function confirm(nextAction) {
    setError("");
    setAction(nextAction);
  }

  // Hàm phụ: Xử lý upload media
  async function processMediaUpload(mediaEntries, signal) {
    const urls = [];
    for (const [index, entry] of mediaEntries.entries()) {
      if (signal.aborted || getSession()?.userId !== sellerId) return null;

      let url = entry.url || uploaded.current.get(entry.id);
      if (!url) {
        setProgress(`Đang tải media ${index + 1}/${mediaEntries.length}: ${entry.file.name}`);
        url = await uploadMedia(entry.file, { domain: "products", entityId: productId, signal });
        if (signal.aborted || getSession()?.userId !== sellerId) return null;
        uploaded.current.set(entry.id, url);
      }
      urls.push(url);
    }
    return JSON.stringify(urls);
  }

  // Hàm phụ: Xử lý upload ảnh biến thể
  async function processVariantImage(image, variantId, signal) {
    let url = uploaded.current.get(image.id);
    if (!url) {
      setProgress("Đang tải ảnh biến thể…");
      url = await uploadMedia(image.file, { domain: "products", entityId: productId, variantId, signal });
      if (signal.aborted || getSession()?.userId !== sellerId) return null;
      uploaded.current.set(image.id, url);
    }
    return url;
  }

  // Hàm chính thực thi thay đổi
  async function execute() {
    if (lock.current || !action || getSession()?.userId !== sellerId) return;

    lock.current = true;
    setPending(true);
    setError("");

    const request = new AbortController();
    mutation.current = request;

    try {
      let updated;

      if (action.type === "stock") {
        setProgress("Đang điều chỉnh tồn kho…");
        updated = await adjustSellerStock(action.variant.id, action.newQuantity, { signal: request.signal });
      }
      else if (action.type === "deleteProduct") {
        await deleteSellerProduct(shopId, productId, { signal: request.signal });
      }
      else if (action.type === "deleteVariant") {
        updated = await deleteSellerVariant(shopId, productId, action.variant.id, { signal: request.signal });
      }
      else if (action.type === "status") {
        updated = await updateSellerProductStatus(shopId, productId, action.status, { signal: request.signal });
      }
      else if (action.type === "createVariant") {
        const input = { ...action.input };
        if (action.image) {
          const imageUrl = await processVariantImage(action.image, undefined, request.signal);
          if (imageUrl === null) return;
          input.imageUrl = imageUrl;
        }
        setProgress("Đang thêm biến thể…");
        updated = await createSellerVariant(shopId, productId, input, { signal: request.signal });
      }
      else if (action.type === "product") {
        const input = { ...action.input };
        if (Array.isArray(action.media)) {
          const mediaJson = await processMediaUpload(action.media, request.signal);
          if (mediaJson === null) return; // Bị hủy
          input.mediaJson = mediaJson;
        }
        setProgress("Đang lưu thông tin và media sản phẩm…");
        updated = await updateSellerProduct(shopId, productId, input, { signal: request.signal });
      }
      else if (action.type === "variant") {
        const input = { ...action.input };
        if (action.image) {
          const imageUrl = await processVariantImage(action.image, action.variant.id, request.signal);
          if (imageUrl === null) return; // Bị hủy
          input.imageUrl = imageUrl;
        }
        setProgress("Đang lưu biến thể…");
        updated = await updateSellerVariant(shopId, productId, action.variant.id, input, { signal: request.signal });
      }

      if (request.signal.aborted || getSession()?.userId !== sellerId) return;

      // Xử lý sau khi API thành công
      if (action.type === "stock") {
        const stock = validateVariantStock(updated, shopId, productId, action.variant.id);
        setInventory((current) => ({ stocks: { ...current?.stocks, [stock.variantId]: stock } }));
        setAction(null); setEditor(null);
        toast.success("Điều chỉnh tồn kho thành công.");
      } else if (action.type === "deleteProduct") {
        toast.success("Xóa sản phẩm thành công.");
        onChanged();
        onClose();
      } else {
        const product = validateSellerProduct(updated, shopId, productId);
        setResult({ product });
        setAction(null);
        setEditor(null);
        onChanged();
        if (action.type === "createVariant") refreshInventory();
        toast.success(action.type === "createVariant" ? "Thêm biến thể thành công. Sản phẩm sẽ chờ duyệt lại theo backend." : action.type === "status" ? "Đã cập nhật trạng thái bán sản phẩm." : action.type === "deleteVariant" ? "Xóa biến thể thành công." : "Cập nhật thành công.");
      }
    } catch (failure) {
      if (!request.signal.aborted && getSession()?.userId === sellerId) {
        const message = authErrorMessage(failure, "Không thực hiện được thao tác. Vui lòng thử lại.");
        setError(message);
        toast.error(message);
        if (action.type === "createVariant") {
          setAction(null); setEditor(null); setResult(null); setAttempt((value) => value + 1);
          onChanged();
        } else if (action.type === "stock") {
          setAction(null); setEditor(null); refreshInventory();
        } else onChanged();
      }
    } finally {
      lock.current = false;
      if (!request.signal.aborted) { setPending(false); setProgress(""); }
      mutation.current = null;
    }
  }

  // 4. KHU VỰC RENDER (Tách nhỏ giao diện)
  const product = result?.product;
  const activeVariants = product?.variants.filter((variant) => !variant.deletedAt) ?? [];

  // Hàm render: Dialog xác nhận Xóa/Lưu
  const renderActionConfirm = () => {
    if (!action) return null;
    return (
      <section role="alertdialog" aria-modal="true" aria-labelledby={confirmId} className="grid gap-4 rounded-xl border border-primary/25 bg-accent p-5">
        <h3 id={confirmId} className="text-lg font-bold">{action.title}</h3>
        <p className="break-words text-sm leading-7">{action.message}</p>

        {progress && <p role="status" className="text-sm text-accent-foreground">{progress}</p>}
        {error && <p role="alert" className="text-sm text-red-700">{error}</p>}

        <div className="flex flex-wrap gap-3">
          <Button variant="outline" autoFocus type="button" disabled={pending} onClick={() => setAction(null)} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">
            Quay lại
          </Button>
          <Button
            type="button"
            disabled={pending}
            onClick={() => void execute()}
            className={`min-h-11 rounded-lg px-4 text-sm font-bold text-white disabled:opacity-50 ${action.type.startsWith("delete") ? "bg-red-600" : "bg-primary"}`}
          >
            {pending ? "Đang xử lý…" : "Xác nhận"}
          </Button>
        </div>
      </section>
    );
  };

  // Hàm render: Khối chi tiết sản phẩm
  const renderProductDetails = () => {
    if (!result) {
      return <p role="status" className="text-sm text-muted-foreground">Đang tải sản phẩm…</p>;
    }
    if (result.error) {
      return (
        <div role="alert" className="grid gap-3 text-sm text-red-700">
          <p>{result.error}</p>
          <Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="min-h-11 rounded-lg border px-4 w-fit">Thử lại</Button>
        </div>
      );
    }

    return (
      <div className="grid gap-5">
        {/* Thông tin cơ bản */}
        <div>
          <h3 className="break-words text-xl font-bold">Tên sản phẩm: {product.name}</h3>
          <p className="mt-2 break-all text-xs text-muted-foreground">ID: {product.id}</p>
          <p className="mt-1 break-words text-sm text-muted-foreground">Slug: {product.slug}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
            {/* Trạng thái bán */}
            <span className={`rounded-md px-2 py-0.5 text-xs font-semibold ${product.status === 'ACTIVE'
                ? 'bg-green-100 text-green-700'
                : 'bg-muted text-muted-foreground'
              }`}>
              {productStatuses[product.status] ?? product.status}
            </span>

            {/* Trạng thái duyệt */}
            <span className="rounded-md bg-violet/10 px-2 py-0.5 text-xs font-semibold text-violet">
              {approvalStatuses[product.approvalStatus] ?? product.approvalStatus}
            </span>

            {/* Lượt bán */}
            <span className="text-xs font-medium">
              Đã bán: <strong className="text-foreground">{product.soldCount ?? 0}</strong>
            </span>
          </div>
          
        </div>

        {/* Media */}
        <div className="flex gap-3 overflow-x-auto">
          {mediaUrls(product.mediaJson).map((url) => (
            isVideo(url) ? (
              <video key={url} src={url} controls preload="metadata" playsInline className="h-32 w-44 shrink-0 rounded-lg bg-muted object-contain" />
            ) : (
              <img key={url} src={url} alt={product.name} loading="lazy" className="size-32 shrink-0 rounded-lg bg-muted object-contain" />
            )
          ))}
        </div>

        {/* Mô tả */}
        <div className="border-t-1 border-primary">
          <p className="whitespace-pre-line break-words text-sm leading-7 text-muted-foreground">
          {product.description || "Chưa có mô tả"}
        </p>
        </div>

        {/* Nút Hành động (Sửa / Xóa) */}
        <div className="flex flex-wrap gap-3">
          <Button variant="outline" type="button" disabled={Boolean(editor)} onClick={() => setEditor({ type: "product" })} className="min-h-11 rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary disabled:opacity-50">
            Sửa sản phẩm
          </Button>
          <Button
            type="button"
            disabled={Boolean(editor)}
            onClick={() => confirm({ type: "deleteProduct", title: "Xóa sản phẩm?", message: `Bạn có chắc muốn xóa sản phẩm “${product.name}”?` })}
            className="min-h-11 rounded-lg border border-red-200 px-4 text-sm font-semibold text-red-700 disabled:opacity-50"
          >
            Xóa sản phẩm
          </Button>
        </div>

        <div className="grid justify-items-start gap-2">
          <Button variant="outline" type="button" disabled={Boolean(editor)} onClick={() => confirm({ type: "status", status: product.status === "ACTIVE" ? "INACTIVE" : "ACTIVE", title: product.status === "ACTIVE" ? "Ngừng bán sản phẩm?" : "Bật bán sản phẩm?", message: "Xác nhận thay đổi trạng thái bán. Sản phẩm chỉ hiển thị công khai khi được duyệt, shop hoạt động và các điều kiện bán khác được đáp ứng." })} className="min-h-11 rounded-lg border border-primary/25 px-4 text-sm font-semibold text-primary disabled:opacity-50">{product.status === "ACTIVE" ? "Ngừng bán sản phẩm" : "Bật bán sản phẩm"}</Button>
          {product.approvalStatus !== "APPROVED" && <p className="text-xs text-muted-foreground">Sản phẩm chưa được duyệt: bật bán không làm sản phẩm xuất hiện công khai ngay.</p>}
        </div>

        {/* Form Sửa Sản phẩm */}
        {editor?.type === "product" && (
          <SellerProductEditForm
            product={product}
            categories={categories}
            categoriesError={categoriesError}
            pending={pending}
            onCancel={() => setEditor(null)}
            onConfirm={(input, media) => confirm({ type: "product", input, media, title: "Lưu thay đổi sản phẩm?", message: "Xác nhận cập nhật thông tin và media." })}
          />
        )}

        {/* Danh sách Biến thể */}
        <section className="grid gap-3 border-t border-border/60 pt-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h3 className="font-bold">Biến thể ({activeVariants.length})</h3>
            <Button variant="outline" type="button" disabled={pending || Boolean(editor) || !inventory} onClick={refreshInventory} className="min-h-11 rounded-lg border border-border px-3 text-sm font-semibold disabled:opacity-50">Tải lại tồn kho</Button>
          </div>
          {inventory?.error && <p role="alert" className="text-sm text-red-700">{inventory.error}</p>}
          <Button variant="default" type="button" disabled={Boolean(editor)} onClick={() => setEditor({ type: "createVariant" })} className="min-h-11 w-fit rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">+ Thêm biến thể</Button>
          {editor?.type === "createVariant" && <SellerVariantEditForm creating pending={pending} onCancel={() => setEditor(null)} onConfirm={(input, image) => confirm({ type: "createVariant", input, image, title: "Thêm biến thể mới?", message: "Xác nhận tạo biến thể với SKU, giá, thuộc tính và ảnh đã chọn. Sau khi thêm, kiểm tra tồn kho trước khi bán." })} />}
          {!activeVariants.length && <p className="text-sm text-muted-foreground">Chưa có biến thể chưa bị xóa.</p>}

          {activeVariants.map((variant) => (
            <div key={variant.id} className="rounded-xl border border-border p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="flex min-w-0 gap-3">
                  {isMediaUrl(variant.imageUrl) && (
                    <img src={variant.imageUrl} alt={variantName(variant)} loading="lazy" className="size-14 shrink-0 rounded-lg object-contain" />
                  )}
                  <div className="min-w-0">
                    <p className="break-words text-sm font-bold">{variantName(variant)}</p>
                    <p className="mt-1 break-all text-xs text-muted-foreground">SKU: {variant.sku} · ID: {variant.id}</p>
                    <p className="mt-2 text-sm">{money.format(variant.price)} · Đã bán: {variant.soldCount ?? 0} · {variant.active ? "Đang bán" : "Ngừng bán"}</p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" type="button" disabled={Boolean(editor)} onClick={() => setEditor({ type: "variant", id: variant.id })} className="min-h-11 rounded-lg border px-3 text-sm disabled:opacity-50">Sửa</Button>
                  <Button
                    type="button"
                    disabled={Boolean(editor)}
                    onClick={() => confirm({ type: "deleteVariant", variant, title: "Xóa biến thể?", message: `Bạn có chắc muốn xóa biến thể “${variantName(variant)}” (SKU: ${variant.sku})?` })}
                    className="min-h-11 rounded-lg border border-red-200 px-3 text-sm text-red-700 disabled:opacity-50"
                  >
                    Xóa
                  </Button>
                </div>
              </div>

              {/* Form Sửa Biến thể */}
              <SellerVariantStock
                stock={inventory?.stocks?.[variant.id]}
                loading={!inventory}
                error={inventory?.error}
                editing={editor?.type === "stock" && editor.id === variant.id}
                disabled={Boolean(editor)}
                pending={pending}
                onStart={() => setEditor({ type: "stock", id: variant.id })}
                onCancel={() => setEditor(null)}
                onConfirm={(newQuantity) => confirm({ type: "stock", variant, newQuantity, title: "Xác nhận điều chỉnh tồn kho?", message: `Đặt tồn thực tế của “${variantName(variant)}” từ ${inventory.stocks[variant.id].onHandQuantity.toLocaleString("vi-VN")} thành ${newQuantity.toLocaleString("vi-VN")}. Lượng đang giữ chỗ không thay đổi; backend sẽ kiểm tra lại trước khi lưu.` })}
              />
              {editor?.type === "variant" && editor.id === variant.id && (
                <div className="mt-4">
                  <SellerVariantEditForm
                    variant={variant}
                    pending={pending}
                    onCancel={() => setEditor(null)}
                    onConfirm={(input, image) => confirm({ type: "variant", variant, input, image, title: "Lưu thay đổi biến thể?", message: `Xác nhận cập nhật thông tin biến thể “${variantName(variant)}”.` })}
                  />
                </div>
              )}
            </div>
          ))}
        </section>
      </div>
    );
  };

  return (
    <dialog
      ref={dialog}
      aria-labelledby={titleId}
      onCancel={(event) => { event.preventDefault(); close(); }}
      onClick={(event) => { if (event.target === event.currentTarget) close(); }}
      className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-4xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50"
    >
      {/* HEADER CỦA DIALOG */}
      <div className="sticky top-0 z-10 flex items-center justify-between gap-3 border-b border-border/60 bg-card px-5 py-3">
        <h2 id={titleId} className="text-xl font-bold">Chi tiết sản phẩm</h2>
        <Button variant="outline" type="button" disabled={pending} onClick={close} aria-label="Đóng chi tiết sản phẩm" className="min-h-11 min-w-11 rounded-lg text-2xl disabled:opacity-50 hover:bg-background">
          ×
        </Button>
      </div>

      {/* BODY CỦA DIALOG */}
      <div className="p-5 sm:p-7" aria-busy={pending}>
        {renderActionConfirm()}

        {/* Ẩn nội dung chính khi đang hiển thị Dialog xác nhận */}
        <div hidden={Boolean(action)}>
          {renderProductDetails()}
        </div>
      </div>
    </dialog>
  );
}
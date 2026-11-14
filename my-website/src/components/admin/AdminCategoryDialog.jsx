import { Button } from "@/components/ui/button";
import { useEffect, useId, useRef, useState } from "react";
import { getCategory } from "../../api/category/categoryApi.js";
import { createCategory, updateCategory, updateCategoryStatus, deleteCategory } from "../../api/category/adminCategoryApi.js";
import { uploadMedia } from "../../api/media/mediaApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { Link } from "react-router-dom";
import { categorySearchPath } from "../common/catalogPaths.js";
import { registerToastHost, toast } from "../common/toast.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { categoryStatuses, validateAdminCategory } from "./adminCategoryModel.js";
import AdminCategoryForm from "./AdminCategoryForm.jsx";

export default function AdminCategoryDialog({ categoryId, mode, categories, adminId, onClose, onChanged }) {
  const dialog = useRef(null);
  const titleId = useId();
  const [result, setResult] = useState(() => categoryId ? null : { category: null });
  const [attempt, setAttempt] = useState(0);
  const [action, setAction] = useState(["hide", "activate"].includes(mode) ? { type: mode } : null);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState("");
  const [uncertain, setUncertain] = useState(false);
  const [createdId, setCreatedId] = useState(null);
  const created = useRef(null);
  const uploaded = useRef(new Map());
  const lock = useRef(false);
  const mutation = useRef(null);
  useEffect(() => {
    const node = dialog.current; node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); mutation.current?.abort(); };
  }, []);
  useEffect(() => {
    if (!categoryId) return;
    const request = new AbortController();
    getCategory(categoryId, { signal: request.signal }).then((category) => { if (!request.signal.aborted) setResult({ category: validateAdminCategory(category, categoryId) }); }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được danh mục.") }); });
    return () => request.abort();
  }, [categoryId, attempt]);
  function close() { if (!lock.current) { if (createdId || uncertain) onChanged(); onClose(); } }
  async function execute() {
    if (lock.current || !action || !result || uncertain || getSession()?.userId !== adminId) return;
    lock.current = true; setPending(true); setError("");
    const request = new AbortController(); mutation.current = request;
    let dispatchedCreate = false;
    try {
      let category;
      if (action.type === "hide") {
        await deleteCategory(categoryId, { signal: request.signal });
        if (request.signal.aborted || getSession()?.userId !== adminId) return;
        category = validateAdminCategory(await getCategory(categoryId, { signal: request.signal }), categoryId);
        if (category.status !== "INACTIVE") throw new Error("Chưa xác minh được kết quả ẩn danh mục.");
      } else if (action.type === "activate") {
        category = validateAdminCategory(await updateCategoryStatus(categoryId, "ACTIVE", { signal: request.signal }), categoryId);
        if (category.status !== "ACTIVE") throw new Error("Chưa xác minh được trạng thái kích hoạt.");
      } else {
        let id = categoryId || created.current?.id;
        let justCreated = false;
        if (!id) {
          setProgress("Đang tạo danh mục…"); dispatchedCreate = true;
          const response = await createCategory({ ...action.input, imageUrl: null }, { signal: request.signal });
          if (request.signal.aborted || getSession()?.userId !== adminId) return;
          category = validateAdminCategory(response);
          created.current = category; setCreatedId(category.id); id = category.id; justCreated = true;
        }
        const input = { ...action.input };
        if (action.image) {
          let url = uploaded.current.get(action.image.id);
          if (!url) {
            setProgress("Đang tải ảnh danh mục…");
            url = await uploadMedia(action.image.file, { domain: "categories", entityId: id, signal: request.signal });
            if (request.signal.aborted || getSession()?.userId !== adminId) return;
            if (url.length > 255) throw new Error("URL ảnh vượt giới hạn 255 ký tự của danh mục.");
            uploaded.current.set(action.image.id, url);
          }
          input.imageUrl = url;
        }
        if (!justCreated || action.image) {
          if (request.signal.aborted || getSession()?.userId !== adminId) return;
          setProgress("Đang lưu danh mục…");
          category = validateAdminCategory(await updateCategory(id, input, { signal: request.signal }), id);
        }
      }
      if (request.signal.aborted || getSession()?.userId !== adminId) return;
      toast.success(action.type === "hide" ? "Đã ẩn danh mục." : action.type === "activate" ? "Đã kích hoạt danh mục." : action.type === "create" ? "Tạo danh mục thành công." : "Cập nhật danh mục thành công.");
      onChanged(); onClose();
    } catch (failure) {
      if (!request.signal.aborted && getSession()?.userId === adminId) {
        if (dispatchedCreate && !created.current && (!failure.response || failure.response.status >= 500)) setUncertain(true);
        const message = (created.current ? "Danh mục đã được tạo, nhưng chưa lưu xong ảnh/thông tin. Thử lại sẽ dùng cùng danh mục. " : "") + authErrorMessage(failure, "Không thực hiện được thao tác danh mục.");
        setError(message); toast.error(message); onChanged();
        if (["hide", "activate"].includes(action.type)) { setResult(null); setAttempt((value) => value + 1); }
      }
    } finally { lock.current = false; if (!request.signal.aborted) { setPending(false); setProgress(""); } mutation.current = null; }
  }
  const category = result?.category;
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-2xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50"><div className="sticky top-0 z-10 flex items-center justify-between border-b border-border/60 bg-card px-5 py-3"><h2 id={titleId} className="text-xl font-bold">{categoryId ? "Quản lý danh mục" : "Thêm danh mục"}</h2><Button variant="outline" type="button" disabled={pending} onClick={close} aria-label="Đóng quản lý danh mục" className="min-h-11 min-w-11 text-2xl">×</Button></div>
    <div className="grid gap-4 p-5 sm:p-6" aria-busy={pending}>{!result ? <p role="status">Đang tải danh mục…</p> : result.error ? <div role="alert" className="grid gap-3 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="min-h-11 rounded-lg border px-4">Thử lại</Button></div> : <>
      {category && <div className="grid gap-1 text-xs text-muted-foreground"><p className="break-all">ID: {category.id}</p><p>Trạng thái: {categoryStatuses[category.status]}</p></div>}
      {action && <section role="alertdialog" aria-modal="true" aria-label="Xác nhận thay đổi danh mục" className="grid gap-3 rounded-xl border border-primary/25 bg-accent p-4"><h3 className="font-bold">{action.type === "hide" ? "Ẩn danh mục?" : action.type === "activate" ? "Kích hoạt danh mục?" : "Lưu thông tin danh mục?"}</h3><p className="break-words text-sm leading-7">{action.type === "hide" ? `Danh mục “${category?.name}” sẽ chuyển sang đã ẩn. Không xóa vĩnh viễn, không tự ẩn danh mục con hoặc xóa sản phẩm thuộc danh mục.` : action.type === "activate" ? `Hiển thị lại danh mục “${category?.name}”.` : action.input.name}</p>{createdId && <p className="break-all text-xs">Mã danh mục đã tạo: {createdId}</p>}{progress && <p role="status" className="text-sm">{progress}</p>}{error && <p role="alert" className="text-sm text-red-700">{error}</p>}{uncertain && <p className="text-sm text-amber-900">Chưa rõ kết quả tạo. Đóng dialog và kiểm tra danh sách trước khi tạo lại.</p>}<div className="flex flex-wrap gap-3"><Button variant="outline" autoFocus type="button" disabled={pending} onClick={() => { if (uncertain || ["hide", "activate"].includes(action.type)) close(); else setAction(null); }} className="min-h-11 rounded-lg border bg-card px-4 text-sm">{uncertain ? "Đóng và kiểm tra danh sách" : "Quay lại"}</Button><Button variant="default" type="button" disabled={pending || uncertain} onClick={() => void execute()} className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">{pending ? "Đang xử lý…" : "Xác nhận"}</Button></div></section>}
      <div hidden={Boolean(action)}><AdminCategoryForm editingId={categoryId || createdId} category={category} categories={categories} pending={pending} onCancel={close} onConfirm={(input, image) => { setError(""); setAction({ type: categoryId ? "update" : "create", input, image }); }} />{category?.status === "ACTIVE" && <Link to={categorySearchPath(category)} className="mt-4 inline-flex min-h-11 items-center text-sm font-semibold text-primary underline">Xem sản phẩm trong danh mục</Link>}</div>
    </>}</div>
  </dialog>;
}

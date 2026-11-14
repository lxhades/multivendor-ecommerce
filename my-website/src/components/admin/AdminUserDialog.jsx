import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useId, useRef, useState } from "react";
import { getAdminUser, updateAdminUser, setAdminUserStatus, deleteAdminUser } from "../../api/auth/adminUserApi.js";
import { getSession } from "../../api/auth/authSession.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { registerToastHost, toast } from "../common/toast.js";
import { adminUserContact, userRoles, userStatuses, validateAdminUser } from "./adminUserModel.js";

export default function AdminUserDialog({ userId, adminId, mode, desiredStatus, onClose, onChanged, onSelfUpdated }) {
  const dialog = useRef(null);
  const titleId = useId();
  const [result, setResult] = useState(null);
  const [attempt, setAttempt] = useState(0);
  const [action, setAction] = useState(mode === "edit" ? null : { type: mode, status: desiredStatus });
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const lock = useRef(false);
  const mutation = useRef(null);
  useEffect(() => {
    const node = dialog.current; node.showModal();
    const release = registerToastHost(node);
    return () => { release(); node.close(); mutation.current?.abort(); };
  }, []);
  useEffect(() => {
    const request = new AbortController();
    getAdminUser(userId, { signal: request.signal }).then((user) => { if (!request.signal.aborted) setResult({ user: validateAdminUser(user, userId) }); }).catch((failure) => { if (!request.signal.aborted) setResult({ error: authErrorMessage(failure, "Không tải được người dùng.") }); });
    return () => request.abort();
  }, [userId, attempt]);
  function close() { if (!lock.current) onClose(); }
  function submit(event) {
    event.preventDefault();
    if (lock.current) return;
    try { setError(""); setAction({ type: "update", input: adminUserContact(new FormData(event.currentTarget)) }); }
    catch (failure) { toast.error(failure.message); }
  }
  async function execute() {
    const user = result?.user;
    if (lock.current || !action || !user || getSession()?.userId !== adminId) return;
    if (userId === adminId && ["delete", "status"].includes(action.type)) { toast.error("Không thể tự cấm hoặc xóa tài khoản đang đăng nhập."); return; }
    const request = new AbortController(); mutation.current = request;
    lock.current = true; setPending(true); setError("");
    try {
      if (action.type === "delete") await deleteAdminUser(userId, { signal: request.signal });
      else if (action.type === "update") validateAdminUser(await updateAdminUser(userId, action.input, { signal: request.signal }), userId);
      else {
        const next = action.status;
        if (!["ACTIVE", "LOCKED"].includes(next)) throw new Error("Trạng thái yêu cầu không hợp lệ.");
        const saved = validateAdminUser(await setAdminUserStatus(userId, next, { signal: request.signal }), userId);
        if (saved.status !== next) throw new Error("Chưa xác minh được trạng thái tài khoản sau cập nhật.");
      }
      if (request.signal.aborted || getSession()?.userId !== adminId) return;
      toast.success(action.type === "delete" ? "Xóa người dùng thành công." : action.type === "update" ? "Cập nhật người dùng thành công." : action.status === "ACTIVE" ? "Đã bỏ cấm tài khoản." : "Đã cấm tài khoản.");
      onChanged(); onClose();
      if (action.type === "update" && userId === adminId) onSelfUpdated();
    } catch (failure) {
      if (!request.signal.aborted && getSession()?.userId === adminId) {
        const message = authErrorMessage(failure, "Không thực hiện được thao tác. Vui lòng kiểm tra lại.");
        setError(message); toast.error(message); onChanged();
        // Do not resend a status toggle using a stale snapshot after an ambiguous response.
        if (action.type === "status") { setResult(null); setAttempt((value) => value + 1); }
      }
    } finally { lock.current = false; if (!request.signal.aborted) setPending(false); mutation.current = null; }
  }
  const user = result?.user;
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); close(); }} onClick={(event) => { if (event.target === event.currentTarget) close(); }} className="m-auto max-h-[90dvh] w-[calc(100%_-_1.5rem)] max-w-xl overflow-y-auto rounded-2xl border border-border bg-card p-0 text-foreground shadow-xl backdrop:bg-night/50"><div className="sticky top-0 z-10 flex items-center justify-between border-b border-border/60 bg-card px-5 py-3"><h2 id={titleId} className="text-xl font-bold">Quản lý người dùng</h2><Button variant="outline" type="button" disabled={pending} onClick={close} aria-label="Đóng quản lý người dùng" className="min-h-11 min-w-11 text-2xl">×</Button></div>
    <div className="grid gap-4 p-5" aria-busy={pending}>{!result ? <p role="status">Đang tải người dùng…</p> : result.error ? <div role="alert" className="grid gap-3 text-sm text-red-700"><p>{result.error}</p><Button variant="outline" type="button" onClick={() => { setResult(null); setAttempt((value) => value + 1); }} className="min-h-11 rounded-lg border px-4">Thử lại</Button></div> : <>
      <div className="grid gap-2 text-sm"><p className="break-all"><strong>ID:</strong> {user.userId}</p><p className="break-words"><strong>Email:</strong> {user.email}</p><p><strong>Vai trò:</strong> {userRoles[user.role]}</p><p><strong>Trạng thái:</strong> {userStatuses[user.status]}</p></div>
      {action && <section role="alertdialog" aria-modal="true" aria-label="Xác nhận thao tác người dùng" className="grid gap-3 rounded-xl border border-primary/25 bg-accent p-4"><h3 className="font-bold">{action.type === "delete" ? "Xóa vĩnh viễn tài khoản?" : action.type === "update" ? "Lưu thay đổi thông tin?" : action.status === "ACTIVE" ? "Bỏ cấm tài khoản?" : "Cấm tài khoản?"}</h3><p className="break-words text-sm leading-7">{action.type === "delete" ? "Tài khoản và địa chỉ trong Auth sẽ bị xóa, không thể hoàn tác. Đơn hàng ở service khác được giữ nguyên." : action.type === "update" ? `Email: ${action.input.email}; điện thoại: ${action.input.phone}. Vai trò và mật khẩu được giữ nguyên.` : "Cập nhật trạng thái đăng nhập của người dùng này."}</p>{error && <p role="alert" className="text-sm text-red-700">{error}</p>}<div className="flex flex-wrap gap-3"><Button variant="outline" autoFocus type="button" disabled={pending} onClick={() => { if (action.type === "update") setAction(null); else close(); }} className="min-h-11 rounded-lg border bg-card px-4 text-sm">Hủy</Button><Button variant="default" type="button" disabled={pending || (userId === adminId && action.type !== "update")} onClick={() => void execute()} className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-40">{pending ? "Đang xử lý…" : "Xác nhận"}</Button></div></section>}
      <form hidden={Boolean(action)} onSubmit={submit} className="grid gap-4"><fieldset disabled={pending} className="grid gap-4"><label className="grid gap-2 text-sm font-semibold">Email<Input name="email" type="email" required maxLength={100} defaultValue={user.email} className="min-h-12 rounded-lg border border-input px-3" /></label><label className="grid gap-2 text-sm font-semibold">Số điện thoại<Input name="phone" type="tel" required pattern="0[0-9]{9}" maxLength={10} title="10 số bắt đầu bằng 0" defaultValue={user.phone} className="min-h-12 rounded-lg border border-input px-3" /></label><Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white">Lưu thay đổi</Button></fieldset></form>
    </>}</div>
  </dialog>;
}

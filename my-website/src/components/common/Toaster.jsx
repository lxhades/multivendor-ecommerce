import { createPortal } from "react-dom";
import { useEffect, useState, useSyncExternalStore } from "react";
import { getToast, getToastHost, subscribeToast, toast, toastConfig } from "./toast.js";

const getServerToast = () => null;

export default function Toaster() {
  const current = useSyncExternalStore(subscribeToast, getToast, getServerToast);
  const host = useSyncExternalStore(subscribeToast, getToastHost, getServerToast);
  const content = (
    <div className="pointer-events-none fixed inset-x-2 top-2 z-[110] sm:left-auto sm:w-72">
      <div role="status" aria-atomic="true">
        {current?.type === "success" && <ToastNotice key={current.id} notification={current} />}
      </div>
      <div role="alert" aria-atomic="true">
        {current?.type === "error" && <ToastNotice key={current.id} notification={current} />}
      </div>
    </div>
  );
  return host ? createPortal(content, host) : content;
}

function ToastNotice({ notification }) {
  const [hovered, setHovered] = useState(false);
  const [focused, setFocused] = useState(false);
  const failed = notification.type === "error";

  useEffect(() => {
    if (hovered || focused) return;
    const timer = setTimeout(
      () => toast.dismiss(notification.id),
      failed ? toastConfig.errorDurationMs : toastConfig.successDurationMs
    );
    return () => clearTimeout(timer);
  }, [notification.id, failed, hovered, focused]);

  return (
    <div
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocusCapture={() => setFocused(true)}
      onBlurCapture={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) setFocused(false);
      }}
      // Giảm padding từ p-4 -> p-3, bo góc từ rounded-xl -> rounded-lg
      className={`pointer-events-auto flex items-start gap-2.5 rounded-lg border bg-white p-3 shadow-md ${
        failed ? "border-red-200" : "border-green-200"
      }`}
    >
      {/* Icon nhỏ lại: size-7 -> size-5, font-base -> text-xs */}
      <span
        aria-hidden="true"
        className={`mt-0.5 grid size-5 shrink-0 place-items-center rounded-full text-xs font-bold ${
          failed ? "bg-red-50 text-red-700" : "bg-green-50 text-green-700"
        }`}
      >
        {failed ? "!" : "✓"}
      </span>

      <div className="min-w-0 flex-1">
        {/* Tiêu đề: text-sm -> text-xs */}
        <p className={`text-xs font-bold ${failed ? "text-red-800" : "text-green-800"}`}>
          {failed ? "Có lỗi xảy ra" : "Thành công"}
        </p>
        {/* Nội dung: text-sm -> text-[11px] (hoặc text-xs), leading-5 */}
        <p className="mt-0.5 text-xs leading-5 whitespace-pre-wrap break-words text-slate-600">
          {notification.message}
        </p>
      </div>

      {/* Nút đóng nhỏ lại: min-h-8 min-w-8, bỏ -mt-2 -mr-2 */}
      <button
        type="button"
        onClick={() => toast.dismiss(notification.id)}
        aria-label="Đóng thông báo"
        className="-mr-1.5 -mt-1.5 grid min-h-8 min-w-8 shrink-0 place-items-center rounded-md text-lg text-slate-400 hover:bg-slate-50 hover:text-slate-700"
      >
        ×
      </button>
    </div>
  );
}
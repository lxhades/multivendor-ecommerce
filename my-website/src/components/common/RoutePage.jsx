import { Skeleton } from "@/components/ui/skeleton";
import { Component, Suspense } from "react";
import { Link, useLocation } from "react-router-dom";

function PageLoading() {
  return <div role="status" aria-live="polite" className="mx-auto grid min-h-64 w-full max-w-6xl content-start gap-4 px-4 py-8 sm:px-6">
    <p className="text-sm text-slate-500">Đang tải trang…</p>
    <div aria-hidden="true" className="grid gap-4"><Skeleton className="h-8 w-1/3 rounded-lg" /><Skeleton className="h-36 rounded-2xl" /></div>
  </div>;
}

class PageErrorBoundary extends Component {
  state = { failed: false };
  static getDerivedStateFromError() { return { failed: true }; }
  render() {
    if (!this.state.failed) return this.props.children;
    return <section role="alert" className="mx-auto grid max-w-3xl justify-items-start gap-4 px-4 py-10">
      <h2 className="text-xl font-bold">Không thể hiển thị trang</h2>
      <p className="text-sm leading-7 text-slate-600">Có lỗi khi tải hoặc hiển thị trang. Kiểm tra kết nối và tải lại để thử lại; dữ liệu chưa lưu trên trang có thể bị mất.</p>
      <div className="flex flex-wrap gap-3"><button type="button" onClick={() => window.location.reload()} className="min-h-11 rounded-lg bg-orange-600 px-4 text-sm font-bold text-white">Tải lại trang</button><Link to="/" className="inline-flex min-h-11 items-center rounded-lg border border-slate-300 px-4 text-sm">Về trang chủ</Link></div>
    </section>;
  }
}

// Keep Header/Footer and role navigation mounted while a page chunk is loading.
export default function RoutePage({ children }) {
  const { pathname } = useLocation();
  return <PageErrorBoundary key={pathname}><Suspense fallback={<PageLoading />}>{children}</Suspense></PageErrorBoundary>;
}

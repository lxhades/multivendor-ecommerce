import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getAdminUsers } from "../../api/auth/adminUserApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { validateAdminUsers } from "../../components/admin/adminUserModel.js";
import AdminUsersTable from "../../components/admin/AdminUsersTable.jsx";
import AdminUserDialog from "../../components/admin/AdminUserDialog.jsx";

/* ------------------------- Icons ------------------------- */
const IconUsers = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M15 19.128a9.38 9.38 0 0 0 2.625.372 9.337 9.337 0 0 0 4.121-.952 4.125 4.125 0 0 0-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 0 1 8.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0 1 11.964-3.07M12 6.375a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0Zm8.25 2.25a2.625 2.625 0 1 1-5.25 0 2.625 2.625 0 0 1 5.25 0Z"
    />
  </svg>
);
const IconRefresh = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M16.023 9.348h4.992V4.356M3.02 14.652h4.992v4.992M4.5 9.35a7.5 7.5 0 0 1 12.36-3.536l4.155 4.156M19.5 14.65a7.5 7.5 0 0 1-12.36 3.535l-4.155-4.155"
    />
  </svg>
);
const IconSearch = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"
    />
  </svg>
);
const IconX = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
  </svg>
);
const IconAlert = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M12 9v3.75m9.303 3.376c.866 1.5-.217 3.374-1.948 3.374H4.645c-1.73 0-2.813-1.874-1.948-3.374l7.007-12.13c.866-1.5 3.032-1.5 3.898 0l7.701 13.13ZM12 15.75h.007v.008H12v-.008Z"
    />
  </svg>
);
const IconEmpty = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"
    />
  </svg>
);
const IconChevronLeft = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
  </svg>
);
const IconChevronRight = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
  </svg>
);
const IconHash = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M5.25 8.25h13.5m-13.5 7.5h13.5m-10.5-15-3 22.5m10.5-22.5-3 22.5"
    />
  </svg>
);

/* ------------------------- Page wrapper ------------------------- */
const adminUsersPageStyles = {
  refreshIcon: (loading) => `size-4 ${loading ? "animate-spin" : ""}`,
  container: "grid min-w-0 gap-6",
  container2: "relative overflow-hidden rounded-3xl border border-border bg-card p-6 sm:p-8",
  container3: "hidden",
  container4: "hidden",
  container5: "relative flex flex-wrap items-start justify-between gap-4",
  container6: "flex items-start gap-4",
  container7: "flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-accent text-primary",
  iconUsers: "h-5 w-5",
  title: "text-2xl font-extrabold tracking-tight text-foreground sm:text-3xl",
  description: "mt-1 max-w-2xl text-sm leading-7 text-muted-foreground",
  button: "inline-flex min-h-11 items-center gap-2 rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground shadow-sm transition hover:border-input hover:bg-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
  form: "rounded-2xl border border-border bg-card p-5 shadow-sm",
  filters: "flex flex-wrap items-end gap-3",
  label: "grid min-w-0 flex-1 gap-2 text-xs font-semibold text-muted-foreground",
  labelText: "inline-flex items-center gap-1.5",
  iconSearch: "h-3.5 w-3.5 text-muted-foreground",
  input: "min-h-12 min-w-0 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition placeholder:text-muted-foreground focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:bg-background disabled:text-muted-foreground",
  label2: "grid gap-2 text-xs font-semibold text-muted-foreground",
  select: "min-h-12 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:bg-background disabled:text-muted-foreground",
  primaryButton: "inline-flex min-h-12 items-center gap-2 rounded-lg bg-primary px-4 text-sm font-bold text-white shadow-sm transition hover:bg-primary/90 focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2",
  iconSearch2: "h-4 w-4",
  button2: "inline-flex min-h-12 items-center gap-2 rounded-lg border border-input bg-card px-4 text-sm font-semibold text-foreground transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-rose-200",
  container8: "flex items-center gap-3 rounded-2xl border border-border bg-card p-6 text-sm text-muted-foreground shadow-sm",
  labelText2: "inline-block h-4 w-4 animate-spin rounded-full border-2 border-input border-t-orange-500",
  errorContainer: "grid justify-items-start gap-4 rounded-2xl border border-red-200 bg-card from-red-50  p-5 text-sm text-red-700 shadow-sm",
  container9: "flex items-start gap-3",
  errorLabelText: "flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-red-100 text-red-600 ring-1 ring-red-200",
  description2: "leading-6",
  errorButton: "inline-flex min-h-11 items-center gap-2 rounded-lg border border-red-200 bg-card px-4 text-sm font-semibold text-red-700 transition hover:bg-red-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-300",
  container10: "flex flex-wrap items-center gap-2 text-sm text-muted-foreground",
  labelText3: "inline-flex items-center gap-1.5 rounded-full bg-muted px-3 py-1 font-semibold text-foreground",
  iconUsers2: "h-3.5 w-3.5",
  labelText4: "text-muted-foreground",
  labelText5: "font-semibold text-foreground",
  container11: "grid justify-items-center gap-3 rounded-2xl border border-dashed border-input bg-background/60 p-10 text-center shadow-sm",
  labelText6: "flex h-12 w-12 items-center justify-center rounded-full bg-muted text-muted-foreground ring-1 ring-slate-200",
  iconEmpty: "h-6 w-6",
  description3: "text-sm font-semibold text-foreground",
  description4: "max-w-sm text-xs leading-6 text-muted-foreground",
  container12: "overflow-hidden rounded-2xl border border-border bg-card shadow-sm",
  navigation: "flex flex-wrap items-center justify-center gap-2 text-sm",
  button3: "inline-flex min-h-11 items-center gap-1 rounded-full border border-border bg-card px-4 font-semibold text-foreground transition hover:border-input hover:bg-background disabled:cursor-not-allowed disabled:opacity-50",
  labelText7: "rounded-full bg-secondary px-4 py-2 text-xs font-bold text-white",
};

export default function AdminUsersPage() {
  const { auth } = useOutletContext();
  return (
    <AdminUsers
      key={auth.user.userId}
      adminId={auth.user.userId}
      onSelfUpdated={auth.retry}
    />
  );
}

function AdminUsers({ adminId, onSelfUpdated }) {
  const [result, setResult] = useState(null);
  const [filters, setFilters] = useState({ keyword: "", size: 20 });
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [attempt, setAttempt] = useState(0);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    const request = new AbortController();

    getAdminUsers({ ...filters, page }, { signal: request.signal })
      .then((data) => {
        if (request.signal.aborted) return;

        const checked = validateAdminUsers(data, page, filters.size);
        const lastPage = Math.max(0, checked.totalPages - 1);

        if (page > lastPage) {
          setPage(lastPage);
          return;
        }

        setResult({ data: checked });
      })
      .catch((failure) => {
        if (!request.signal.aborted) {
          setResult({
            error: authErrorMessage(
              failure,
              "Không tải được danh sách người dùng.",
            ),
          });
        }
      });

    return () => request.abort();
  }, [filters, page, attempt]);

  function refresh() {
    setResult(null);
    setAttempt((value) => value + 1);
  }

  function apply(event) {
    event.preventDefault();
    setResult(null);
    setPage(0);
    setFilters({
      keyword: keyword.trim(),
      size: Number(new FormData(event.currentTarget).get("size")),
    });
  }

  function changePage(next) {
    setResult(null);
    setPage(next);
  }

  function clearKeyword() {
    setKeyword("");
    setResult(null);
    setPage(0);
    setFilters((current) => ({ ...current, keyword: "" }));
  }

  const data = result?.data;
  const hasFilter = Boolean(filters.keyword);

  return (
    <div className={adminUsersPageStyles.container}>
      {/* -------- Hero header -------- */}
      <div className={adminUsersPageStyles.container2}>
        <div
          aria-hidden
          className={adminUsersPageStyles.container3}
        />
        <div
          aria-hidden
          className={adminUsersPageStyles.container4}
        />
        <div className={adminUsersPageStyles.container5}>
          <div className={adminUsersPageStyles.container6}>
            <div className={adminUsersPageStyles.container7}>
              <IconUsers className={adminUsersPageStyles.iconUsers} />
            </div>
            <div>
              <h2 className={adminUsersPageStyles.title}>
                Quản lý người dùng
              </h2>
              <p className={adminUsersPageStyles.description}>
                Tìm tài khoản, chỉnh thông tin liên hệ và quản lý trạng thái hoạt
                động.
              </p>
            </div>
          </div>
          <Button variant="outline"
            type="button"
            disabled={!result || Boolean(selected)}
            onClick={refresh}
            className={adminUsersPageStyles.button}
          >
            <IconRefresh
              className={adminUsersPageStyles.refreshIcon(!result)}
            />
            Tải lại
          </Button>
        </div>
      </div>

      {/* -------- Search form -------- */}
      <form
        role="search"
        onSubmit={apply}
        className={adminUsersPageStyles.form}
      >
        <fieldset
          disabled={Boolean(selected)}
          className={adminUsersPageStyles.filters}
        >
          <label className={adminUsersPageStyles.label}>
            <span className={adminUsersPageStyles.labelText}>
              <IconSearch className={adminUsersPageStyles.iconSearch} />
              Tìm theo email hoặc số điện thoại
            </span>
            <Input
              type="search"
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              maxLength={100}
              placeholder="Email hoặc số điện thoại…"
              className={adminUsersPageStyles.input}
            />
          </label>

          <label className={adminUsersPageStyles.label2}>
            <span className={adminUsersPageStyles.labelText}>
              <IconHash className={adminUsersPageStyles.iconSearch} />
              Mỗi trang
            </span>
            <select
              name="size"
              defaultValue="20"
              className={adminUsersPageStyles.select}
            >
              {[10, 20, 50].map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </label>

          <Button variant="default"
            type="submit"
            className={adminUsersPageStyles.primaryButton}
          >
            <IconSearch className={adminUsersPageStyles.iconSearch2} />
            Tìm kiếm
          </Button>

          {hasFilter && (
            <Button variant="outline"
              type="button"
              onClick={clearKeyword}
              className={adminUsersPageStyles.button2}
            >
              <IconX className={adminUsersPageStyles.iconSearch2} />
              Bỏ từ khóa
            </Button>
          )}
        </fieldset>
      </form>

      {/* -------- Results / state -------- */}
      {!result ? (
        <div
          role="status"
          className={adminUsersPageStyles.container8}
        >
          <span className={adminUsersPageStyles.labelText2} />
          Đang tải người dùng…
        </div>
      ) : result.error ? (
        <div
          role="alert"
          className={adminUsersPageStyles.errorContainer}
        >
          <div className={adminUsersPageStyles.container9}>
            <span className={adminUsersPageStyles.errorLabelText}>
              <IconAlert className={adminUsersPageStyles.iconUsers} />
            </span>
            <p className={adminUsersPageStyles.description2}>{result.error}</p>
          </div>
          <Button variant="outline"
            type="button"
            onClick={refresh}
            className={adminUsersPageStyles.errorButton}
          >
            <IconRefresh className={adminUsersPageStyles.iconSearch2} />
            Thử lại
          </Button>
        </div>
      ) : (
        <>
          {/* Counter pill */}
          <div className={adminUsersPageStyles.container10}>
            <span className={adminUsersPageStyles.labelText3}>
              <IconUsers className={adminUsersPageStyles.iconUsers2} />
              {data.totalElements.toLocaleString("vi-VN")} tài khoản
            </span>
            {hasFilter && (
              <span className={adminUsersPageStyles.labelText4}>
                khớp &ldquo;
                <span className={adminUsersPageStyles.labelText5}>
                  {filters.keyword}
                </span>
                &rdquo;
              </span>
            )}
          </div>

          {/* Table / empty */}
          {!data.users.length ? (
            <div className={adminUsersPageStyles.container11}>
              <span className={adminUsersPageStyles.labelText6}>
                <IconEmpty className={adminUsersPageStyles.iconEmpty} />
              </span>
              <p className={adminUsersPageStyles.description3}>
                Không tìm thấy người dùng phù hợp
              </p>
              <p className={adminUsersPageStyles.description4}>
                Thử đổi từ khóa tìm kiếm hoặc bấm &ldquo;Bỏ từ khóa&rdquo; để
                xem toàn bộ danh sách.
              </p>
            </div>
          ) : (
            <div className={adminUsersPageStyles.container12}>
              <AdminUsersTable
                users={data.users}
                adminId={adminId}
                onAction={(userId, mode, status) =>
                  setSelected({ userId, mode, status })
                }
              />
            </div>
          )}

          {/* Pagination */}
          {(data.totalPages > 1 || page > 0) && (
            <nav
              aria-label="Phân trang người dùng"
              className={adminUsersPageStyles.navigation}
            >
              <Button variant="outline"
                type="button"
                disabled={page === 0 || Boolean(selected)}
                onClick={() => changePage(page - 1)}
                className={adminUsersPageStyles.button3}
              >
                <IconChevronLeft className={adminUsersPageStyles.iconSearch2} />
                Trang trước
              </Button>
              <span className={adminUsersPageStyles.labelText7}>
                Trang {page + 1}/{Math.max(1, data.totalPages)}
              </span>
              <Button variant="outline"
                type="button"
                disabled={
                  page + 1 >= data.totalPages || Boolean(selected)
                }
                onClick={() => changePage(page + 1)}
                className={adminUsersPageStyles.button3}
              >
                Trang sau
                <IconChevronRight className={adminUsersPageStyles.iconSearch2} />
              </Button>
            </nav>
          )}
        </>
      )}

      {/* -------- Dialog -------- */}
      {selected && (
        <AdminUserDialog
          key={`${selected.userId}:${selected.mode}`}
          userId={selected.userId}
          adminId={adminId}
          mode={selected.mode}
          desiredStatus={selected.status}
          onClose={() => setSelected(null)}
          onChanged={refresh}
          onSelfUpdated={onSelfUpdated}
        />
      )}
    </div>
  );
}
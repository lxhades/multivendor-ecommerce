import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getCategories } from "../../api/category/categoryApi.js";
import { authErrorMessage } from "../../components/auth/authHelpers.js";
import { searchText } from "../../components/seller/sellerProductHelpers.js";
import {
  categoryStatuses,
  validateAdminCategory,
} from "../../components/admin/adminCategoryModel.js";
import AdminCategoriesTable from "../../components/admin/AdminCategoriesTable.jsx";
import AdminCategoryDialog from "../../components/admin/AdminCategoryDialog.jsx";

/* ------------------------- Icons ------------------------- */
const IconGrid = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6a2.25 2.25 0 0 1-2.25-2.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z" />
  </svg>
);
const IconPlus = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
  </svg>
);
const IconRefresh = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M16.023 9.348h4.992V4.356M3.02 14.652h4.992v4.992M4.5 9.35a7.5 7.5 0 0 1 12.36-3.536l4.155 4.156M19.5 14.65a7.5 7.5 0 0 1-12.36 3.535l-4.155-4.155" />
  </svg>
);
const IconSearch = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
  </svg>
);
const IconFilter = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v1.044a2.25 2.25 0 0 1-.659 1.591l-5.432 5.432a2.25 2.25 0 0 0-.659 1.591v2.927a2.25 2.25 0 0 1-1.244 2.013L9.75 21v-6.568a2.25 2.25 0 0 0-.659-1.591L3.659 7.409A2.25 2.25 0 0 1 3 5.818V4.774c0-.54.384-1.006.917-1.096A48.32 48.32 0 0 1 12 3Z" />
  </svg>
);
const IconX = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
  </svg>
);
const IconAlert = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9.303 3.376c.866 1.5-.217 3.374-1.948 3.374H4.645c-1.73 0-2.813-1.874-1.948-3.374l7.007-12.13c.866-1.5 3.032-1.5 3.898 0l7.701 13.13ZM12 15.75h.007v.008H12v-.008Z" />
  </svg>
);
const IconEmpty = (props) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" {...props}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 7.5 12 15.75 3.75 7.5m16.5 0v9a1.5 1.5 0 0 1-1.5 1.5H5.25a1.5 1.5 0 0 1-1.5-1.5v-9m16.5 0-8.25 5.25L3.75 7.5" />
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

/* ------------------------- Page wrapper ------------------------- */
const adminCategoriesPageStyles = {
  refreshIcon: (loading) => `size-4 ${loading ? "animate-spin" : ""}`,
  container: "grid min-w-0 gap-6",
  container2: "relative overflow-hidden rounded-3xl border border-border bg-card p-6 sm:p-8",
  container3: "hidden",
  container4: "hidden",
  container5: "relative flex flex-wrap items-start justify-between gap-4",
  container6: "flex items-start gap-4",
  container7: "flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-accent text-primary",
  iconGrid: "h-5 w-5",
  title: "text-2xl font-extrabold tracking-tight text-foreground sm:text-3xl",
  description: "mt-1 max-w-2xl text-sm leading-7 text-muted-foreground",
  container8: "flex flex-wrap gap-2",
  primaryButton: "inline-flex min-h-11 items-center gap-2 rounded-lg bg-primary px-4 text-sm font-bold text-white shadow-sm transition hover:bg-primary/90 focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
  iconPlus: "h-4 w-4",
  button: "inline-flex min-h-11 items-center gap-2 rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground transition hover:border-input hover:bg-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
  filters: "grid gap-4 rounded-2xl border border-border bg-card p-5 shadow-sm sm:grid-cols-2 xl:grid-cols-4",
  label: "grid min-w-0 gap-2 text-xs font-semibold text-muted-foreground",
  labelText: "inline-flex items-center gap-1.5",
  iconSearch: "h-3.5 w-3.5 text-muted-foreground",
  input: "min-h-12 min-w-0 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition placeholder:text-muted-foreground focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:bg-background disabled:text-muted-foreground",
  label2: "grid gap-2 text-xs font-semibold text-muted-foreground",
  select: "min-h-12 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:bg-background disabled:text-muted-foreground",
  select2: "min-h-12 min-w-0 rounded-lg border border-input bg-card px-3 text-sm text-foreground shadow-sm transition focus:border-primary/60 focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:bg-background disabled:text-muted-foreground",
  button2: "inline-flex min-h-11 w-fit items-center gap-2 rounded-lg border border-input bg-card px-3 text-sm font-semibold text-foreground transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-rose-200",
  container9: "flex flex-wrap items-center gap-2 text-sm text-muted-foreground",
  labelText2: "inline-flex items-center gap-1.5 rounded-full bg-muted px-3 py-1 font-semibold text-foreground",
  iconGrid2: "h-3.5 w-3.5",
  container10: "flex items-center gap-3 rounded-2xl border border-border bg-card p-6 text-sm text-muted-foreground shadow-sm",
  labelText3: "inline-block h-4 w-4 animate-spin rounded-full border-2 border-input border-t-orange-500",
  errorContainer: "grid justify-items-start gap-4 rounded-2xl border border-red-200 bg-card from-red-50  p-5 text-sm text-red-700 shadow-sm",
  container11: "flex items-start gap-3",
  errorLabelText: "flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-red-100 text-red-600 ring-1 ring-red-200",
  description2: "leading-6",
  errorButton: "inline-flex min-h-11 items-center gap-2 rounded-lg border border-red-200 bg-card px-4 text-sm font-semibold text-red-700 transition hover:bg-red-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-300",
  container12: "grid justify-items-center gap-3 rounded-2xl border border-dashed border-input bg-background/60 p-10 text-center shadow-sm",
  labelText4: "flex h-12 w-12 items-center justify-center rounded-full bg-muted text-muted-foreground ring-1 ring-slate-200",
  iconEmpty: "h-6 w-6",
  description3: "text-sm font-semibold text-foreground",
  description4: "max-w-sm text-xs leading-6 text-muted-foreground",
  container13: "overflow-hidden rounded-2xl border border-border bg-card shadow-sm",
  navigation: "flex flex-wrap items-center justify-center gap-2 text-sm",
  button3: "inline-flex min-h-11 items-center gap-1 rounded-full border border-border bg-card px-4 font-semibold text-foreground transition hover:border-input hover:bg-background disabled:cursor-not-allowed disabled:opacity-50",
  labelText5: "rounded-full bg-secondary px-4 py-2 text-xs font-bold text-white",
};

export default function AdminCategoriesPage() {
  const { auth } = useOutletContext();
  return <AdminCategories key={auth.user.userId} adminId={auth.user.userId} />;
}

function AdminCategories({ adminId }) {
  const [result, setResult] = useState({ loading: true, categories: [] });
  const [attempt, setAttempt] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [parent, setParent] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    const request = new AbortController();

    getCategories({ signal: request.signal })
      .then((data) => {
        if (request.signal.aborted) return;

        if (!Array.isArray(data)) {
          throw new Error("Danh sách danh mục không hợp lệ.");
        }

        const categories = data.map((category) => validateAdminCategory(category));

        if (
          new Set(categories.map((category) => category.id)).size !==
          categories.length
        ) {
          throw new Error("Danh sách danh mục bị trùng ID.");
        }

        categories.sort(
          (a, b) =>
            a.displayOrder - b.displayOrder ||
            a.name.localeCompare(b.name, "vi") ||
            a.id.localeCompare(b.id),
        );

        setResult({ categories, loading: false });
      })
      .catch((failure) => {
        if (!request.signal.aborted) {
          setResult((current) => ({
            ...current,
            loading: false,
            error: authErrorMessage(failure, "Không tải được danh mục."),
          }));
        }
      });

    return () => request.abort();
  }, [attempt]);

  function refresh() {
    setResult((current) => ({ ...current, loading: true, error: "" }));
    setPage(0);
    setAttempt((value) => value + 1);
  }

  const categories = result.categories;
  const term = searchText(keyword);
  const names = new Map(categories.map((category) => [category.id, category.name]));

  const filtered = categories.filter((category) => {
    const matchStatus = !status || category.status === status;
    const matchParent =
      !parent ||
      (parent === "ROOT" ? !category.parentId : category.parentId === parent);
    const haystack = searchText(
      [
        category.id,
        category.name,
        category.slug,
        names.get(category.parentId) ?? category.parentName,
      ].join(" "),
    );
    return matchStatus && matchParent && haystack.includes(term);
  });

  const totalPages = Math.ceil(filtered.length / size);
  const currentPage = Math.min(page, Math.max(0, totalPages - 1));
  const items = filtered.slice(currentPage * size, (currentPage + 1) * size);
  const hasActiveFilter = Boolean(keyword || status || parent);

  return (
    <div className={adminCategoriesPageStyles.container}>
      {/* -------- Hero header -------- */}
      <div className={adminCategoriesPageStyles.container2}>
        <div
          aria-hidden
          className={adminCategoriesPageStyles.container3}
        />
        <div
          aria-hidden
          className={adminCategoriesPageStyles.container4}
        />
        <div className={adminCategoriesPageStyles.container5}>
          <div className={adminCategoriesPageStyles.container6}>
            <div className={adminCategoriesPageStyles.container7}>
              <IconGrid className={adminCategoriesPageStyles.iconGrid} />
            </div>
            <div>
              <h2 className={adminCategoriesPageStyles.title}>
                Quản lý danh mục
              </h2>
              <p className={adminCategoriesPageStyles.description}>
                Danh mục cha/con, hình ảnh và thứ tự hiển thị.
              </p>
            </div>
          </div>
          <div className={adminCategoriesPageStyles.container8}>
            <Button variant="default"
              type="button"
              disabled={result.loading || Boolean(result.error) || Boolean(selected)}
              onClick={() => setSelected({ id: null, mode: "create" })}
              className={adminCategoriesPageStyles.primaryButton}
            >
              <IconPlus className={adminCategoriesPageStyles.iconPlus} />
              Thêm danh mục
            </Button>
            <Button variant="outline"
              type="button"
              disabled={result.loading || Boolean(selected)}
              onClick={refresh}
              className={adminCategoriesPageStyles.button}
            >
              <IconRefresh
                className={adminCategoriesPageStyles.refreshIcon(result.loading)}
              />
              Tải lại
            </Button>
          </div>
        </div>
      </div>

      {/* -------- Filter card -------- */}
      <fieldset
        disabled={Boolean(selected)}
        className={adminCategoriesPageStyles.filters}
      >
        <label className={adminCategoriesPageStyles.label}>
          <span className={adminCategoriesPageStyles.labelText}>
            <IconSearch className={adminCategoriesPageStyles.iconSearch} />
            Tìm danh mục
          </span>
          <Input
            type="search"
            value={keyword}
            onChange={(event) => {
              setKeyword(event.target.value);
              setPage(0);
            }}
            maxLength={150}
            placeholder="Tên, slug, ID hoặc tên danh mục cha…"
            className={adminCategoriesPageStyles.input}
          />
        </label>

        <label className={adminCategoriesPageStyles.label2}>
          <span className={adminCategoriesPageStyles.labelText}>
            <IconFilter className={adminCategoriesPageStyles.iconSearch} />
            Trạng thái
          </span>
          <select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value);
              setPage(0);
            }}
            className={adminCategoriesPageStyles.select}
          >
            <option value="">Tất cả</option>
            {Object.entries(categoryStatuses).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>

        <label className={adminCategoriesPageStyles.label2}>
          <span className={adminCategoriesPageStyles.labelText}>
            <IconFilter className={adminCategoriesPageStyles.iconSearch} />
            Danh mục cha
          </span>
          <select
            value={parent}
            onChange={(event) => {
              setParent(event.target.value);
              setPage(0);
            }}
            className={adminCategoriesPageStyles.select2}
          >
            <option value="">Tất cả cấp</option>
            <option value="ROOT">Chỉ danh mục gốc</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                Con trực tiếp của {category.name}
              </option>
            ))}
          </select>
        </label>

        <label className={adminCategoriesPageStyles.label2}>
          <span className={adminCategoriesPageStyles.labelText}>
            <IconGrid className={adminCategoriesPageStyles.iconSearch} />
            Mỗi trang
          </span>
          <select
            value={size}
            onChange={(event) => {
              setSize(Number(event.target.value));
              setPage(0);
            }}
            className={adminCategoriesPageStyles.select}
          >
            {[10, 20, 50].map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>

        {hasActiveFilter && (
          <Button variant="outline"
            type="button"
            onClick={() => {
              setKeyword("");
              setStatus("");
              setParent("");
              setPage(0);
            }}
            className={adminCategoriesPageStyles.button2}
          >
            <IconX className={adminCategoriesPageStyles.iconPlus} />
            Bỏ bộ lọc
          </Button>
        )}
      </fieldset>

      {/* -------- Result counter -------- */}
      {!result.loading && !result.error && (
        <div className={adminCategoriesPageStyles.container9}>
          <span className={adminCategoriesPageStyles.labelText2}>
            <IconGrid className={adminCategoriesPageStyles.iconGrid2} />
            {filtered.length}/{categories.length} danh mục
          </span>
          <span>
            khớp bộ lọc. Tìm kiếm trên toàn bộ danh sách; phân trang hiển thị
            phía frontend.
          </span>
        </div>
      )}

      {/* -------- Loading / Error / Empty / Table -------- */}
      {result.loading ? (
        <div
          role="status"
          className={adminCategoriesPageStyles.container10}
        >
          <span className={adminCategoriesPageStyles.labelText3} />
          Đang tải danh mục…
        </div>
      ) : result.error ? (
        <div
          role="alert"
          className={adminCategoriesPageStyles.errorContainer}
        >
          <div className={adminCategoriesPageStyles.container11}>
            <span className={adminCategoriesPageStyles.errorLabelText}>
              <IconAlert className={adminCategoriesPageStyles.iconGrid} />
            </span>
            <p className={adminCategoriesPageStyles.description2}>{result.error}</p>
          </div>
          <Button variant="outline"
            type="button"
            onClick={refresh}
            className={adminCategoriesPageStyles.errorButton}
          >
            <IconRefresh className={adminCategoriesPageStyles.iconPlus} />
            Thử lại
          </Button>
        </div>
      ) : !items.length ? (
        <div className={adminCategoriesPageStyles.container12}>
          <span className={adminCategoriesPageStyles.labelText4}>
            <IconEmpty className={adminCategoriesPageStyles.iconEmpty} />
          </span>
          <p className={adminCategoriesPageStyles.description3}>
            Không có danh mục phù hợp
          </p>
          <p className={adminCategoriesPageStyles.description4}>
            Thử đổi từ khóa hoặc xóa bộ lọc để xem toàn bộ danh mục.
          </p>
        </div>
      ) : (
        <div className={adminCategoriesPageStyles.container13}>
          <AdminCategoriesTable
            categories={items}
            allCategories={categories}
            onOpen={(id, mode) => setSelected({ id, mode })}
          />
        </div>
      )}

      {/* -------- Pagination -------- */}
      {!result.loading && !result.error && totalPages > 1 && (
        <nav
          aria-label="Phân trang danh mục"
          className={adminCategoriesPageStyles.navigation}
        >
          <Button variant="outline"
            type="button"
            disabled={currentPage === 0 || Boolean(selected)}
            onClick={() => setPage(currentPage - 1)}
            className={adminCategoriesPageStyles.button3}
          >
            <IconChevronLeft className={adminCategoriesPageStyles.iconPlus} />
            Trang trước
          </Button>
          <span className={adminCategoriesPageStyles.labelText5}>
            Trang {currentPage + 1}/{totalPages}
          </span>
          <Button variant="outline"
            type="button"
            disabled={currentPage + 1 >= totalPages || Boolean(selected)}
            onClick={() => setPage(currentPage + 1)}
            className={adminCategoriesPageStyles.button3}
          >
            Trang sau
            <IconChevronRight className={adminCategoriesPageStyles.iconPlus} />
          </Button>
        </nav>
      )}

      {selected && (
        <AdminCategoryDialog
          key={`${selected.id ?? "new"}:${selected.mode}`}
          categoryId={selected.id}
          mode={selected.mode}
          categories={categories}
          adminId={adminId}
          onClose={() => setSelected(null)}
          onChanged={refresh}
        />
      )}
    </div>
  );
}